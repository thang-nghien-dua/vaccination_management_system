package ut.edu.vaccinationmanagement_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ut.edu.vaccinationmanagement_system.entity.AppointmentSlot;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentSlotRepository
        extends JpaRepository<AppointmentSlot, Long> {

    // Xem slot theo trung tâm + ngày
    List<AppointmentSlot> findByCenter_IdAndDate(
            Long centerId, LocalDate date);

    // Slot còn trống theo ngày
    @Query("""
        SELECT s FROM AppointmentSlot s
        WHERE s.center.id = :centerId
        AND s.date BETWEEN :startDate AND :endDate
        AND s.currentBookings < s.maxCapacity
    """)
    List<AppointmentSlot> findAvailableSlots(
            Long centerId,
            LocalDate startDate,
            LocalDate endDate
    );
}
