package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.BookAppointmentDTO;
import ut.edu.vaccinationmanagementsystem.dto.ConsultationRequestDTO;
import ut.edu.vaccinationmanagementsystem.dto.WalkInAppointmentDTO;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.AppointmentService;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * POST /api/appointments/consultation-request
     * Tạo yêu cầu tư vấn (hỗ trợ cả user đã đăng nhập và guest)
     */
    @PostMapping("/consultation-request")
    public ResponseEntity<?> createConsultationRequest(@RequestBody ConsultationRequestDTO dto) {
        try {
            // Validate required fields
            if (dto.getConsultationPhone() == null || dto.getConsultationPhone().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Số điện thoại là bắt buộc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Validate guest fields if no user is authenticated
            // (This will be checked in service based on authentication)
            
            Appointment appointment = appointmentService.createConsultationRequest(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Gửi yêu cầu tư vấn thành công");
            response.put("bookingCode", appointment.getBookingCode());
            response.put("appointmentId", appointment.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    
    /**
     * POST /api/appointments/book
     * Đặt lịch trực tiếp (chỉ dành cho user đã đăng nhập)
     */
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody BookAppointmentDTO dto) {
        try {
            Appointment appointment = appointmentService.bookAppointment(dto);
            
            // Check cảnh báo cùng ngày
            Long userIdToCheck = null;
            Long familyMemberIdToCheck = null;
            if (dto.getBookedForUserId() != null) {
                familyMemberIdToCheck = dto.getBookedForUserId();
            } else {
                // Lấy current user ID
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                    User currentUser = null;
                    if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                        currentUser = customOAuth2User.getUser();
                    } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                        currentUser = customUserDetails.getUser();
                    } else {
                        String email = authentication.getName();
                        Optional<User> userOpt = userRepository.findByEmail(email);
                        if (userOpt.isPresent()) {
                            currentUser = userOpt.get();
                        }
                    }
                    if (currentUser != null) {
                        userIdToCheck = currentUser.getId();
                    }
                }
            }
            
            String sameDayWarning = appointmentService.checkSameDayAppointmentWarning(
                    appointment.getAppointmentDate(), 
                    userIdToCheck, 
                    familyMemberIdToCheck,
                    appointment.getId() // Exclude appointment vừa tạo
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đặt lịch thành công");
            response.put("bookingCode", appointment.getBookingCode());
            response.put("appointmentId", appointment.getId());
            response.put("appointmentDate", appointment.getAppointmentDate());
            response.put("appointmentTime", appointment.getAppointmentTime());
            response.put("centerName", appointment.getCenter().getName());
            response.put("vaccineName", appointment.getVaccine().getName());
            if (sameDayWarning != null) {
                response.put("warning", sameDayWarning);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    
    /**
     * PUT /api/appointments/{id}/cancel
     * Hủy lịch hẹn (chỉ cho phép hủy nếu trạng thái là PENDING)
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        try {
            // Lấy current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User must be authenticated to cancel appointment");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            User currentUser = null;
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                currentUser = customOAuth2User.getUser();
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                currentUser = customUserDetails.getUser();
            } else {
                String email = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    currentUser = userOpt.get();
                }
            }
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Appointment appointment = appointmentService.cancelAppointment(id, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Hủy lịch hẹn thành công");
            response.put("appointmentId", appointment.getId());
            response.put("bookingCode", appointment.getBookingCode());
            response.put("status", appointment.getStatus().name());
            
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
    
    /**
     * GET /api/appointments/today
     * Xem lịch hẹn hôm nay (cho Receptionist)
     * Query params: 
     *   - status (optional) - Lọc theo trạng thái
     *   - date (optional) - Ngày cần xem (format: YYYY-MM-DD), nếu không có thì mặc định là hôm nay
     */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date) {
        try {
            AppointmentStatus statusFilter = null;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    statusFilter = AppointmentStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid status: " + status);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            }
            
            java.time.LocalDate targetDate;
            if (date != null && !date.trim().isEmpty()) {
                try {
                    targetDate = java.time.LocalDate.parse(date);
                } catch (Exception e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid date format. Use YYYY-MM-DD");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
            } else {
                targetDate = java.time.LocalDate.now();
            }
            
            List<Appointment> appointments = appointmentService.getAppointmentsByDate(targetDate, statusFilter);
            
            // Convert to Map để tránh circular reference khi serialize JSON
            List<Map<String, Object>> appointmentDTOs = appointments.stream().map(apt -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", apt.getId());
                dto.put("bookingCode", apt.getBookingCode());
                dto.put("status", apt.getStatus() != null ? apt.getStatus().name() : null);
                dto.put("appointmentDate", apt.getAppointmentDate());
                dto.put("appointmentTime", apt.getAppointmentTime());
                dto.put("queueNumber", apt.getQueueNumber());
                dto.put("doseNumber", apt.getDoseNumber());
                dto.put("notes", apt.getNotes());
                dto.put("requiresConsultation", apt.getRequiresConsultation());
                dto.put("consultationPhone", apt.getConsultationPhone());
                dto.put("guestFullName", apt.getGuestFullName());
                dto.put("guestEmail", apt.getGuestEmail());
                dto.put("guestDayOfBirth", apt.getGuestDayOfBirth());
                dto.put("guestGender", apt.getGuestGender());
                
                // Patient info
                if (apt.getBookedForUser() != null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", apt.getBookedForUser().getId());
                    user.put("fullName", apt.getBookedForUser().getFullName());
                    user.put("email", apt.getBookedForUser().getEmail());
                    user.put("phoneNumber", apt.getBookedForUser().getPhoneNumber());
                    user.put("dayOfBirth", apt.getBookedForUser().getDayOfBirth());
                    user.put("gender", apt.getBookedForUser().getGender());
                    dto.put("bookedForUser", user);
                }
                
                if (apt.getFamilyMember() != null) {
                    Map<String, Object> member = new HashMap<>();
                    member.put("id", apt.getFamilyMember().getId());
                    member.put("fullName", apt.getFamilyMember().getFullName());
                    member.put("phoneNumber", apt.getFamilyMember().getPhoneNumber());
                    member.put("dateOfBirth", apt.getFamilyMember().getDateOfBirth());
                    member.put("gender", apt.getFamilyMember().getGender());
                    dto.put("familyMember", member);
                }
                
                // Vaccine info
                if (apt.getVaccine() != null) {
                    Map<String, Object> vaccine = new HashMap<>();
                    vaccine.put("id", apt.getVaccine().getId());
                    vaccine.put("name", apt.getVaccine().getName());
                    vaccine.put("price", apt.getVaccine().getPrice());
                    vaccine.put("status", apt.getVaccine().getStatus() != null ? apt.getVaccine().getStatus().name() : null);
                    dto.put("vaccine", vaccine);
                }
                
                // Center info
                if (apt.getCenter() != null) {
                    Map<String, Object> center = new HashMap<>();
                    center.put("id", apt.getCenter().getId());
                    center.put("name", apt.getCenter().getName());
                    center.put("address", apt.getCenter().getAddress());
                    center.put("phoneNumber", apt.getCenter().getPhoneNumber());
                    dto.put("center", center);
                }
                
                // Room info
                if (apt.getRoom() != null) {
                    Map<String, Object> room = new HashMap<>();
                    room.put("id", apt.getRoom().getId());
                    room.put("roomNumber", apt.getRoom().getRoomNumber());
                    dto.put("room", room);
                }
                
                // Payment info (chỉ lấy thông tin cần thiết, không lấy lại appointment để tránh circular)
                if (apt.getPayment() != null) {
                    Map<String, Object> payment = new HashMap<>();
                    payment.put("id", apt.getPayment().getId());
                    payment.put("amount", apt.getPayment().getAmount());
                    payment.put("paymentMethod", apt.getPayment().getPaymentMethod() != null ? apt.getPayment().getPaymentMethod().name() : null);
                    payment.put("paymentStatus", apt.getPayment().getPaymentStatus() != null ? apt.getPayment().getPaymentStatus().name() : null);
                    payment.put("invoiceNumber", apt.getPayment().getInvoiceNumber());
                    payment.put("transactionId", apt.getPayment().getTransactionId());
                    payment.put("paidAt", apt.getPayment().getPaidAt());
                    dto.put("payment", payment);
                }
                
                return dto;
            }).collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointmentDTOs);
            response.put("count", appointmentDTOs.size());
            response.put("date", targetDate.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/appointments/{id}/confirm
     * Xác nhận lịch hẹn (cho Receptionist)
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Long id) {
        try {
            // Lấy current user (receptionist)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            User currentUser = null;
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                currentUser = customOAuth2User.getUser();
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                currentUser = customUserDetails.getUser();
            } else {
                String email = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    currentUser = userOpt.get();
                }
            }
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Appointment appointment = appointmentService.confirmAppointmentByReceptionist(id, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment confirmed successfully");
            response.put("appointmentId", appointment.getId());
            response.put("status", appointment.getStatus().name());
            
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
    
    /**
     * POST /api/appointments/{id}/cancel-by-receptionist
     * Hủy lịch hẹn bởi Receptionist
     */
    @PostMapping("/{id}/cancel-by-receptionist")
    public ResponseEntity<?> cancelAppointmentByReceptionist(@PathVariable Long id, @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            // Lấy current user (receptionist)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            User currentUser = null;
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                currentUser = customOAuth2User.getUser();
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                currentUser = customUserDetails.getUser();
            } else {
                String email = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    currentUser = userOpt.get();
                }
            }
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String reason = requestBody != null ? requestBody.get("reason") : null;
            Appointment appointment = appointmentService.cancelAppointmentByReceptionist(id, currentUser, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment cancelled successfully");
            response.put("appointmentId", appointment.getId());
            response.put("status", appointment.getStatus().name());
            
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
    
    /**
     * POST /api/appointments/{id}/check-in
     * Check-in khách hàng (cho Receptionist)
     */
    @PostMapping("/{id}/check-in")
    public ResponseEntity<?> checkInAppointment(@PathVariable Long id) {
        try {
            // Lấy current user (receptionist)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User must be authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            User currentUser = null;
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                currentUser = customOAuth2User.getUser();
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                currentUser = customUserDetails.getUser();
            } else {
                String email = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    currentUser = userOpt.get();
                }
            }
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Appointment appointment = appointmentService.checkInAppointment(id, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Check-in thành công");
            response.put("appointmentId", appointment.getId());
            response.put("bookingCode", appointment.getBookingCode());
            response.put("status", appointment.getStatus().name());
            response.put("queueNumber", appointment.getQueueNumber());
            
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
    
    /**
     * GET /api/appointments/search
     * Tìm kiếm appointment theo số điện thoại hoặc booking code (cho Receptionist)
     * Query params: phone (optional), bookingCode (optional)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAppointments(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String bookingCode) {
        try {
            if ((phone == null || phone.trim().isEmpty()) && 
                (bookingCode == null || bookingCode.trim().isEmpty())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Phải cung cấp số điện thoại hoặc mã booking để tìm kiếm");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<Appointment> appointments = appointmentService.searchAppointments(phone, bookingCode);
            
            // Convert to Map để tránh circular reference khi serialize JSON (giống endpoint /today)
            List<Map<String, Object>> appointmentDTOs = appointments.stream().map(apt -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", apt.getId());
                dto.put("bookingCode", apt.getBookingCode());
                dto.put("status", apt.getStatus() != null ? apt.getStatus().name() : null);
                dto.put("appointmentDate", apt.getAppointmentDate());
                dto.put("appointmentTime", apt.getAppointmentTime());
                dto.put("queueNumber", apt.getQueueNumber());
                dto.put("doseNumber", apt.getDoseNumber());
                dto.put("notes", apt.getNotes());
                dto.put("requiresConsultation", apt.getRequiresConsultation());
                dto.put("consultationPhone", apt.getConsultationPhone());
                dto.put("guestFullName", apt.getGuestFullName());
                dto.put("guestEmail", apt.getGuestEmail());
                dto.put("guestDayOfBirth", apt.getGuestDayOfBirth());
                dto.put("guestGender", apt.getGuestGender());
                
                // Patient info
                if (apt.getBookedForUser() != null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", apt.getBookedForUser().getId());
                    user.put("fullName", apt.getBookedForUser().getFullName());
                    user.put("email", apt.getBookedForUser().getEmail());
                    user.put("phoneNumber", apt.getBookedForUser().getPhoneNumber());
                    user.put("dayOfBirth", apt.getBookedForUser().getDayOfBirth());
                    user.put("gender", apt.getBookedForUser().getGender());
                    dto.put("bookedForUser", user);
                }
                
                if (apt.getFamilyMember() != null) {
                    Map<String, Object> member = new HashMap<>();
                    member.put("id", apt.getFamilyMember().getId());
                    member.put("fullName", apt.getFamilyMember().getFullName());
                    member.put("phoneNumber", apt.getFamilyMember().getPhoneNumber());
                    member.put("dateOfBirth", apt.getFamilyMember().getDateOfBirth());
                    member.put("gender", apt.getFamilyMember().getGender());
                    dto.put("familyMember", member);
                }
                
                // Vaccine info
                if (apt.getVaccine() != null) {
                    Map<String, Object> vaccine = new HashMap<>();
                    vaccine.put("id", apt.getVaccine().getId());
                    vaccine.put("name", apt.getVaccine().getName());
                    vaccine.put("price", apt.getVaccine().getPrice());
                    vaccine.put("status", apt.getVaccine().getStatus() != null ? apt.getVaccine().getStatus().name() : null);
                    dto.put("vaccine", vaccine);
                }
                
                // Center info
                if (apt.getCenter() != null) {
                    Map<String, Object> center = new HashMap<>();
                    center.put("id", apt.getCenter().getId());
                    center.put("name", apt.getCenter().getName());
                    center.put("address", apt.getCenter().getAddress());
                    center.put("phoneNumber", apt.getCenter().getPhoneNumber());
                    dto.put("center", center);
                }
                
                // Room info
                if (apt.getRoom() != null) {
                    Map<String, Object> room = new HashMap<>();
                    room.put("id", apt.getRoom().getId());
                    room.put("roomNumber", apt.getRoom().getRoomNumber());
                    dto.put("room", room);
                }
                
                // Payment info
                if (apt.getPayment() != null) {
                    Map<String, Object> payment = new HashMap<>();
                    payment.put("id", apt.getPayment().getId());
                    payment.put("amount", apt.getPayment().getAmount());
                    payment.put("paymentMethod", apt.getPayment().getPaymentMethod() != null ? apt.getPayment().getPaymentMethod().name() : null);
                    payment.put("paymentStatus", apt.getPayment().getPaymentStatus() != null ? apt.getPayment().getPaymentStatus().name() : null);
                    payment.put("invoiceNumber", apt.getPayment().getInvoiceNumber());
                    payment.put("transactionId", apt.getPayment().getTransactionId());
                    payment.put("paidAt", apt.getPayment().getPaidAt());
                    dto.put("payment", payment);
                }
                
                return dto;
            }).collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointmentDTOs);
            response.put("count", appointmentDTOs.size());
            
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
    
    /**
     * POST /api/appointments/walk-in
     * Đăng ký walk-in (không đặt trước) - cho Receptionist
     */
    @PostMapping("/walk-in")
    public ResponseEntity<?> createWalkInAppointment(@RequestBody WalkInAppointmentDTO dto) {
        try {
            // Lấy current user (receptionist)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User must be authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            User currentUser = null;
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                currentUser = customOAuth2User.getUser();
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                currentUser = customUserDetails.getUser();
            } else {
                String email = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    currentUser = userOpt.get();
                }
            }
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Appointment appointment = appointmentService.createWalkInAppointment(dto, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tạo walk-in appointment thành công");
            response.put("bookingCode", appointment.getBookingCode());
            response.put("appointmentId", appointment.getId());
            response.put("appointmentDate", appointment.getAppointmentDate());
            response.put("appointmentTime", appointment.getAppointmentTime());
            response.put("centerName", appointment.getCenter().getName());
            response.put("vaccineName", appointment.getVaccine().getName());
            response.put("status", appointment.getStatus().name());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    
    /**
     * GET /api/appointments/{id}/detail
     * Lấy chi tiết appointment (cho Receptionist)
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getAppointmentDetail(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", appointment.getId());
            response.put("bookingCode", appointment.getBookingCode());
            response.put("status", appointment.getStatus().name());
            response.put("appointmentDate", appointment.getAppointmentDate());
            response.put("appointmentTime", appointment.getAppointmentTime());
            response.put("queueNumber", appointment.getQueueNumber());
            response.put("doseNumber", appointment.getDoseNumber());
            response.put("notes", appointment.getNotes());
            response.put("requiresConsultation", appointment.getRequiresConsultation());
            response.put("consultationPhone", appointment.getConsultationPhone());
            
            // Patient info
            if (appointment.getBookedForUser() != null) {
                Map<String, Object> patient = new HashMap<>();
                patient.put("id", appointment.getBookedForUser().getId());
                patient.put("fullName", appointment.getBookedForUser().getFullName());
                patient.put("email", appointment.getBookedForUser().getEmail());
                patient.put("phoneNumber", appointment.getBookedForUser().getPhoneNumber());
                patient.put("dayOfBirth", appointment.getBookedForUser().getDayOfBirth());
                patient.put("gender", appointment.getBookedForUser().getGender());
                response.put("patient", patient);
                response.put("patientType", "USER");
            } else if (appointment.getFamilyMember() != null) {
                Map<String, Object> patient = new HashMap<>();
                patient.put("id", appointment.getFamilyMember().getId());
                patient.put("fullName", appointment.getFamilyMember().getFullName());
                patient.put("phoneNumber", appointment.getFamilyMember().getPhoneNumber());
                patient.put("dayOfBirth", appointment.getFamilyMember().getDateOfBirth());
                patient.put("gender", appointment.getFamilyMember().getGender());
                response.put("patient", patient);
                response.put("patientType", "FAMILY_MEMBER");
            } else {
                Map<String, Object> patient = new HashMap<>();
                patient.put("fullName", appointment.getGuestFullName());
                patient.put("email", appointment.getGuestEmail());
                patient.put("phoneNumber", appointment.getConsultationPhone());
                patient.put("dayOfBirth", appointment.getGuestDayOfBirth());
                patient.put("gender", appointment.getGuestGender());
                response.put("patient", patient);
                response.put("patientType", "GUEST");
            }
            
            // Vaccine info
            if (appointment.getVaccine() != null) {
                Map<String, Object> vaccine = new HashMap<>();
                vaccine.put("id", appointment.getVaccine().getId());
                vaccine.put("name", appointment.getVaccine().getName());
                vaccine.put("price", appointment.getVaccine().getPrice());
                response.put("vaccine", vaccine);
            }
            
            // Center info
            if (appointment.getCenter() != null) {
                Map<String, Object> center = new HashMap<>();
                center.put("id", appointment.getCenter().getId());
                center.put("name", appointment.getCenter().getName());
                center.put("address", appointment.getCenter().getAddress());
                center.put("phoneNumber", appointment.getCenter().getPhoneNumber());
                response.put("center", center);
            }
            
            // Room info
            if (appointment.getRoom() != null) {
                Map<String, Object> room = new HashMap<>();
                room.put("id", appointment.getRoom().getId());
                room.put("roomNumber", appointment.getRoom().getRoomNumber());
                response.put("room", room);
            }
            
            // Payment info
            if (appointment.getPayment() != null) {
                Map<String, Object> payment = new HashMap<>();
                payment.put("id", appointment.getPayment().getId());
                payment.put("amount", appointment.getPayment().getAmount());
                payment.put("paymentMethod", appointment.getPayment().getPaymentMethod().name());
                payment.put("paymentStatus", appointment.getPayment().getPaymentStatus().name());
                payment.put("invoiceNumber", appointment.getPayment().getInvoiceNumber());
                payment.put("transactionId", appointment.getPayment().getTransactionId());
                payment.put("paidAt", appointment.getPayment().getPaidAt());
                response.put("payment", payment);
            }
            
            // Vaccination record info
            if (appointment.getVaccinationRecord() != null) {
                Map<String, Object> vaccinationRecord = new HashMap<>();
                vaccinationRecord.put("id", appointment.getVaccinationRecord().getId());
                vaccinationRecord.put("certificateNumber", appointment.getVaccinationRecord().getCertificateNumber());
                vaccinationRecord.put("injectionDate", appointment.getVaccinationRecord().getInjectionDate());
                vaccinationRecord.put("injectionTime", appointment.getVaccinationRecord().getInjectionTime());
                vaccinationRecord.put("injectionSite", appointment.getVaccinationRecord().getInjectionSite());
                vaccinationRecord.put("doseNumber", appointment.getVaccinationRecord().getDoseNumber());
                response.put("vaccinationRecord", vaccinationRecord);
            }
            
            // Appointment history
            List<Map<String, Object>> histories = new java.util.ArrayList<>();
            if (appointment.getAppointmentHistories() != null && !appointment.getAppointmentHistories().isEmpty()) {
                histories = appointment.getAppointmentHistories().stream()
                    .sorted((a, b) -> b.getChangedAt().compareTo(a.getChangedAt()))
                    .map(h -> {
                        Map<String, Object> history = new HashMap<>();
                        history.put("oldStatus", h.getOldStatus() != null ? h.getOldStatus().name() : null);
                        history.put("newStatus", h.getNewStatus() != null ? h.getNewStatus().name() : null);
                        history.put("changedAt", h.getChangedAt());
                        history.put("changedBy", h.getChangedBy() != null ? h.getChangedBy().getFullName() : "System");
                        history.put("reason", h.getReason());
                        return history;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            response.put("histories", histories);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/appointments/approved
     * Lấy danh sách lịch hẹn đã phê duyệt (APPROVED) - cho Nurse
     */
    @GetMapping("/approved")
    public ResponseEntity<?> getApprovedAppointments() {
        try {
            List<Appointment> appointments = appointmentService.getApprovedAppointments();
            
            // Convert to Map để tránh circular reference khi serialize JSON
            List<Map<String, Object>> appointmentDTOs = appointments.stream().map(apt -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", apt.getId());
                dto.put("bookingCode", apt.getBookingCode());
                dto.put("appointmentDate", apt.getAppointmentDate());
                dto.put("appointmentTime", apt.getAppointmentTime());
                dto.put("status", apt.getStatus());
                dto.put("queueNumber", apt.getQueueNumber());
                dto.put("doseNumber", apt.getDoseNumber());
                dto.put("notes", apt.getNotes());
                
                // Patient info
                if (apt.getBookedForUser() != null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", apt.getBookedForUser().getId());
                    user.put("fullName", apt.getBookedForUser().getFullName());
                    user.put("email", apt.getBookedForUser().getEmail());
                    user.put("phoneNumber", apt.getBookedForUser().getPhoneNumber());
                    user.put("dayOfBirth", apt.getBookedForUser().getDayOfBirth());
                    user.put("gender", apt.getBookedForUser().getGender());
                    dto.put("bookedForUser", user);
                } else if (apt.getFamilyMember() != null) {
                    Map<String, Object> member = new HashMap<>();
                    member.put("id", apt.getFamilyMember().getId());
                    member.put("fullName", apt.getFamilyMember().getFullName());
                    member.put("phoneNumber", apt.getFamilyMember().getPhoneNumber());
                    member.put("dateOfBirth", apt.getFamilyMember().getDateOfBirth());
                    member.put("gender", apt.getFamilyMember().getGender());
                    dto.put("familyMember", member);
                } else {
                    dto.put("guestFullName", apt.getGuestFullName());
                    dto.put("guestEmail", apt.getGuestEmail());
                    dto.put("guestDayOfBirth", apt.getGuestDayOfBirth());
                    dto.put("guestGender", apt.getGuestGender());
                    dto.put("consultationPhone", apt.getConsultationPhone());
                }
                
                // Vaccine info
                if (apt.getVaccine() != null) {
                    Map<String, Object> vaccine = new HashMap<>();
                    vaccine.put("id", apt.getVaccine().getId());
                    vaccine.put("name", apt.getVaccine().getName());
                    vaccine.put("price", apt.getVaccine().getPrice());
                    dto.put("vaccine", vaccine);
                }
                
                // Center info
                if (apt.getCenter() != null) {
                    Map<String, Object> center = new HashMap<>();
                    center.put("id", apt.getCenter().getId());
                    center.put("name", apt.getCenter().getName());
                    center.put("address", apt.getCenter().getAddress());
                    dto.put("center", center);
                }
                
                // Room info
                if (apt.getRoom() != null) {
                    Map<String, Object> room = new HashMap<>();
                    room.put("id", apt.getRoom().getId());
                    room.put("roomNumber", apt.getRoom().getRoomNumber());
                    dto.put("room", room);
                }
                
                return dto;
            }).collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointmentDTOs);
            response.put("count", appointmentDTOs.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}


