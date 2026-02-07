package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {

    long countByVaccine(Vaccine vaccine);

    @Query("SELECT v, COUNT(vr.id) as count " +
           "FROM Vaccine v " +
           "LEFT JOIN VaccinationRecord vr ON vr.vaccine.id = v.id " +
           "GROUP BY v.id " +
           "ORDER BY count DESC")
    List<Object[]> findVaccinesWithVaccinationCount();

    @Query("SELECT vr FROM VaccinationRecord vr " +
           "WHERE vr.appointment.familyMember.id = :familyMemberId " +
           "ORDER BY vr.injectionDate DESC, vr.injectionTime DESC")
    List<VaccinationRecord> findByAppointmentFamilyMemberIdOrderByInjectionDateDesc(
            @Param("familyMemberId") Long familyMemberId);

    List<VaccinationRecord> findByUserAndVaccine(ut.edu.vaccinationmanagementsystem.entity.User user, 
                                                  ut.edu.vaccinationmanagementsystem.entity.Vaccine vaccine);

    @Query("SELECT vr FROM VaccinationRecord vr " +
           "WHERE vr.appointment.familyMember.id = :familyMemberId " +
           "AND vr.vaccine.id = :vaccineId")
    List<VaccinationRecord> findByAppointmentFamilyMemberIdAndVaccine(
            @Param("familyMemberId") Long familyMemberId,
            @Param("vaccineId") Long vaccineId);

    @Query("SELECT vr FROM VaccinationRecord vr " +
           "WHERE vr.user.id = :userId " +
           "ORDER BY vr.injectionDate DESC, vr.injectionTime DESC")
    List<VaccinationRecord> findByUserIdOrderByInjectionDateDesc(@Param("userId") Long userId);
    

    @Query("SELECT COUNT(vr) FROM VaccinationRecord vr " +
           "WHERE vr.nurse.id = :nurseId AND vr.injectionDate = :date")
    long countByNurseIdAndInjectionDate(@Param("nurseId") Long nurseId, @Param("date") LocalDate date);

    List<VaccinationRecord> findByInjectionDate(LocalDate injectionDate);
    

    boolean existsByAppointment(ut.edu.vaccinationmanagementsystem.entity.Appointment appointment);

    List<VaccinationRecord> findByInjectionDateAndAppointmentCenterId(LocalDate injectionDate, Long centerId);
}



