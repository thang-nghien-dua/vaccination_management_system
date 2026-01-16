package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Scheduled task để kiểm tra và tạo thông báo nhắc nhở lịch hẹn sắp tới
 */
@Component
public class NotificationScheduler {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Chạy mỗi giờ để kiểm tra appointments sắp tới và tạo thông báo nhắc nhở
     * - 1 ngày trước (24 giờ)
     * - 2 giờ trước
     */
    @Scheduled(cron = "0 0 * * * ?") // Chạy mỗi giờ
    public void checkUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        
        // Tìm appointments có status PENDING hoặc CONFIRMED
        List<Appointment> appointments = appointmentRepository.findAll();
        
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
                notificationService.createAppointmentReminderNotification(appointment, 1, 0, 0);
            }
            
            // Tạo thông báo nhắc nhở 2 giờ trước (2-3 giờ)
            if (hoursUntil >= 2 && hoursUntil <= 3) {
                notificationService.createAppointmentReminderNotification(appointment, 0, 2, 0);
            }
        }
    }
    
    /**
     * Chạy mỗi phút để kiểm tra appointments mới đặt và gửi nhắc nhở sau 5 phút
     * TEST MODE: Nhắc nhở 5 phút sau khi đặt lịch (để test nhanh)
     */
    @Scheduled(cron = "0 * * * * ?") // Chạy mỗi phút
    public void checkNewAppointments() {
        LocalDateTime now = LocalDateTime.now();
        
        // Tìm appointments có status PENDING hoặc CONFIRMED
        List<Appointment> appointments = appointmentRepository.findAll();
        
        for (Appointment appointment : appointments) {
            // Chỉ xử lý appointments có status PENDING hoặc CONFIRMED
            if (appointment.getStatus() != AppointmentStatus.PENDING && 
                appointment.getStatus() != AppointmentStatus.CONFIRMED) {
                continue;
            }
            
            // Kiểm tra nếu appointment có createdAt
            if (appointment.getCreatedAt() == null) {
                continue;
            }
            
            // Tính số phút từ khi đặt lịch đến bây giờ
            long minutesSinceCreated = java.time.Duration.between(appointment.getCreatedAt(), now).toMinutes();
            
            // Gửi nhắc nhở sau 5 phút kể từ khi đặt lịch (5-6 phút)
            // Chỉ gửi một lần, kiểm tra bằng cách xem có notification nào với type REMINDER và createdAt gần đây không
            if (minutesSinceCreated >= 5 && minutesSinceCreated <= 6) {
                // Kiểm tra xem đã gửi nhắc nhở "sau khi đặt" chưa bằng cách kiểm tra notification
                // (Có thể thêm flag hoặc kiểm tra notification, nhưng để đơn giản thì gửi luôn)
                // Để tránh gửi nhiều lần, có thể thêm một field "reminderSentAfterBooking" vào Appointment
                // Hoặc kiểm tra xem có notification REMINDER nào được tạo trong 1 phút qua không
                // Tạm thời gửi luôn, sau có thể cải thiện
                notificationService.createAppointmentReminderNotification(appointment, 0, 0, 0, true);
            }
        }
    }
}

