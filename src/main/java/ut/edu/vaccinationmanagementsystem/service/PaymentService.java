package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.PaymentMethod;
import ut.edu.vaccinationmanagementsystem.entity.enums.PaymentStatus;
import ut.edu.vaccinationmanagementsystem.repository.PaymentRepository;
import ut.edu.vaccinationmanagementsystem.repository.PromotionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    /**
     * Tính giá cuối cùng của vaccine (sau khi áp dụng promotion)
     * @param vaccine Vaccine cần tính giá
     * @return Giá cuối cùng
     */
    public BigDecimal calculateFinalPrice(Vaccine vaccine) {
        BigDecimal basePrice = vaccine.getPrice();
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }
        
        // Lấy promotion đang hoạt động
        List<Promotion> activePromotions = promotionRepository.findActivePromotionsByVaccine(
            vaccine.getId(), LocalDateTime.now()
        );
        
        if (activePromotions.isEmpty()) {
            return basePrice;
        }
        
        // Áp dụng promotion đầu tiên
        Promotion firstPromotion = activePromotions.get(0);
        BigDecimal finalPrice = basePrice;
        
        if (firstPromotion.getType().toString().equals("PERCENTAGE") && 
            firstPromotion.getDiscountPercentage() != null) {
            BigDecimal discount = basePrice.multiply(firstPromotion.getDiscountPercentage())
                .divide(new BigDecimal("100"));
            finalPrice = basePrice.subtract(discount);
        } else if (firstPromotion.getType().toString().equals("FIXED_AMOUNT") && 
                   firstPromotion.getDiscountAmount() != null) {
            finalPrice = basePrice.subtract(firstPromotion.getDiscountAmount());
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }
        }
        
        return finalPrice;
    }
    
    /**
     * Tạo Payment cho Appointment
     * @param appointment Appointment cần tạo payment
     * @param paymentMethod Phương thức thanh toán
     * @return Payment đã được tạo
     */
    public Payment createPayment(Appointment appointment, PaymentMethod paymentMethod) {
        if (appointment.getVaccine() == null) {
            throw new RuntimeException("Appointment must have a vaccine to create payment");
        }
        
        // Tính giá cuối cùng
        BigDecimal amount = calculateFinalPrice(appointment.getVaccine());
        
        // Tạo Payment
        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        
        // Nếu là tiền mặt, status = PENDING (chờ thanh toán tại trung tâm)
        // Nếu là VNPay, status = PENDING (chờ thanh toán online)
        payment.setPaymentStatus(PaymentStatus.PENDING);
        
        // Generate invoice number
        payment.setInvoiceNumber(generateInvoiceNumber());
        
        return paymentRepository.save(payment);
    }
    
    /**
     * Cập nhật Payment sau khi thanh toán thành công qua VNPay
     * @param payment Payment cần cập nhật
     * @param transactionId Mã giao dịch từ VNPay
     */
    public void markPaymentAsPaid(Payment payment, String transactionId) {
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setTransactionId(transactionId);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
    
    /**
     * Generate invoice number
     * Format: INV-YYYYMMDD-HHMMSS-XXXX
     */
    private String generateInvoiceNumber() {
        LocalDateTime now = LocalDateTime.now();
        String dateTime = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        int random = (int)(Math.random() * 10000);
        return String.format("INV-%s-%04d", dateTime, random);
    }
    
    /**
     * Tìm Payment theo Appointment
     */
    public Payment findByAppointment(Appointment appointment) {
        return paymentRepository.findByAppointment(appointment)
            .orElse(null);
    }
    
    /**
     * Tìm Payment theo Transaction ID
     */
    public Payment findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
            .orElse(null);
    }
    
    /**
     * Tính phí hủy dựa trên thời gian hủy
     * @param originalAmount Số tiền gốc của appointment
     * @param hoursUntilAppointment Số giờ còn lại trước giờ hẹn
     * @return Phí hủy (BigDecimal)
     */
    public BigDecimal calculateCancellationFee(BigDecimal originalAmount, long hoursUntilAppointment) {
        if (originalAmount == null || originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (hoursUntilAppointment >= 24) {
            return BigDecimal.ZERO; // Miễn phí (≥ 24 giờ)
        } else if (hoursUntilAppointment >= 12) {
            return originalAmount.multiply(new BigDecimal("0.20")); // 20% (12-24 giờ)
        } else if (hoursUntilAppointment >= 6) {
            return originalAmount.multiply(new BigDecimal("0.50")); // 50% (6-12 giờ)
        } else {
            return originalAmount; // 100% - không cho hủy hoặc phí 100% (< 6 giờ)
        }
    }
    
    /**
     * Tạo Payment mới cho phí hủy (phải thanh toán bằng VNPay)
     * Lưu ý: Payment có OneToOne với Appointment, nên ta sẽ lưu phí hủy vào Payment cũ
     * và tạo một Appointment "dummy" để tracking phí hủy
     * 
     * @param user User cần thanh toán phí hủy
     * @param cancellationFee Số tiền phí hủy
     * @param cancelledAppointment Appointment đã bị hủy
     * @return Payment đã được tạo (hoặc null nếu không cần tạo)
     */
    public Payment createCancellationFeePayment(User user, BigDecimal cancellationFee, Appointment cancelledAppointment) {
        if (cancellationFee == null || cancellationFee.compareTo(BigDecimal.ZERO) <= 0) {
            return null; // Không có phí hủy
        }
        
        // Vì Payment có OneToOne với Appointment và unique constraint,
        // ta sẽ chỉ lưu thông tin phí hủy vào Payment cũ của appointment đã hủy
        // Payment mới cho phí hủy sẽ được tạo thông qua một Appointment đặc biệt
        // Nhưng để đơn giản, ta chỉ cần đánh dấu trong Payment cũ là có phí hủy chưa thanh toán
        // User sẽ thanh toán phí hủy thông qua một endpoint riêng
        
        // Lưu ý: Logic này sẽ được xử lý trong AppointmentService.cancelAppointment()
        // Ở đây chỉ return null vì Payment đã được cập nhật trong cancelAppointment
        return null;
    }
    
    /**
     * Đánh dấu phí hủy đã được thanh toán
     * @param payment Payment chứa phí hủy
     */
    public void markCancellationFeeAsPaid(Payment payment) {
        payment.setCancellationFeePaid(true);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
}


