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

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.startDate <= :now AND v.endDate >= :now")
    List<Voucher> findActiveVouchers(@Param("now") LocalDateTime now);

    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true AND v.startDate <= :now AND v.endDate >= :now")
    Optional<Voucher> findActiveVoucherByCode(@Param("code") String code, @Param("now") LocalDateTime now);

    List<Voucher> findAllByOrderByCreatedAtDesc();

    List<Voucher> findByIsActiveTrueOrderByCreatedAtDesc();
}




