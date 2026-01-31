package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.AppointmentSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {
    
    // Tìm tất cả slot theo center ID
    List<AppointmentSlot> findByCenterId(Long centerId);
    
    // Tìm slot theo center ID và ngày
    List<AppointmentSlot> findByCenterIdAndDate(Long centerId, LocalDate date);
    
    // Tìm slot trống (isAvailable = true và currentBookings < maxCapacity)
    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "s.isAvailable = true AND " +
           "s.currentBookings < s.maxCapacity")
    List<AppointmentSlot> findAvailableSlots();
    
    // Tìm slot trống theo center ID
    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "s.center.id = :centerId AND " +
           "s.isAvailable = true AND " +
           "s.currentBookings < s.maxCapacity " +
           "ORDER BY s.date ASC, s.startTime ASC")
    List<AppointmentSlot> findAvailableSlotsByCenter(@Param("centerId") Long centerId);
    
    // Tìm slot trống theo center ID và ngày
    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "s.center.id = :centerId AND " +
           "s.date = :date AND " +
           "s.isAvailable = true AND " +
           "s.currentBookings < s.maxCapacity " +
           "ORDER BY s.startTime ASC")
    List<AppointmentSlot> findAvailableSlotsByCenterAndDate(
            @Param("centerId") Long centerId, 
            @Param("date") LocalDate date);
    
    // Tìm slot trống trong khoảng thời gian
    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "s.center.id = :centerId AND " +
           "s.date BETWEEN :startDate AND :endDate AND " +
           "s.isAvailable = true AND " +
           "s.currentBookings < s.maxCapacity " +
           "ORDER BY s.date ASC, s.startTime ASC")
    List<AppointmentSlot> findAvailableSlotsByDateRange(
            @Param("centerId") Long centerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // Tìm slot trống theo center, ngày và khoảng thời gian
    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "s.center.id = :centerId AND " +
           "s.date = :date AND " +
           "s.startTime >= :startTime AND " +
           "s.endTime <= :endTime AND " +
           "s.isAvailable = true AND " +
           "s.currentBookings < s.maxCapacity " +
           "ORDER BY s.startTime ASC")
    List<AppointmentSlot> findAvailableSlotsByCenterDateAndTimeRange(
            @Param("centerId") Long centerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
    
    // Kiểm tra slot có trùng lịch không (cùng center, ngày, và thời gian giao nhau)
    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "s.center.id = :centerId AND " +
           "s.date = :date AND " +
           "s.id != :excludeId AND " +
           "((s.startTime < :endTime AND s.endTime > :startTime))")
    List<AppointmentSlot> findOverlappingSlots(
            @Param("centerId") Long centerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
    
    // Tìm slot đã hết chỗ (currentBookings >= maxCapacity) nhưng chưa được đánh dấu unavailable
    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "s.currentBookings >= s.maxCapacity AND " +
           "s.isAvailable = true")
    List<AppointmentSlot> findFullSlots();
    
    // Tìm slot đã quá ngày nhưng chưa được đánh dấu unavailable
    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "s.date < :today AND " +
           "s.isAvailable = true")
    List<AppointmentSlot> findPastSlots(@Param("today") LocalDate today);
}






