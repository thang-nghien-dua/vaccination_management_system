package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.Payment;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByAppointment(Appointment appointment);
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Tìm các payment có phí hủy chưa thanh toán của một user
     * @param userId ID của user
     * @return Danh sách payment có phí hủy chưa thanh toán
     */
    @Query("SELECT p FROM Payment p WHERE p.appointment.bookedByUser.id = :userId " +
           "AND p.cancellationFee IS NOT NULL AND p.cancellationFee > 0 " +
           "AND p.cancellationFeePaid = false " +
           "AND p.paymentStatus = 'PENDING'")
    List<Payment> findUnpaidCancellationFeesByUser(@Param("userId") Long userId);
}


