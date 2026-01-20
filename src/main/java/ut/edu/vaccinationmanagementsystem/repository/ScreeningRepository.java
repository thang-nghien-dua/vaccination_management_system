package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.Screening;
import ut.edu.vaccinationmanagementsystem.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    
    Optional<Screening> findByAppointmentId(Long appointmentId);
    
    List<Screening> findByDoctorOrderByScreenedAtDesc(User doctor);
    
    @Query("SELECT s FROM Screening s WHERE s.doctor.id = :doctorId ORDER BY s.screenedAt DESC")
    List<Screening> findByDoctorIdOrderByScreenedAtDesc(@Param("doctorId") Long doctorId);
}

