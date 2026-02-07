package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.BookAppointmentDTO;
import ut.edu.vaccinationmanagementsystem.dto.ConsultationRequestDTO;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.Payment;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;
import ut.edu.vaccinationmanagementsystem.repository.StaffInfoRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.AppointmentService;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.PaymentService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private StaffInfoRepository staffInfoRepository;
    
    @Autowired
    private PaymentService paymentService;

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

    @GetMapping("/{id}/cancellation-fee")
    public ResponseEntity<?> getCancellationFee(@PathVariable Long id) {
        try {
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
            
            Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Kiểm tra quyền
            if (appointment.getBookedByUser() == null || !appointment.getBookedByUser().getId().equals(currentUser.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bạn không có quyền xem thông tin lịch hẹn này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> response = new HashMap<>();
            
            if (appointment.getStatus() == AppointmentStatus.CONFIRMED && 
                appointment.getAppointmentDate() != null && 
                appointment.getAppointmentTime() != null &&
                appointment.getPayment() != null) {
                
                java.time.LocalDateTime appointmentDateTime = java.time.LocalDateTime.of(
                    appointment.getAppointmentDate(), appointment.getAppointmentTime());
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                long hoursUntilAppointment = java.time.Duration.between(now, appointmentDateTime).toHours();
                
                if (hoursUntilAppointment < 6) {
                    response.put("canCancel", false);
                    response.put("message", "Không thể hủy lịch hẹn trong vòng 6 giờ trước giờ hẹn");
                    response.put("hoursUntilAppointment", hoursUntilAppointment);
                } else {
                    response.put("canCancel", true);
                    java.math.BigDecimal originalAmount = appointment.getPayment().getAmount();
                    java.math.BigDecimal cancellationFee = paymentService.calculateCancellationFee(
                        originalAmount, hoursUntilAppointment);
                    
                    response.put("cancellationFee", cancellationFee);
                    response.put("originalAmount", originalAmount);
                    response.put("hoursUntilAppointment", hoursUntilAppointment);
                    // Tính phần trăm phí
                    int feePercentage = 0;
                    if (hoursUntilAppointment >= 24) {
                        feePercentage = 0;
                    } else if (hoursUntilAppointment >= 12) {
                        feePercentage = 20;
                    } else if (hoursUntilAppointment >= 6) {
                        feePercentage = 50;
                    } else {
                        feePercentage = 100;
                    }
                    response.put("feePercentage", feePercentage);
                }
            } else if (appointment.getStatus() == AppointmentStatus.PENDING) {
                response.put("canCancel", true);
                response.put("cancellationFee", java.math.BigDecimal.ZERO);
                response.put("message", "Hủy miễn phí cho lịch hẹn chưa xác nhận");
            } else {
                response.put("canCancel", false);
                response.put("message", "Không thể hủy lịch hẹn với trạng thái hiện tại");
            }
            
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
            
            
            Payment payment = appointment.getPayment();
            BigDecimal cancellationFee = null;
            if (payment != null && payment.getCancellationFee() != null) {
                cancellationFee = payment.getCancellationFee();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Hủy lịch hẹn thành công");
            response.put("appointmentId", appointment.getId());
            response.put("bookingCode", appointment.getBookingCode());
            response.put("status", appointment.getStatus().name());
            if (cancellationFee != null) {
                response.put("cancellationFee", cancellationFee);
                response.put("hasCancellationFee", true);
                response.put("cancellationFeePaid", payment.getCancellationFeePaid() != null && payment.getCancellationFeePaid());
            } else {
                response.put("hasCancellationFee", false);
            }
            
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

    @GetMapping("/approved")
    public ResponseEntity<?> getApprovedAppointments() {
        try {
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(apt -> apt.getStatus() == AppointmentStatus.APPROVED)
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> result = appointments.stream().map(apt -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", apt.getId());
                map.put("bookingCode", apt.getBookingCode());
                map.put("appointmentDate", apt.getAppointmentDate());
                map.put("appointmentTime", apt.getAppointmentTime());
                map.put("vaccineName", apt.getVaccine() != null ? apt.getVaccine().getName() : null);
                map.put("centerName", apt.getCenter() != null ? apt.getCenter().getName() : null);
                map.put("roomNumber", apt.getRoom() != null ? apt.getRoom().getRoomNumber() : null);
                map.put("doseNumber", apt.getDoseNumber());
                map.put("status", apt.getStatus().name());
                
                // Thông tin người được tiêm
                if (apt.getFamilyMember() != null) {
                    map.put("patientName", apt.getFamilyMember().getFullName());
                    map.put("patientType", "FAMILY_MEMBER");
                } else if (apt.getBookedForUser() != null) {
                    map.put("patientName", apt.getBookedForUser().getFullName());
                    map.put("patientType", "USER");
                } else if (apt.getBookedByUser() != null) {
                    map.put("patientName", apt.getBookedByUser().getFullName());
                    map.put("patientType", "USER");
                }
                
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

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
            
            // Make variables final for lambda usage
            final AppointmentStatus finalStatusFilter = statusFilter;
            final java.time.LocalDate finalTargetDate = targetDate;
            
            // Lấy appointments theo ngày và status
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(apt -> apt.getAppointmentDate() != null && apt.getAppointmentDate().equals(finalTargetDate))
                    .filter(apt -> finalStatusFilter == null || apt.getStatus() == finalStatusFilter)
                    .collect(Collectors.toList());
            
            // Lọc theo trung tâm (ngoại trừ ADMIN)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUserCheck = getCurrentUserFromAuth(auth);
            if (currentUserCheck != null && currentUserCheck.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                ut.edu.vaccinationmanagementsystem.entity.StaffInfo staffInfo = staffInfoRepository.findByUser(currentUserCheck).orElse(null);
                if (staffInfo != null && staffInfo.getCenter() != null) {
                    Long centerId = staffInfo.getCenter().getId();
                    appointments = appointments.stream()
                            .filter(apt -> apt.getCenter() != null && apt.getCenter().getId().equals(centerId))
                            .collect(Collectors.toList());
                }
            }
            
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
                
                // Patient info - ưu tiên: familyMember > bookedForUser > bookedByUser
                // (familyMember là người thân sẽ được tiêm, nên ưu tiên cao nhất)
                
                // Family member info (nếu có - đây là người sẽ được tiêm khi đặt cho người thân)
                if (apt.getFamilyMember() != null) {
                    Map<String, Object> member = new HashMap<>();
                    member.put("id", apt.getFamilyMember().getId());
                    member.put("fullName", apt.getFamilyMember().getFullName());
                    member.put("phoneNumber", apt.getFamilyMember().getPhoneNumber());
                    member.put("dateOfBirth", apt.getFamilyMember().getDateOfBirth());
                    member.put("gender", apt.getFamilyMember().getGender());
                    dto.put("familyMember", member);
                }
                
                // BookedForUser info (người được đặt lịch cho - nếu không phải family member)
                if (apt.getBookedForUser() != null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", apt.getBookedForUser().getId());
                    user.put("fullName", apt.getBookedForUser().getFullName());
                    user.put("email", apt.getBookedForUser().getEmail());
                    user.put("phoneNumber", apt.getBookedForUser().getPhoneNumber());
                    user.put("dayOfBirth", apt.getBookedForUser().getDayOfBirth());
                    user.put("gender", apt.getBookedForUser().getGender());
                    dto.put("bookedForUser", user);
                } else if (apt.getBookedByUser() != null && apt.getFamilyMember() == null) {
                    // Fallback: nếu bookedForUser null và không có familyMember, dùng bookedByUser
                    // (chỉ dùng khi đặt cho chính mình)
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", apt.getBookedByUser().getId());
                    user.put("fullName", apt.getBookedByUser().getFullName());
                    user.put("email", apt.getBookedByUser().getEmail());
                    user.put("phoneNumber", apt.getBookedByUser().getPhoneNumber());
                    user.put("dayOfBirth", apt.getBookedByUser().getDayOfBirth());
                    user.put("gender", apt.getBookedByUser().getGender());
                    dto.put("bookedForUser", user);
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
            }).collect(Collectors.toList());
            
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

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Long id) {
        try {
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
            
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Kiểm tra center (ngoại trừ ADMIN)
            if (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                ut.edu.vaccinationmanagementsystem.entity.StaffInfo staffInfo = staffInfoRepository.findByUser(currentUser).orElse(null);
                if (staffInfo != null && staffInfo.getCenter() != null) {
                    Long userCenterId = staffInfo.getCenter().getId();
                    if (appointment.getCenter() == null || !appointment.getCenter().getId().equals(userCenterId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Bạn chỉ có thể xác nhận lịch hẹn của trung tâm mình");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            if (appointment.getStatus() != AppointmentStatus.PENDING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chỉ có thể xác nhận lịch hẹn ở trạng thái PENDING");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointmentRepository.save(appointment);
            
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

    @PostMapping("/{id}/cancel-by-receptionist")
    public ResponseEntity<?> cancelAppointmentByReceptionist(@PathVariable Long id, @RequestBody(required = false) Map<String, String> requestBody) {
        try {
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
            
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            
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

    @PostMapping("/{id}/check-in")
    public ResponseEntity<?> checkInAppointment(@PathVariable Long id) {
        try {
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
            
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Kiểm tra center (ngoại trừ ADMIN)
            if (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                ut.edu.vaccinationmanagementsystem.entity.StaffInfo staffInfo = staffInfoRepository.findByUser(currentUser).orElse(null);
                if (staffInfo != null && staffInfo.getCenter() != null) {
                    Long userCenterId = staffInfo.getCenter().getId();
                    if (appointment.getCenter() == null || !appointment.getCenter().getId().equals(userCenterId)) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Bạn chỉ có thể check-in lịch hẹn của trung tâm mình");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                    }
                }
            }
            
            if (appointment.getStatus() != AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.PENDING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chỉ có thể check-in lịch hẹn ở trạng thái PENDING hoặc CONFIRMED");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            appointment.setStatus(AppointmentStatus.CHECKED_IN);
            appointmentRepository.save(appointment);
            
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
            
            List<Appointment> allAppointments = appointmentRepository.findAll();
            List<Appointment> appointments = allAppointments;
            
            if (phone != null && !phone.trim().isEmpty()) {
                final String phoneFilter = phone.trim();
                appointments = allAppointments.stream()
                        .filter(apt -> {
                            if (apt.getBookedForUser() != null && apt.getBookedForUser().getPhoneNumber() != null) {
                                return apt.getBookedForUser().getPhoneNumber().contains(phoneFilter);
                            }
                            if (apt.getFamilyMember() != null && apt.getFamilyMember().getPhoneNumber() != null) {
                                return apt.getFamilyMember().getPhoneNumber().contains(phoneFilter);
                            }
                            if (apt.getConsultationPhone() != null) {
                                return apt.getConsultationPhone().contains(phoneFilter);
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
            }
            
            if (bookingCode != null && !bookingCode.trim().isEmpty()) {
                final String codeFilter = bookingCode.trim().toUpperCase();
                final List<Appointment> phoneFilteredAppointments = appointments;
                appointments = phoneFilteredAppointments.stream()
                        .filter(apt -> apt.getBookingCode() != null && apt.getBookingCode().toUpperCase().contains(codeFilter))
                        .collect(Collectors.toList());
            }
            
            // Lọc theo trung tâm (ngoại trừ ADMIN)
            Authentication authSearch = SecurityContextHolder.getContext().getAuthentication();
            User currentUserSearch = getCurrentUserFromAuth(authSearch);
            if (currentUserSearch != null && currentUserSearch.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                ut.edu.vaccinationmanagementsystem.entity.StaffInfo staffInfo = staffInfoRepository.findByUser(currentUserSearch).orElse(null);
                if (staffInfo != null && staffInfo.getCenter() != null) {
                    Long centerId = staffInfo.getCenter().getId();
                    appointments = appointments.stream()
                            .filter(apt -> apt.getCenter() != null && apt.getCenter().getId().equals(centerId))
                            .collect(Collectors.toList());
                }
            }
            
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
                
                // Patient info - ưu tiên: familyMember > bookedForUser > bookedByUser
                // (familyMember là người thân sẽ được tiêm, nên ưu tiên cao nhất)
                
                // Family member info (nếu có - đây là người sẽ được tiêm khi đặt cho người thân)
                if (apt.getFamilyMember() != null) {
                    Map<String, Object> member = new HashMap<>();
                    member.put("id", apt.getFamilyMember().getId());
                    member.put("fullName", apt.getFamilyMember().getFullName());
                    member.put("phoneNumber", apt.getFamilyMember().getPhoneNumber());
                    member.put("dateOfBirth", apt.getFamilyMember().getDateOfBirth());
                    member.put("gender", apt.getFamilyMember().getGender());
                    dto.put("familyMember", member);
                }
                
                // BookedForUser info (người được đặt lịch cho - nếu không phải family member)
                if (apt.getBookedForUser() != null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", apt.getBookedForUser().getId());
                    user.put("fullName", apt.getBookedForUser().getFullName());
                    user.put("email", apt.getBookedForUser().getEmail());
                    user.put("phoneNumber", apt.getBookedForUser().getPhoneNumber());
                    user.put("dayOfBirth", apt.getBookedForUser().getDayOfBirth());
                    user.put("gender", apt.getBookedForUser().getGender());
                    dto.put("bookedForUser", user);
                } else if (apt.getBookedByUser() != null && apt.getFamilyMember() == null) {
                    // Fallback: nếu bookedForUser null và không có familyMember, dùng bookedByUser
                    // (chỉ dùng khi đặt cho chính mình)
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", apt.getBookedByUser().getId());
                    user.put("fullName", apt.getBookedByUser().getFullName());
                    user.put("email", apt.getBookedByUser().getEmail());
                    user.put("phoneNumber", apt.getBookedByUser().getPhoneNumber());
                    user.put("dayOfBirth", apt.getBookedByUser().getDayOfBirth());
                    user.put("gender", apt.getBookedByUser().getGender());
                    dto.put("bookedForUser", user);
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
            }).collect(Collectors.toList());
            
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

    @PostMapping("/walk-in")
    public ResponseEntity<?> createWalkInAppointment(@RequestBody Map<String, Object> requestBody) {
        try {
            // Validate required fields
            String fullName = (String) requestBody.get("fullName");
            String phoneNumber = (String) requestBody.get("phoneNumber");
            String email = (String) requestBody.get("email");
            String dayOfBirthStr = (String) requestBody.get("dayOfBirth");
            String gender = (String) requestBody.get("gender");
            Object vaccineIdObj = requestBody.get("vaccineId");
            Object centerIdObj = requestBody.get("centerId");
            Object slotIdObj = requestBody.get("slotId");
            String appointmentDateStr = (String) requestBody.get("appointmentDate");
            String appointmentTimeStr = (String) requestBody.get("appointmentTime");
            Object doseNumberObj = requestBody.get("doseNumber");
            String paymentMethod = (String) requestBody.get("paymentMethod");
            String notes = (String) requestBody.get("notes");
            
            if (fullName == null || fullName.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Họ tên là bắt buộc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Số điện thoại là bắt buộc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (vaccineIdObj == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Vaccine ID là bắt buộc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (centerIdObj == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Center ID là bắt buộc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (slotIdObj == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Slot ID là bắt buộc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (appointmentDateStr == null || appointmentDateStr.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ngày hẹn là bắt buộc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (appointmentTimeStr == null || appointmentTimeStr.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Giờ hẹn là bắt buộc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Parse values
            Long vaccineId = null;
            Long centerId = null;
            Long slotId = null;
            Integer doseNumber = 1;
            
            try {
                if (vaccineIdObj instanceof Number) {
                    vaccineId = ((Number) vaccineIdObj).longValue();
                } else if (vaccineIdObj instanceof String) {
                    vaccineId = Long.parseLong((String) vaccineIdObj);
                }
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Vaccine ID không hợp lệ");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            try {
                if (centerIdObj instanceof Number) {
                    centerId = ((Number) centerIdObj).longValue();
                } else if (centerIdObj instanceof String) {
                    centerId = Long.parseLong((String) centerIdObj);
                }
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Center ID không hợp lệ");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            try {
                if (slotIdObj instanceof Number) {
                    slotId = ((Number) slotIdObj).longValue();
                } else if (slotIdObj instanceof String) {
                    slotId = Long.parseLong((String) slotIdObj);
                }
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Slot ID không hợp lệ");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (doseNumberObj != null) {
                try {
                    if (doseNumberObj instanceof Number) {
                        doseNumber = ((Number) doseNumberObj).intValue();
                    } else if (doseNumberObj instanceof String) {
                        doseNumber = Integer.parseInt((String) doseNumberObj);
                    }
                } catch (Exception e) {
                    doseNumber = 1; // Default to 1
                }
            }
            
            // Parse dates
            java.time.LocalDate dayOfBirth = null;
            if (dayOfBirthStr != null && !dayOfBirthStr.trim().isEmpty()) {
                try {
                    dayOfBirth = java.time.LocalDate.parse(dayOfBirthStr);
                } catch (Exception e) {
                    // Invalid date format, will be null
                }
            }
            
            java.time.LocalDate appointmentDate = null;
            try {
                appointmentDate = java.time.LocalDate.parse(appointmentDateStr);
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Ngày hẹn không hợp lệ. Sử dụng định dạng YYYY-MM-DD");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            java.time.LocalTime appointmentTime = null;
            try {
                // Try parsing with different formats
                if (appointmentTimeStr.length() == 5) {
                    // Format: HH:mm
                    appointmentTime = java.time.LocalTime.parse(appointmentTimeStr, java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                } else {
                    // Format: HH:mm:ss or HH:mm:ss.SSS
                    appointmentTime = java.time.LocalTime.parse(appointmentTimeStr);
                }
            } catch (Exception e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Giờ hẹn không hợp lệ. Sử dụng định dạng HH:mm hoặc HH:mm:ss");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Create walk-in appointment
            Appointment appointment = appointmentService.createWalkInAppointment(
                    fullName,
                    phoneNumber,
                    email,
                    dayOfBirth,
                    gender,
                    vaccineId,
                    centerId,
                    slotId,
                    appointmentDate,
                    appointmentTime,
                    doseNumber,
                    paymentMethod,
                    notes
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký walk-in thành công");
            response.put("bookingCode", appointment.getBookingCode());
            response.put("appointmentId", appointment.getId());
            response.put("appointmentDate", appointment.getAppointmentDate());
            response.put("appointmentTime", appointment.getAppointmentTime());
            response.put("centerName", appointment.getCenter().getName());
            response.put("vaccineName", appointment.getVaccine().getName());
            
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
            
            // Patient info - ưu tiên: familyMember > bookedForUser > bookedByUser > guest
            // (familyMember là người thân sẽ được tiêm, nên ưu tiên cao nhất)
            if (appointment.getFamilyMember() != null) {
                Map<String, Object> patient = new HashMap<>();
                patient.put("id", appointment.getFamilyMember().getId());
                patient.put("fullName", appointment.getFamilyMember().getFullName());
                patient.put("phoneNumber", appointment.getFamilyMember().getPhoneNumber());
                patient.put("dayOfBirth", appointment.getFamilyMember().getDateOfBirth());
                patient.put("gender", appointment.getFamilyMember().getGender());
                response.put("patient", patient);
                response.put("patientType", "FAMILY_MEMBER");
            } else if (appointment.getBookedForUser() != null) {
                Map<String, Object> patient = new HashMap<>();
                patient.put("id", appointment.getBookedForUser().getId());
                patient.put("fullName", appointment.getBookedForUser().getFullName());
                patient.put("email", appointment.getBookedForUser().getEmail());
                patient.put("phoneNumber", appointment.getBookedForUser().getPhoneNumber());
                patient.put("dayOfBirth", appointment.getBookedForUser().getDayOfBirth());
                patient.put("gender", appointment.getBookedForUser().getGender());
                response.put("patient", patient);
                response.put("patientType", "USER");
            } else if (appointment.getBookedByUser() != null) {
                // Fallback: nếu bookedForUser null và không có familyMember, dùng bookedByUser
                // (chỉ dùng khi đặt cho chính mình)
                Map<String, Object> patient = new HashMap<>();
                patient.put("id", appointment.getBookedByUser().getId());
                patient.put("fullName", appointment.getBookedByUser().getFullName());
                patient.put("email", appointment.getBookedByUser().getEmail());
                patient.put("phoneNumber", appointment.getBookedByUser().getPhoneNumber());
                patient.put("dayOfBirth", appointment.getBookedByUser().getDayOfBirth());
                patient.put("gender", appointment.getBookedByUser().getGender());
                response.put("patient", patient);
                response.put("patientType", "USER");
            } else {
                // Guest info
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
                payment.put("paymentMethod", appointment.getPayment().getPaymentMethod() != null ? appointment.getPayment().getPaymentMethod().name() : null);
                payment.put("paymentStatus", appointment.getPayment().getPaymentStatus() != null ? appointment.getPayment().getPaymentStatus().name() : null);
                payment.put("invoiceNumber", appointment.getPayment().getInvoiceNumber());
                payment.put("transactionId", appointment.getPayment().getTransactionId());
                payment.put("paidAt", appointment.getPayment().getPaidAt());
                response.put("payment", payment);
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private User getCurrentUserFromAuth(Authentication authentication) {
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
}


