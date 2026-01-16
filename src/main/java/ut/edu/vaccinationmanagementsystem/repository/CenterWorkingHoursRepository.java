package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.CenterWorkingHours;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.entity.enums.DayOfWeek;

import java.util.List;
import java.util.Optional;

@Repository
public interface CenterWorkingHoursRepository extends JpaRepository<CenterWorkingHours, Long> {
    
    List<CenterWorkingHours> findByCenter(VaccinationCenter center);
    
    List<CenterWorkingHours> findByCenterId(Long centerId);
    
    Optional<CenterWorkingHours> findByCenterAndDayOfWeek(VaccinationCenter center, DayOfWeek dayOfWeek);
    
    boolean existsByCenterAndDayOfWeek(VaccinationCenter center, DayOfWeek dayOfWeek);
}









