package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.NotificationDTO;
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
     * POST /api/notifications
     * Tạo thông báo mới
     */
    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody NotificationDTO dto) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra quyền: chỉ ADMIN mới có thể tạo thông báo
            if (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only ADMIN can create notifications");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate required fields
            if (dto.getUserId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (dto.getType() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Notification type is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Title is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Content is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Notification notification = notificationService.createNotification(
                    dto.getUserId(),
                    dto.getAppointmentId(),
                    dto.getType(),
                    dto.getTitle().trim(),
                    dto.getContent().trim()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", notification.getId());
            result.put("userId", notification.getUser().getId());
            result.put("type", notification.getType().name());
            result.put("title", notification.getTitle());
            result.put("content", notification.getContent());
            result.put("status", notification.getStatus().name());
            result.put("isRead", notification.getIsRead());
            result.put("createdAt", notification.getCreatedAt());
            result.put("sentAt", notification.getSentAt());
            result.put("message", "Notification created successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/notifications/user/{userId}
     * Xem thông báo của user cụ thể
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserNotificationsByUserId(@PathVariable Long userId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra quyền: chỉ user đó hoặc ADMIN mới xem được
            if (!currentUser.getId().equals(userId) && 
                currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You don't have permission to view this user's notifications");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            
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
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Lấy danh sách thông báo của user hiện tại
     * Query params:
     * - filter: "all" (mặc định), "unread", "appointment", "system"
     */
    @GetMapping
    public ResponseEntity<?> getUserNotifications(@RequestParam(required = false, defaultValue = "all") String filter) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            List<Notification> notifications;
            
            // Lọc theo filter
            switch (filter.toLowerCase()) {
                case "unread":
                    notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId());
                    break;
                case "appointment":
                    // Lấy tất cả thông báo có appointment
                    notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                        .stream()
                        .filter(n -> n.getAppointment() != null)
                        .collect(java.util.stream.Collectors.toList());
                    break;
                case "system":
                    // Lấy thông báo không có appointment (thông báo hệ thống)
                    notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                        .stream()
                        .filter(n -> n.getAppointment() == null)
                        .collect(java.util.stream.Collectors.toList());
                    break;
                case "all":
                default:
                    notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
                    break;
            }
            
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
    
    /**
     * POST /api/notifications/send-appointment-reminder
     * Gửi nhắc lịch cho tất cả appointments sắp tới (Cron job)
     */
    @PostMapping("/send-appointment-reminder")
    public ResponseEntity<?> sendAppointmentReminder() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra quyền: chỉ ADMIN mới có thể gửi nhắc lịch manual
            if (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only ADMIN can send appointment reminders");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            int sentCount = notificationService.sendAppointmentReminders();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Sent " + sentCount + " appointment reminder notifications");
            result.put("sentCount", sentCount);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/notifications/admin/users/search
     * Tìm kiếm users (cho trang cá nhân cụ thể)
     */
    @GetMapping("/admin/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam(required = false) String q) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            List<User> users;
            if (q != null && !q.trim().isEmpty()) {
                String searchLower = q.toLowerCase();
                users = userRepository.findAll().stream()
                    .filter(u -> (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchLower)) ||
                               (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchLower)) ||
                               (u.getPhoneNumber() != null && u.getPhoneNumber().contains(q)))
                    .limit(20)
                    .collect(java.util.stream.Collectors.toList());
            } else {
                users = userRepository.findAll().stream().limit(20).collect(java.util.stream.Collectors.toList());
            }
            
            List<Map<String, Object>> result = users.stream().map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("fullName", user.getFullName());
                map.put("email", user.getEmail());
                map.put("phoneNumber", user.getPhoneNumber());
                map.put("role", user.getRole() != null ? user.getRole().name() : null);
                return map;
            }).collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/notifications/admin/users/count
     * Lấy tổng số users
     */
    @GetMapping("/admin/users/count")
    public ResponseEntity<?> getUsersCount() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            long totalCount = userRepository.count();
            Map<String, Object> result = new HashMap<>();
            result.put("total", totalCount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/notifications/admin/users/count-by-role
     * Lấy số lượng users theo role
     */
    @GetMapping("/admin/users/count-by-role")
    public ResponseEntity<?> getUsersCountByRole() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Map<String, Long> counts = new HashMap<>();
            for (ut.edu.vaccinationmanagementsystem.entity.enums.Role role : ut.edu.vaccinationmanagementsystem.entity.enums.Role.values()) {
                long count = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == role)
                    .count();
                counts.put(role.name(), count);
            }
            
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/notifications/admin/bulk
     * Gửi thông báo cho nhiều users
     */
    @PostMapping("/admin/bulk")
    public ResponseEntity<?> sendBulkNotifications(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            @SuppressWarnings("unchecked")
            List<?> userIdsRaw = (List<?>) request.get("userIds");
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String typeStr = (String) request.get("type");
            Boolean sendSms = (Boolean) request.getOrDefault("sendSms", false);
            Boolean sendApp = (Boolean) request.getOrDefault("sendApp", true);
            
            if (userIdsRaw == null || userIdsRaw.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User IDs are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Convert to List<Long> handling both Integer and Long
            List<Long> userIds = userIdsRaw.stream()
                .map(id -> {
                    if (id instanceof Long) {
                        return (Long) id;
                    } else if (id instanceof Integer) {
                        return ((Integer) id).longValue();
                    } else if (id instanceof Number) {
                        return ((Number) id).longValue();
                    } else {
                        return Long.parseLong(id.toString());
                    }
                })
                .collect(java.util.stream.Collectors.toList());
            if (title == null || title.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Title is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (content == null || content.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Content is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            NotificationType type = NotificationType.IN_APP;
            if (typeStr != null) {
                try {
                    type = NotificationType.valueOf(typeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Use default
                }
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (Long userId : userIds) {
                try {
                    if (sendApp) {
                        notificationService.createNotification(userId, null, NotificationType.IN_APP, title, content);
                    }
                    if (sendSms && type == NotificationType.SMS) {
                        notificationService.createNotification(userId, null, NotificationType.SMS, title, content);
                    }
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    System.err.println("Failed to send notification to user " + userId + ": " + e.getMessage());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Sent " + successCount + " notifications successfully");
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/notifications/admin/send-by-role
     * Gửi thông báo theo role
     */
    @PostMapping("/admin/send-by-role")
    public ResponseEntity<?> sendNotificationsByRole(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) request.get("roles");
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            Boolean sendSms = (Boolean) request.getOrDefault("sendSms", false);
            Boolean sendApp = (Boolean) request.getOrDefault("sendApp", true);
            
            if (roles == null || roles.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Roles are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (title == null || title.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Title is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (content == null || content.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Content is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<ut.edu.vaccinationmanagementsystem.entity.enums.Role> roleEnums = new java.util.ArrayList<>();
            for (String roleStr : roles) {
                try {
                    roleEnums.add(ut.edu.vaccinationmanagementsystem.entity.enums.Role.valueOf(roleStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Skip invalid role
                }
            }
            
            List<User> users = userRepository.findAll().stream()
                .filter(u -> roleEnums.contains(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
            
            int successCount = 0;
            int failCount = 0;
            
            for (User user : users) {
                try {
                    if (sendApp) {
                        notificationService.createNotification(user.getId(), null, NotificationType.IN_APP, title, content);
                    }
                    if (sendSms) {
                        notificationService.createNotification(user.getId(), null, NotificationType.SMS, title, content);
                    }
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    System.err.println("Failed to send notification to user " + user.getId() + ": " + e.getMessage());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Sent " + successCount + " notifications to " + users.size() + " users");
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalUsers", users.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/notifications/admin/send-all
     * Gửi thông báo cho tất cả users
     */
    @PostMapping("/admin/send-all")
    public ResponseEntity<?> sendNotificationsToAll(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            Boolean sendSms = (Boolean) request.getOrDefault("sendSms", false);
            Boolean sendApp = (Boolean) request.getOrDefault("sendApp", true);
            
            if (title == null || title.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Title is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (content == null || content.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Content is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<User> users = userRepository.findAll();
            
            int successCount = 0;
            int failCount = 0;
            
            for (User user : users) {
                try {
                    if (sendApp) {
                        notificationService.createNotification(user.getId(), null, NotificationType.IN_APP, title, content);
                    }
                    if (sendSms) {
                        notificationService.createNotification(user.getId(), null, NotificationType.SMS, title, content);
                    }
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    System.err.println("Failed to send notification to user " + user.getId() + ": " + e.getMessage());
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Sent " + successCount + " notifications to " + users.size() + " users");
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalUsers", users.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

