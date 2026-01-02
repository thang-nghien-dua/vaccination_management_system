package ut.edu.vaccinationmanagement_system.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ut.edu.vaccinationmanagement_system.entity.VaccineLot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VaccineLotRepository extends JpaRepository<VaccineLot, Long> {

    Optional<VaccineLot> findByLotNumber(String lotNumber);

    // Lô sắp hết hạn
    @Query("""
        SELECT v FROM VaccineLot v
        WHERE v.expiryDate <= :date
          AND v.status = 'AVAILABLE'
    """)
    List<VaccineLot> findExpiringBefore(@Param("date") LocalDate date);

    // Lô sắp hết số lượng
    List<VaccineLot> findByRemainingQuantityLessThanEqual(int threshold);
}
