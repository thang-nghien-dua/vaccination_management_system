package ut.edu.vaccinationmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Chương trình ưu đãi - Quản lý các chương trình khuyến mãi cho vaccine
 */
@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @Column(nullable = false)
    private String name; // Tên chương trình ưu đãi (ví dụ: "Giảm giá 20% cho vaccine cúm")
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết về ưu đãi
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionType type; // Loại ưu đãi (PERCENTAGE, FIXED_AMOUNT, PACKAGE)
    
    @Column(nullable = true, precision = 5, scale = 2)
    private BigDecimal discountPercentage; // % giảm giá (nếu type = PERCENTAGE, ví dụ: 20.00 = 20%)
    
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal discountAmount; // Số tiền giảm (nếu type = FIXED_AMOUNT, ví dụ: 50000 = 50,000 VNĐ)
    
    @Column(nullable = false)
    private LocalDateTime startDate; // Ngày bắt đầu ưu đãi
    
    @Column(nullable = false)
    private LocalDateTime endDate; // Ngày kết thúc ưu đãi
    
    @Column(nullable = false)
    private Boolean isActive; // Đang hoạt động hay không
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo
    
    // Relationships
    @ManyToMany
    @JoinTable(
        name = "promotion_vaccine",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "vaccine_id")
    )
    @JsonIgnore
    private List<Vaccine> vaccines; // Danh sách vaccine được áp dụng ưu đãi
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public PromotionType getType() {
        return type;
    }
    
    public void setType(PromotionType type) {
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
    
    public List<Vaccine> getVaccines() {
        return vaccines;
    }
    
    public void setVaccines(List<Vaccine> vaccines) {
        this.vaccines = vaccines;
    }
}




