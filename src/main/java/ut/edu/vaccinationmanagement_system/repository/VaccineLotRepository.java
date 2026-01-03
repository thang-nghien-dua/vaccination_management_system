package ut.edu.vaccinationmanagement_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ut.edu.vaccinationmanagement_system.entity.VaccineLot;

import java.time.LocalDate;
import java.util.List;

public interface VaccineLotRepository extends JpaRepository<VaccineLot, Long> {

    // Cảnh báo sắp hết hạn
    @Query("SELECT v FROM VaccineLot v WHERE v.expiryDate <= :date")
    List<VaccineLot> findExpiringSoon(LocalDate date);

    // Cảnh báo sắp hết số lượng
    List<VaccineLot> findByRemainingQuantityLessThanEqual(Integer threshold);
}
