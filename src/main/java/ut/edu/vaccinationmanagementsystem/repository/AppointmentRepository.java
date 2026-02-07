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

    @Query("SELECT a FROM Appointment a WHERE " +
           "((a.bookedForUser.id = :userId) OR (a.bookedForUser IS NULL AND a.bookedByUser.id = :userId)) AND " +
           "a.slot.id = :slotId AND " +
           "a.status IN :statuses")
    List<Appointment> findExistingAppointmentsByUserAndSlot(
            @Param("userId") Long userId,
            @Param("slotId") Long slotId,
            @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a WHERE " +
           "a.familyMember.id = :familyMemberId AND " +
           "a.slot.id = :slotId AND " +
           "a.status IN :statuses")
    List<Appointment> findExistingAppointmentsByFamilyMemberAndSlot(
            @Param("familyMemberId") Long familyMemberId,
            @Param("slotId") Long slotId,
            @Param("statuses") List<AppointmentStatus> statuses);
    

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
    

    @Query("SELECT a FROM Appointment a WHERE " +
           "a.familyMember.id = :familyMemberId " +
           "ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<Appointment> findByFamilyMemberIdOrderByAppointmentDateDesc(@Param("familyMemberId") Long familyMemberId);
    

    @Query("SELECT a FROM Appointment a WHERE " +
           "((a.bookedForUser.id = :userId) OR (a.bookedForUser IS NULL AND a.bookedByUser.id = :userId)) AND " +
           "a.vaccine.id = :vaccineId AND " +
           "a.status IN :statuses")
    List<Appointment> findByBookedForUserAndVaccineAndStatusIn(
            @Param("userId") Long userId,
            @Param("vaccineId") Long vaccineId,
            @Param("statuses") List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a WHERE " +
           "a.familyMember.id = :familyMemberId AND " +
           "a.vaccine.id = :vaccineId AND " +
           "a.status IN :statuses")
    List<Appointment> findByFamilyMemberAndVaccineAndStatusIn(
            @Param("familyMemberId") Long familyMemberId,
            @Param("vaccineId") Long vaccineId,
            @Param("statuses") List<AppointmentStatus> statuses);

    long countByStatus(AppointmentStatus status);

    List<Appointment> findByStatus(AppointmentStatus status);

    long countByStatusAndCenterId(AppointmentStatus status, Long centerId);

    List<Appointment> findByStatusAndCenterId(AppointmentStatus status, Long centerId);
}



