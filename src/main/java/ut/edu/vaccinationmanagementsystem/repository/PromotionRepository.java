package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.Promotion;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    /**
     * Tìm các promotion đang hoạt động trong khoảng thời gian hiện tại
     */
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
    
    /**
     * Tìm các promotion đang áp dụng cho một vaccine cụ thể
     */
    @Query("SELECT p FROM Promotion p JOIN p.vaccines v WHERE v.id = :vaccineId AND p.isActive = true AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotionsByVaccine(@Param("vaccineId") Long vaccineId, @Param("now") LocalDateTime now);
    
    /**
     * Tìm tất cả promotion (bao gồm cả không hoạt động)
     */
    List<Promotion> findAllByOrderByCreatedAtDesc();
    
    /**
     * Tìm các promotion đang hoạt động
     */
    List<Promotion> findByIsActiveTrueOrderByCreatedAtDesc();
}




