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
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.AppointmentService;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
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
}


