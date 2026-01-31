package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.AppointmentHistory;

import java.util.List;

@Repository
public interface AppointmentHistoryRepository extends JpaRepository<AppointmentHistory, Long> {
    /**
     * Tìm tất cả lịch sử thay đổi của một appointment
     */
    List<AppointmentHistory> findByAppointmentIdOrderByChangedAtDesc(Long appointmentId);
}

