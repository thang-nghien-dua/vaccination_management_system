package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.Voucher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    /**
     * Tìm voucher theo mã code
     */
    Optional<Voucher> findByCode(String code);
    
    /**
     * Kiểm tra voucher có tồn tại theo code không
     */
    boolean existsByCode(String code);
    
    /**
     * Tìm các voucher đang hoạt động và có hiệu lực trong khoảng thời gian hiện tại
     */
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findActiveVouchers(@Param("now") LocalDateTime now);
    
    /**
     * Tìm voucher theo code và đang hoạt động
     */
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true AND v.startDate <= :now AND v.endDate >= :now")
    Optional<Voucher> findActiveVoucherByCode(@Param("code") String code, @Param("now") LocalDateTime now);
    
    /**
     * Tìm tất cả voucher (bao gồm cả không hoạt động)
     */
    List<Voucher> findAllByOrderByCreatedAtDesc();
    
    /**
     * Tìm các voucher đang hoạt động
     */
    List<Voucher> findByIsActiveTrueOrderByCreatedAtDesc();
}




