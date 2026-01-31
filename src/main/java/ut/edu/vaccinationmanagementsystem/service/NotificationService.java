package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.Notification;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.NotificationStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.NotificationType;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;
import ut.edu.vaccinationmanagementsystem.repository.NotificationRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${spring.mail.username:noreply@tiemchung.gov.vn}")
    private String fromEmail;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Tạo thông báo nhắc nhở lịch hẹn sắp tới
     */
    public void createAppointmentReminderNotification(Appointment appointment, int daysBefore, int hoursBefore, int minutesBefore) {
        createAppointmentReminderNotification(appointment, daysBefore, hoursBefore, minutesBefore, false);
    }
    
    /**
     * Tạo thông báo nhắc nhở lịch hẹn sắp tới
     * @param isAfterBooking true nếu là nhắc nhở sau khi đặt lịch, false nếu là nhắc nhở trước giờ hẹn
     */
    public void createAppointmentReminderNotification(Appointment appointment, int daysBefore, int hoursBefore, int minutesBefore, boolean isAfterBooking) {
        User user = getNotificationUser(appointment);
        if (user == null) {
            return; // Không có user để gửi thông báo
        }
        
        String reminderText = "";
        String title = "";
        
        if (isAfterBooking) {
            // Nhắc nhở sau khi đặt lịch
            title = "Nhắc nhở lịch hẹn tiêm chủng";
            reminderText = "Bạn đã đặt lịch hẹn thành công";
        } else {
            // Nhắc nhở trước giờ hẹn
            if (daysBefore > 0) {
                reminderText = daysBefore + " ngày";
            } else if (hoursBefore > 0) {
                reminderText = hoursBefore + " giờ";
            } else if (minutesBefore > 0) {
                reminderText = minutesBefore + " phút";
            }
            title = "Nhắc nhở lịch hẹn tiêm chủng - " + reminderText + " nữa";
        }
        
        String content = buildAppointmentReminderContent(appointment, reminderText, isAfterBooking);
        
        // Tạo IN_APP notification (hiển thị trong ứng dụng)
        createInAppNotification(user, appointment, title, content);
        
        // Gửi EMAIL notification (chỉ gửi email, không hiển thị trong danh sách thông báo)
        try {
            sendEmail(user.getEmail(), title, content);
        } catch (Exception e) {
            System.err.println("Failed to send reminder email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tạo thông báo khi đặt lịch thành công
     */
    public void createAppointmentCreatedNotification(Appointment appointment) {
        User user = getNotificationUser(appointment);
        if (user == null) {
            return;
        }
        
        String title = "Đặt lịch hẹn thành công";
        String content = buildAppointmentCreatedContent(appointment);
        
        // Tạo IN_APP notification (hiển thị trong ứng dụng)
        createInAppNotification(user, appointment, title, content);
        
        // Gửi EMAIL notification (chỉ gửi email, không hiển thị trong danh sách thông báo)
        try {
            sendEmail(user.getEmail(), title, content);
        } catch (Exception e) {
            System.err.println("Failed to send appointment created email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tạo thông báo khi lịch hẹn bị hủy
     */
    public void createAppointmentCancelledNotification(Appointment appointment) {
        User user = getNotificationUser(appointment);
        if (user == null) {
            return;
        }
        
        String title = "Lịch hẹn đã được hủy";
        String content = buildAppointmentCancelledContent(appointment);
        
        // Tạo IN_APP notification (hiển thị trong ứng dụng)
        createInAppNotification(user, appointment, title, content);
        
        // Gửi EMAIL notification (chỉ gửi email, không hiển thị trong danh sách thông báo)
        try {
            sendEmail(user.getEmail(), title, content);
        } catch (Exception e) {
            System.err.println("Failed to send appointment cancelled email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tạo IN_APP notification
     */
    private void createInAppNotification(User user, Appointment appointment, String title, String content) {
        // Kiểm tra xem đã có thông báo với cùng title và appointment chưa để tránh trùng lặp
        // (Cho phép nhiều loại notification khác nhau cho cùng appointment, nhưng không cho phép trùng title)
        Optional<Notification> existing = notificationRepository.findByAppointmentIdAndTypeAndTitle(
            appointment.getId(), NotificationType.IN_APP, title);
        
        if (existing.isPresent()) {
            return; // Đã có thông báo này rồi
        }
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setAppointment(appointment);
        notification.setType(NotificationType.IN_APP);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus(NotificationStatus.SENT); // IN_APP được gửi ngay
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSentAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }
    
    /**
     * Tạo và gửi EMAIL notification
     */
    private void createAndSendEmailNotification(User user, Appointment appointment, String title, String content) {
        // Kiểm tra xem đã có thông báo này chưa để tránh trùng lặp
        Optional<Notification> existing = notificationRepository.findByAppointmentIdAndType(
            appointment.getId(), NotificationType.EMAIL);
        
        if (existing.isPresent()) {
            return; // Đã có thông báo này rồi
        }
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setAppointment(appointment);
        notification.setType(NotificationType.EMAIL);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        try {
            // Gửi email
            sendEmail(user.getEmail(), title, content);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            System.err.println("Failed to send notification email: " + e.getMessage());
            e.printStackTrace();
        }
        
        notificationRepository.save(notification);
    }
    
    /**
     * Gửi email
     */
    private void sendEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Lấy user để gửi thông báo (bookedForUser hoặc bookedByUser)
     */
    private User getNotificationUser(Appointment appointment) {
        if (appointment.getBookedForUser() != null) {
            return appointment.getBookedForUser();
        }
        return appointment.getBookedByUser();
    }
    
    /**
     * Build nội dung thông báo nhắc nhở
     */
    private String buildAppointmentReminderContent(Appointment appointment, String reminderText) {
        return buildAppointmentReminderContent(appointment, reminderText, false);
    }
    
    /**
     * Build nội dung thông báo nhắc nhở
     */
    private String buildAppointmentReminderContent(Appointment appointment, String reminderText, boolean isAfterBooking) {
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào,\n\n");
        
        if (isAfterBooking) {
            sb.append("Bạn đã đặt lịch hẹn tiêm chủng thành công. Thông tin lịch hẹn:\n\n");
        } else {
            sb.append("Bạn có lịch hẹn tiêm chủng sau ").append(reminderText).append(":\n\n");
        }
        
        if (appointment.getVaccine() != null) {
            sb.append("Vaccine: ").append(appointment.getVaccine().getName()).append("\n");
        }
        sb.append("Mũi tiêm: ").append(appointment.getDoseNumber()).append("\n");
        
        if (appointment.getAppointmentDate() != null) {
            sb.append("Ngày: ").append(appointment.getAppointmentDate().format(DATE_FORMATTER)).append("\n");
        }
        if (appointment.getAppointmentTime() != null) {
            sb.append("Giờ: ").append(appointment.getAppointmentTime().format(TIME_FORMATTER)).append("\n");
        }
        if (appointment.getCenter() != null) {
            sb.append("Trung tâm: ").append(appointment.getCenter().getName()).append("\n");
            if (appointment.getCenter().getAddress() != null) {
                sb.append("Địa chỉ: ").append(appointment.getCenter().getAddress()).append("\n");
            }
        }
        if (appointment.getRoom() != null) {
            sb.append("Phòng: ").append(appointment.getRoom().getRoomNumber()).append("\n");
        }
        sb.append("Mã booking: ").append(appointment.getBookingCode()).append("\n\n");
        
        sb.append("Vui lòng đến đúng giờ và mang theo CMND/CCCD.\n\n");
        sb.append("Trân trọng,\n");
        sb.append("Hệ thống Tiêm chủng QG");
        
        return sb.toString();
    }
    
    /**
     * Build nội dung thông báo đặt lịch thành công
     */
    private String buildAppointmentCreatedContent(Appointment appointment) {
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào,\n\n");
        sb.append("Bạn đã đặt lịch hẹn tiêm chủng thành công!\n\n");
        
        if (appointment.getVaccine() != null) {
            sb.append("Vaccine: ").append(appointment.getVaccine().getName()).append("\n");
        }
        sb.append("Mũi tiêm: ").append(appointment.getDoseNumber()).append("\n");
        
        if (appointment.getAppointmentDate() != null) {
            sb.append("Ngày: ").append(appointment.getAppointmentDate().format(DATE_FORMATTER)).append("\n");
        }
        if (appointment.getAppointmentTime() != null) {
            sb.append("Giờ: ").append(appointment.getAppointmentTime().format(TIME_FORMATTER)).append("\n");
        }
        if (appointment.getCenter() != null) {
            sb.append("Trung tâm: ").append(appointment.getCenter().getName()).append("\n");
            if (appointment.getCenter().getAddress() != null) {
                sb.append("Địa chỉ: ").append(appointment.getCenter().getAddress()).append("\n");
            }
        }
        if (appointment.getRoom() != null) {
            sb.append("Phòng: ").append(appointment.getRoom().getRoomNumber()).append("\n");
        }
        sb.append("Mã booking: ").append(appointment.getBookingCode()).append("\n\n");
        
        sb.append("Bạn sẽ nhận được thông báo nhắc nhở trước lịch hẹn.\n\n");
        sb.append("Trân trọng,\n");
        sb.append("Hệ thống Tiêm chủng VacciCare");
        
        return sb.toString();
    }
    
    /**
     * Build nội dung thông báo hủy lịch
     */
    private String buildAppointmentCancelledContent(Appointment appointment) {
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào,\n\n");
        sb.append("Lịch hẹn tiêm chủng của bạn đã được hủy.\n\n");
        
        sb.append("Mã booking: ").append(appointment.getBookingCode()).append("\n");
        if (appointment.getVaccine() != null) {
            sb.append("Vaccine: ").append(appointment.getVaccine().getName()).append("\n");
        }
        if (appointment.getAppointmentDate() != null) {
            sb.append("Ngày đã đặt: ").append(appointment.getAppointmentDate().format(DATE_FORMATTER)).append("\n");
        }
        if (appointment.getRoom() != null) {
            sb.append("Phòng đã đặt: ").append(appointment.getRoom().getRoomNumber()).append("\n");
        }
        if (appointment.getCancellationReason() != null && !appointment.getCancellationReason().isEmpty()) {
            sb.append("Lý do: ").append(appointment.getCancellationReason()).append("\n");
        }
        sb.append("\n");
        
        sb.append("Nếu bạn muốn đặt lịch mới, vui lòng truy cập hệ thống.\n\n");
        sb.append("Trân trọng,\n");
        sb.append("Hệ thống Tiêm chủng VacciCare");
        
        return sb.toString();
    }
    
    /**
     * Lấy danh sách thông báo của user
     */
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Lấy số lượng thông báo chưa đọc
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    /**
     * Đánh dấu thông báo đã đọc
     */
    public void markAsRead(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            if (notification.getUser().getId().equals(userId)) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        }
    }
    
    /**
     * Xóa thông báo
     */
    public void deleteNotification(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            if (notification.getUser().getId().equals(userId)) {
                notificationRepository.delete(notification);
            }
        }
    }
    
    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * Tạo thông báo mới
     */
    public Notification createNotification(Long userId, Long appointmentId, NotificationType type, 
                                          String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        if (appointmentId != null) {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElse(null);
            notification.setAppointment(appointment);
        }
        
        // Nếu là EMAIL type, gửi email ngay
        if (type == NotificationType.EMAIL) {
            try {
                sendEmail(user.getEmail(), title, content);
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
                System.err.println("Failed to send notification email: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // IN_APP được gửi ngay
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        }
        
        return notificationRepository.save(notification);
    }
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Gửi thông báo nhắc lịch cho tất cả appointments sắp tới
     * Có thể gọi từ cron job hoặc manual
     */
    public int sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> appointments = appointmentRepository.findAll();
        int sentCount = 0;
        
        for (Appointment appointment : appointments) {
            // Chỉ xử lý appointments có status PENDING hoặc CONFIRMED
            if (appointment.getStatus() != AppointmentStatus.PENDING && 
                appointment.getStatus() != AppointmentStatus.CONFIRMED) {
                continue;
            }
            
            // Kiểm tra nếu appointment có đầy đủ thông tin
            if (appointment.getAppointmentDate() == null || 
                appointment.getAppointmentTime() == null) {
                continue;
            }
            
            // Tính toán thời gian appointment
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
            );
            
            // Tính số giờ còn lại
            long hoursUntil = java.time.Duration.between(now, appointmentDateTime).toHours();
            
            // Tạo thông báo nhắc nhở 1 ngày trước (24-25 giờ)
            if (hoursUntil >= 24 && hoursUntil <= 25) {
                createAppointmentReminderNotification(appointment, 1, 0, 0);
                sentCount++;
            }
            
            // Tạo thông báo nhắc nhở 2 giờ trước (2-3 giờ)
            if (hoursUntil >= 2 && hoursUntil <= 3) {
                createAppointmentReminderNotification(appointment, 0, 2, 0);
                sentCount++;
            }
        }
        
        return sentCount;
    }
}

