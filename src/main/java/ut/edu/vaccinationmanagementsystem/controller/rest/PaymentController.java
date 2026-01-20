package ut.edu.vaccinationmanagementsystem.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.Payment;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.PaymentStatus;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;
import ut.edu.vaccinationmanagementsystem.repository.PaymentRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.AppointmentService;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.PaymentService;
import ut.edu.vaccinationmanagementsystem.service.VnPayService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    
    @Autowired
    private VnPayService vnPayService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private AppointmentService appointmentService;
    
    /**
     * POST /api/payment/create-vnpay-url
     * Tạo URL thanh toán VNPay cho appointment
     */
    @PostMapping("/create-vnpay-url")
    public ResponseEntity<?> createVnPayUrl(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long appointmentId = Long.valueOf(request.get("appointmentId").toString());
            
            // Get appointment
            Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Check authorization
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null || appointment.getBookedByUser() == null || 
                !appointment.getBookedByUser().getId().equals(currentUser.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Get payment
            Payment payment = paymentService.findByAppointment(appointment);
            if (payment == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Check if already paid
            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment already completed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Create VNPay URL
            long amount = payment.getAmount().longValue();
            String orderInfo = "Thanh toan dat lich tiem chung - " + appointment.getBookingCode();
            String orderId = appointment.getBookingCode();
            String ipAddress = getClientIpAddress(httpRequest);
            
            String paymentUrl = vnPayService.createPaymentUrl(amount, orderInfo, orderId, ipAddress);
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("appointmentId", appointmentId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/payment/vnpay-return
     * Callback từ VNPay sau khi thanh toán
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnPayReturn(@RequestParam Map<String, String> params, HttpServletRequest request) {
        try {
            // Get response code và orderId trước để có thể xử lý cancel
            String responseCode = params.get("vnp_ResponseCode");
            String orderId = params.get("vnp_TxnRef");
            
            // Nếu không có orderId, không thể xử lý
            if (orderId == null || orderId.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid request: missing order ID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Find appointment by booking code
            Appointment appointment = appointmentRepository.findByBookingCode(orderId)
                .orElse(null);
            
            // Nếu không tìm thấy appointment, có thể đã bị xóa hoặc không tồn tại
            if (appointment == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Lịch hẹn không tồn tại hoặc đã bị hủy");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Payment payment = paymentService.findByAppointment(appointment);
            if (payment == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Verify signature - lấy raw query string từ request
            String queryString = request.getQueryString();
            if (queryString != null && queryString.contains("vnp_SecureHash")) {
                if (!vnPayService.verifySignatureFromQueryString(queryString)) {
                    // Signature không hợp lệ - xóa appointment nếu là VNPay payment
                    if (payment.getPaymentMethod().toString().equals("VNPAY") && 
                        appointment.getStatus().toString().equals("PENDING")) {
                        try {
                            appointmentService.deleteAppointmentWhenPaymentFailed(appointment.getId());
                        } catch (Exception e) {
                            // Log error but continue
                        }
                    }
                    
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid signature");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            
            String transactionId = params.get("vnp_TransactionNo");
            Map<String, Object> response = new HashMap<>();
            
            // Check response code (00 = success, 24 = user cancel, null/empty = user quit)
            if ("00".equals(responseCode)) {
                // Payment successful
                paymentService.markPaymentAsPaid(payment, transactionId);
                
                // Redirect to success page
                String redirectUrl = String.format("/payment-result?success=true&bookingCode=%s&appointmentId=%d",
                    appointment.getBookingCode(), appointment.getId());
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
            } else if ("24".equals(responseCode) || responseCode == null || responseCode.isEmpty()) {
                // User cancel (24) hoặc user quit (null/empty) - Xóa appointment
                Long appointmentId = appointment.getId();
                
                try {
                    // Xóa appointment (sẽ rollback slot và xóa payment)
                    appointmentService.deleteAppointmentWhenPaymentFailed(appointmentId);
                } catch (Exception e) {
                    // Nếu không xóa được, chỉ đánh dấu payment failed
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    if (transactionId != null) {
                        payment.setTransactionId(transactionId);
                    }
                    paymentRepository.save(payment);
                }
                
                // Redirect to error page
                String redirectUrl = "/payment-result?success=false&message=" + 
                    java.net.URLEncoder.encode("Bạn đã hủy thanh toán. Lịch hẹn đã được hủy.", StandardCharsets.UTF_8);
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
            } else {
                // Payment failed (các mã lỗi khác) - Xóa appointment và payment
                Long appointmentId = appointment.getId();
                
                try {
                    // Xóa appointment (sẽ rollback slot và xóa payment)
                    appointmentService.deleteAppointmentWhenPaymentFailed(appointmentId);
                } catch (Exception e) {
                    // Nếu không xóa được, chỉ đánh dấu payment failed
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    if (transactionId != null) {
                        payment.setTransactionId(transactionId);
                    }
                    paymentRepository.save(payment);
                }
                
                // Redirect to error page
                String redirectUrl = "/payment-result?success=false&message=" + 
                    java.net.URLEncoder.encode("Thanh toán thất bại. Mã lỗi: " + responseCode + ". Lịch hẹn đã được hủy.", StandardCharsets.UTF_8);
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/payment/status/{appointmentId}
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/status/{appointmentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long appointmentId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Check authorization
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser == null || appointment.getBookedByUser() == null || 
                !appointment.getBookedByUser().getId().equals(currentUser.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Payment payment = paymentService.findByAppointment(appointment);
            if (payment == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("paymentStatus", payment.getPaymentStatus().toString());
            response.put("paymentMethod", payment.getPaymentMethod().toString());
            response.put("amount", payment.getAmount());
            response.put("invoiceNumber", payment.getInvoiceNumber());
            response.put("transactionId", payment.getTransactionId());
            response.put("paidAt", payment.getPaidAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Helper method to get current user
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return null;
        }
        
        try {
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                return customOAuth2User.getUser();
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                return customUserDetails.getUser();
            } else {
                String email = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                return userOpt.orElse(null);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
            return request.getRemoteAddr();
    }
    
    /**
     * POST /api/payment/{appointmentId}/mark-paid-cash
     * Đánh dấu thanh toán tiền mặt (cho Receptionist)
     */
    @PostMapping("/{appointmentId}/mark-paid-cash")
    public ResponseEntity<?> markPaidCash(@PathVariable Long appointmentId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            Payment payment = paymentService.findByAppointment(appointment);
            if (payment == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment already completed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Mark as paid
            paymentService.markPaymentAsPaid(payment, "CASH-" + appointmentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đánh dấu thanh toán thành công");
            response.put("paymentStatus", payment.getPaymentStatus().toString());
            response.put("appointmentId", appointmentId);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

