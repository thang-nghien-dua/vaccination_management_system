package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.Notification;
import ut.edu.vaccinationmanagementsystem.entity.enums.NotificationType;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Tìm tất cả thông báo của một user, sắp xếp theo thời gian tạo giảm dần
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Tìm tất cả thông báo chưa đọc của một user
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    /**
     * Đếm số lượng thông báo chưa đọc của một user
     */
    long countByUserIdAndIsReadFalse(Long userId);
    
    /**
     * Tìm thông báo theo appointment và type để tránh trùng lặp
     */
    Optional<Notification> findByAppointmentIdAndType(Long appointmentId, NotificationType type);
    
    /**
     * Tìm thông báo theo appointment, type và title để tránh trùng lặp chính xác hơn
     */
    Optional<Notification> findByAppointmentIdAndTypeAndTitle(Long appointmentId, NotificationType type, String title);
    
    /**
     * Tìm tất cả thông báo của một user theo type
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type);
}

