package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.VaccineLot;
import ut.edu.vaccinationmanagementsystem.entity.enums.VaccineLotStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VaccineLotRepository extends JpaRepository<VaccineLot, Long> {
    
    // Tìm lô vaccine theo số lô (duy nhất)
    Optional<VaccineLot> findByLotNumber(String lotNumber);
    
    // Kiểm tra số lô đã tồn tại chưa
    boolean existsByLotNumber(String lotNumber);
    
    // Tìm tất cả lô vaccine theo vaccine ID
    List<VaccineLot> findByVaccineId(Long vaccineId);
    
    // Tìm lô vaccine theo trạng thái
    List<VaccineLot> findByStatus(VaccineLotStatus status);
    
    // Tìm lô vaccine sắp hết hạn (trong vòng X ngày)
    @Query("SELECT vl FROM VaccineLot vl WHERE " +
           "vl.expiryDate BETWEEN :today AND :warningDate AND " +
           "vl.status = 'AVAILABLE' " +
           "ORDER BY vl.expiryDate ASC")
    List<VaccineLot> findExpiringSoon(@Param("today") LocalDate today, 
                                      @Param("warningDate") LocalDate warningDate);
    
    // Tìm lô vaccine sắp hết (remainingQuantity <= threshold)
    @Query("SELECT vl FROM VaccineLot vl WHERE " +
           "vl.remainingQuantity <= :threshold AND " +
           "vl.remainingQuantity > 0 AND " +
           "vl.status = 'AVAILABLE' " +
           "ORDER BY vl.remainingQuantity ASC")
    List<VaccineLot> findLowStock(@Param("threshold") Integer threshold);
    
    // Tìm lô vaccine đã hết hạn nhưng chưa được cập nhật status
    @Query("SELECT vl FROM VaccineLot vl WHERE " +
           "vl.expiryDate < :today AND " +
           "vl.status = 'AVAILABLE'")
    List<VaccineLot> findExpiredLots(@Param("today") LocalDate today);
    
    // Tìm lô vaccine đã hết nhưng chưa được cập nhật status
    @Query("SELECT vl FROM VaccineLot vl WHERE " +
           "vl.remainingQuantity = 0 AND " +
           "vl.status = 'AVAILABLE'")
    List<VaccineLot> findDepletedLots();
    
    // Tìm kiếm lô vaccine theo từ khóa (số lô, nhà cung cấp)
    @Query("SELECT vl FROM VaccineLot vl WHERE " +
           "LOWER(vl.lotNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(vl.supplier) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<VaccineLot> searchByKeyword(@Param("keyword") String keyword);
}

