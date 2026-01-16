package ut.edu.vaccinationmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.VoucherType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Voucher/Thẻ giảm giá - Mã giảm giá có thể sử dụng cho đơn hàng
 */
@Entity
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @Column(unique = true, nullable = false, length = 50)
    private String code; // Mã voucher (ví dụ: "WELCOME2024", "SAVE50K")
    
    @Column(nullable = false)
    private String name; // Tên voucher (ví dụ: "Chào mừng khách hàng mới")
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết về voucher
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType type; // Loại voucher (PERCENTAGE, FIXED_AMOUNT)
    
    @Column(nullable = true, precision = 5, scale = 2)
    private BigDecimal discountPercentage; // % giảm giá (nếu type = PERCENTAGE, ví dụ: 10.00 = 10%)
    
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal discountAmount; // Số tiền giảm (nếu type = FIXED_AMOUNT, ví dụ: 50000 = 50,000 VNĐ)
    
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal minOrderAmount; // Đơn tối thiểu để áp dụng voucher (null = không giới hạn)
    
    @Column(nullable = false)
    private LocalDateTime startDate; // Ngày bắt đầu voucher
    
    @Column(nullable = false)
    private LocalDateTime endDate; // Ngày kết thúc voucher
    
    @Column(nullable = false)
    private Boolean isActive; // Đang hoạt động hay không
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo
    
    // Relationships
    @OneToMany(mappedBy = "voucher")
    @JsonIgnore
    private List<UserVoucher> userVouchers; // Danh sách user đã sử dụng voucher này
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public VoucherType getType() {
        return type;
    }
    
    public void setType(VoucherType type) {
        this.type = type;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getMinOrderAmount() {
        return minOrderAmount;
    }
    
    public void setMinOrderAmount(BigDecimal minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<UserVoucher> getUserVouchers() {
        return userVouchers;
    }
    
    public void setUserVouchers(List<UserVoucher> userVouchers) {
        this.userVouchers = userVouchers;
    }
}




