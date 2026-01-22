package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByBookingCode(String bookingCode);
    
    List<Appointment> findByBookedByUserId(Long userId);
    
    List<Appointment> findByBookedForUserId(Long userId);
    
    List<Appointment> findByRequiresConsultationAndStatus(Boolean requiresConsultation, String status);
    
    /**
     * Kiểm tra xem một user đã có lịch hẹn trong slot này chưa (chỉ check PENDING và CONFIRMED)
     * @param userId ID của user cần check
     * @param slotId ID của slot
     * @return Danh sách appointment trùng lịch
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "((a.bookedForUser.id = :userId) OR (a.bookedForUser IS NULL AND a.bookedByUser.id = :userId)) AND " +
           "a.slot.id = :slotId AND " +
           "a.status IN :statuses")
    List<Appointment> findExistingAppointmentsByUserAndSlot(
            @Param("userId") Long userId,
            @Param("slotId") Long slotId,
            @Param("statuses") List<AppointmentStatus> statuses);
    
    /**
     * Kiểm tra xem một người thân (family member) đã có lịch hẹn trong slot này chưa
     * @param familyMemberId ID của family member cần check
     * @param slotId ID của slot
     * @return Danh sách appointment trùng lịch
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.familyMember.id = :familyMemberId AND " +
           "a.slot.id = :slotId AND " +
           "a.status IN :statuses")
    List<Appointment> findExistingAppointmentsByFamilyMemberAndSlot(
            @Param("familyMemberId") Long familyMemberId,
            @Param("slotId") Long slotId,
            @Param("statuses") List<AppointmentStatus> statuses);
    
    /**
     * Tìm tất cả appointments của một user trong khoảng thời gian (để check vaccine incompatibility)
     * @param userId ID của user
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách appointments
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "((a.bookedForUser.id = :userId) OR (a.bookedForUser IS NULL AND a.bookedByUser.id = :userId)) AND " +
           "a.slot.date BETWEEN :startDate AND :endDate AND " +
           "a.vaccine IS NOT NULL AND " +
           "a.status IN :statuses")
    List<Appointment> findAppointmentsByUserInDateRange(
            @Param("userId") Long userId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("statuses") List<AppointmentStatus> statuses);
    
    /**
     * Tìm tất cả appointments của một family member trong khoảng thời gian
     * @param familyMemberId ID của family member
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách appointments
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.familyMember.id = :familyMemberId AND " +
           "a.slot.date BETWEEN :startDate AND :endDate AND " +
           "a.vaccine IS NOT NULL AND " +
           "a.status IN :statuses")
    List<Appointment> findAppointmentsByFamilyMemberInDateRange(
            @Param("familyMemberId") Long familyMemberId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("statuses") List<AppointmentStatus> statuses);
    
    /**
     * Tìm tất cả appointments của một family member, sắp xếp theo ngày giảm dần
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.familyMember.id = :familyMemberId " +
           "ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findByFamilyMemberIdOrderByAppointmentDateDesc(@Param("familyMemberId") Long familyMemberId);
    
    /**
     * Tìm tất cả appointments của một user và vaccine cụ thể với status PENDING hoặc CONFIRMED
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "((a.bookedForUser.id = :userId) OR (a.bookedForUser IS NULL AND a.bookedByUser.id = :userId)) AND " +
           "a.vaccine.id = :vaccineId AND " +
           "a.status IN :statuses")
    List<Appointment> findByBookedForUserAndVaccineAndStatusIn(
            @Param("userId") Long userId,
            @Param("vaccineId") Long vaccineId,
            @Param("statuses") List<AppointmentStatus> statuses);
    
    /**
     * Tìm tất cả appointments của một family member và vaccine cụ thể với status PENDING hoặc CONFIRMED
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.familyMember.id = :familyMemberId AND " +
           "a.vaccine.id = :vaccineId AND " +
           "a.status IN :statuses")
    List<Appointment> findByFamilyMemberAndVaccineAndStatusIn(
            @Param("familyMemberId") Long familyMemberId,
            @Param("vaccineId") Long vaccineId,
            @Param("statuses") List<AppointmentStatus> statuses);
    
    /**
     * Đếm số lượng appointments theo status
     */
    long countByStatus(AppointmentStatus status);
    
    /**
     * Tìm tất cả appointments theo status
     */
    List<Appointment> findByStatus(AppointmentStatus status);
    
    /**
     * Đếm số lượng appointments theo status và centerId
     */
    long countByStatusAndCenterId(AppointmentStatus status, Long centerId);
    
    /**
     * Tìm tất cả appointments theo status và centerId
     */
    List<Appointment> findByStatusAndCenterId(AppointmentStatus status, Long centerId);
}



