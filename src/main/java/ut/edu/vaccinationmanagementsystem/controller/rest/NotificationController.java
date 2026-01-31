package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.Notification;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.NotificationStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.NotificationType;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;
import ut.edu.vaccinationmanagementsystem.repository.NotificationRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Lấy danh sách thông báo của user hiện tại
     */
    @GetMapping
    public ResponseEntity<?> getUserNotifications() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            List<Notification> notifications = notificationService.getUserNotifications(currentUser.getId());
            
            // Convert to DTO để tránh circular reference và đảm bảo hiển thị đầy đủ
            List<Map<String, Object>> notificationDTOs = notifications.stream().map(notification -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", notification.getId());
                dto.put("type", notification.getType().toString());
                dto.put("title", notification.getTitle());
                dto.put("content", notification.getContent());
                dto.put("status", notification.getStatus().toString());
                dto.put("isRead", notification.getIsRead());
                dto.put("createdAt", notification.getCreatedAt());
                dto.put("sentAt", notification.getSentAt());
                
                // Thêm thông tin appointment nếu có
                if (notification.getAppointment() != null) {
                    Map<String, Object> appointmentInfo = new HashMap<>();
                    appointmentInfo.put("id", notification.getAppointment().getId());
                    appointmentInfo.put("bookingCode", notification.getAppointment().getBookingCode());
                    if (notification.getAppointment().getVaccine() != null) {
                        appointmentInfo.put("vaccineName", notification.getAppointment().getVaccine().getName());
                    }
                    if (notification.getAppointment().getCenter() != null) {
                        appointmentInfo.put("centerName", notification.getAppointment().getCenter().getName());
                    }
                    appointmentInfo.put("appointmentDate", notification.getAppointment().getAppointmentDate());
                    appointmentInfo.put("appointmentTime", notification.getAppointment().getAppointmentTime());
                    dto.put("appointment", appointmentInfo);
                } else {
                    dto.put("appointment", null);
                }
                
                return dto;
            }).toList();
            
            return ResponseEntity.ok(notificationDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy số lượng thông báo chưa đọc
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            long count = notificationService.getUnreadCount(currentUser.getId());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Đánh dấu thông báo đã đọc
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            notificationService.markAsRead(id, currentUser.getId());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            notificationService.markAllAsRead(currentUser.getId());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Xóa thông báo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            notificationService.deleteNotification(id, currentUser.getId());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * TEST ENDPOINT: Tạo thông báo nhắc nhở cho appointment sắp tới của user hiện tại
     * Dùng để test nhanh mà không cần chờ scheduled job
     */
    @PostMapping("/test/create-reminders")
    public ResponseEntity<?> testCreateReminders() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            // Tìm appointments sắp tới của user (PENDING hoặc CONFIRMED)
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(apt -> {
                    // Kiểm tra appointment thuộc về user hiện tại
                    boolean belongsToUser = (apt.getBookedForUser() != null && apt.getBookedForUser().getId().equals(currentUser.getId())) ||
                                          (apt.getBookedForUser() == null && apt.getBookedByUser() != null && apt.getBookedByUser().getId().equals(currentUser.getId()));
                    
                    // Kiểm tra status
                    boolean validStatus = apt.getStatus() == AppointmentStatus.PENDING || apt.getStatus() == AppointmentStatus.CONFIRMED;
                    
                    // Kiểm tra có đầy đủ thông tin
                    boolean hasDateAndTime = apt.getAppointmentDate() != null && apt.getAppointmentTime() != null;
                    
                    return belongsToUser && validStatus && hasDateAndTime;
                })
                .toList();
            
            int createdCount = 0;
            for (Appointment appointment : appointments) {
                // Tạo thông báo nhắc nhở 1 ngày trước
                notificationService.createAppointmentReminderNotification(appointment, 1, 0, 0);
                createdCount++;
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã tạo thông báo nhắc nhở cho " + createdCount + " lịch hẹn",
                "appointmentsChecked", appointments.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * TEST ENDPOINT: Tạo thông báo test đơn giản
     */
    @PostMapping("/test/create-test-notification")
    public ResponseEntity<?> testCreateTestNotification() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            // Tạo một thông báo test
            Notification testNotification = new Notification();
            testNotification.setUser(currentUser);
            testNotification.setType(NotificationType.IN_APP);
            testNotification.setTitle("Thông báo test");
            testNotification.setContent("Đây là thông báo test để kiểm tra chức năng thông báo.\n\nBạn có thể xóa thông báo này sau khi test xong.");
            testNotification.setStatus(NotificationStatus.SENT);
            testNotification.setIsRead(false);
            testNotification.setCreatedAt(java.time.LocalDateTime.now());
            testNotification.setSentAt(java.time.LocalDateTime.now());
            
            notificationRepository.save(testNotification);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã tạo thông báo test thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy user hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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

