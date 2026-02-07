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

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    Optional<Notification> findByAppointmentIdAndType(Long appointmentId, NotificationType type);

    Optional<Notification> findByAppointmentIdAndTypeAndTitle(Long appointmentId, NotificationType type, String title);

    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type);
}

