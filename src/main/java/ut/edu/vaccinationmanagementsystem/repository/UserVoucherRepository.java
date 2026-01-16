package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.UserVoucher;
import ut.edu.vaccinationmanagementsystem.entity.Voucher;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    /**
     * Kiểm tra user đã sử dụng voucher này chưa
     */
    boolean existsByUserAndVoucher(User user, Voucher voucher);
    
    /**
     * Tìm UserVoucher theo user và voucher
     */
    Optional<UserVoucher> findByUserAndVoucher(User user, Voucher voucher);
    
    /**
     * Lấy danh sách voucher mà user đã sử dụng
     */
    @Query("SELECT uv.voucher FROM UserVoucher uv WHERE uv.user = :user")
    List<Voucher> findVouchersUsedByUser(@Param("user") User user);
    
    /**
     * Lấy danh sách UserVoucher của một user
     */
    List<UserVoucher> findByUserOrderByUsedAtDesc(User user);
    
    /**
     * Đếm số lần voucher đã được sử dụng
     */
    long countByVoucher(Voucher voucher);
}




