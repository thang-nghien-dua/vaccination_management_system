package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.CenterVaccine;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;

import java.util.List;
import java.util.Optional;

@Repository
public interface CenterVaccineRepository extends JpaRepository<CenterVaccine, Long> {
    
    List<CenterVaccine> findByCenter(VaccinationCenter center);
    
    List<CenterVaccine> findByCenterId(Long centerId);
    
    Optional<CenterVaccine> findByCenterAndVaccine(VaccinationCenter center, Vaccine vaccine);
    
    boolean existsByCenterAndVaccine(VaccinationCenter center, Vaccine vaccine);
}


