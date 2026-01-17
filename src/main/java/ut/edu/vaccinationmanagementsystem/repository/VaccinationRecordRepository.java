package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {
    /**
     * Đếm số lượng vaccination records của một vaccine
     */
    long countByVaccine(Vaccine vaccine);
    
    /**
     * Lấy danh sách vaccine và số lượng đã tiêm, sắp xếp theo số lượng giảm dần
     */
    @Query("SELECT v, COUNT(vr.id) as count " +
           "FROM Vaccine v " +
           "LEFT JOIN VaccinationRecord vr ON vr.vaccine.id = v.id " +
           "GROUP BY v.id " +
           "ORDER BY count DESC")
    List<Object[]> findVaccinesWithVaccinationCount();
    
    /**
     * Tìm tất cả vaccination records của một family member thông qua appointments
     * Sắp xếp theo ngày tiêm giảm dần
     */
    @Query("SELECT vr FROM VaccinationRecord vr " +
           "WHERE vr.appointment.familyMember.id = :familyMemberId " +
           "ORDER BY vr.injectionDate DESC, vr.injectionTime DESC")
    List<VaccinationRecord> findByAppointmentFamilyMemberIdOrderByInjectionDateDesc(
            @Param("familyMemberId") Long familyMemberId);
    
    /**
     * Tìm tất cả vaccination records của một user và vaccine cụ thể
     */
    List<VaccinationRecord> findByUserAndVaccine(ut.edu.vaccinationmanagementsystem.entity.User user, 
                                                  ut.edu.vaccinationmanagementsystem.entity.Vaccine vaccine);
    
    /**
     * Tìm tất cả vaccination records của một family member và vaccine cụ thể
     */
    @Query("SELECT vr FROM VaccinationRecord vr " +
           "WHERE vr.appointment.familyMember.id = :familyMemberId " +
           "AND vr.vaccine.id = :vaccineId")
    List<VaccinationRecord> findByAppointmentFamilyMemberIdAndVaccine(
            @Param("familyMemberId") Long familyMemberId,
            @Param("vaccineId") Long vaccineId);
    
    /**
     * Tìm tất cả vaccination records của một user
     * Sắp xếp theo ngày tiêm giảm dần
     */
    @Query("SELECT vr FROM VaccinationRecord vr " +
           "WHERE vr.user.id = :userId " +
           "ORDER BY vr.injectionDate DESC, vr.injectionTime DESC")
    List<VaccinationRecord> findByUserIdOrderByInjectionDateDesc(@Param("userId") Long userId);
    
    /**
     * Đếm số lượng vaccination records được tiêm trong ngày cụ thể
     */
    @Query("SELECT COUNT(vr) FROM VaccinationRecord vr WHERE vr.injectionDate = :date")
    long countByInjectionDate(@Param("date") java.time.LocalDate date);
    
    /**
     * Đếm số lượng vaccination records được tiêm bởi một nurse trong ngày cụ thể
     */
    @Query("SELECT COUNT(vr) FROM VaccinationRecord vr WHERE vr.nurse.id = :nurseId AND vr.injectionDate = :date")
    long countByNurseIdAndInjectionDate(@Param("nurseId") Long nurseId, @Param("date") java.time.LocalDate date);
    
    /**
     * Lấy danh sách vaccination records được tiêm trong ngày cụ thể
     */
    @Query("SELECT vr FROM VaccinationRecord vr WHERE vr.injectionDate = :date ORDER BY vr.injectionTime DESC")
    List<VaccinationRecord> findByInjectionDate(@Param("date") java.time.LocalDate date);
    
    /**
     * Tìm vaccination record theo số chứng nhận
     */
    Optional<VaccinationRecord> findByCertificateNumber(String certificateNumber);
}



