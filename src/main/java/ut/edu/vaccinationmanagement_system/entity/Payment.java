package ut.edu.vaccinationmanagement_system.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagement_system.entity.enums.PaymentMethod;
import ut.edu.vaccinationmanagement_system.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thông tin thanh toán cho lịch hẹn
 */
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true, nullable = false)
    private Appointment appointment; // Lịch hẹn liên kết (One-to-One)
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Số tiền thanh toán
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod; // Phương thức thanh toán (CASH, VNPAY)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus; // Trạng thái thanh toán (PENDING, PAID, REFUNDED, FAILED)
    
    @Column(nullable = true)
    private String transactionId; // Mã giao dịch từ cổng thanh toán (ví dụ: VNPay transaction ID)
    
    @Column(nullable = true)
    private LocalDateTime paidAt; // Thời gian thanh toán
    
    @Column(nullable = true, unique = true)
    private String invoiceNumber; // Số hóa đơn (duy nhất)
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Appointment getAppointment() {
        return appointment;
    }
    
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public LocalDateTime getPaidAt() {
        return paidAt;
    }
    
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
}

