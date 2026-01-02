package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.VaccineLotStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lô vaccine - Quản lý từng lô vaccine nhập kho
 */
@Entity
@Table(name = "vaccine_lots")
public class VaccineLot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @Column(unique = true, nullable = false)
    private String lotNumber; // Số lô vaccine (duy nhất, ví dụ: "LOT-2024-001")
    
    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine; // Vaccine thuộc lô này
    
    @Column(nullable = false)
    private Integer quantity; // Số lượng ban đầu khi nhập kho
    
    @Column(nullable = false)
    private Integer remainingQuantity; // Số lượng còn lại trong kho
    
    @Column(nullable = false)
    private LocalDate manufacturingDate; // Ngày sản xuất
    
    @Column(nullable = false)
    private LocalDate expiryDate; // Ngày hết hạn
    
    @Column(nullable = true)
    private String supplier; // Nhà cung cấp
    
    @Column(nullable = false)
    private LocalDate importDate; // Ngày nhập kho
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VaccineLotStatus status; // Trạng thái (AVAILABLE, EXPIRED, DEPLETED)
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo
    
    // Relationships
    @OneToMany(mappedBy = "vaccineLot")
    private List<VaccinationRecord> vaccinationRecords; // Danh sách lần tiêm sử dụng lô này
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public String getLotNumber() {
        return lotNumber;
    }
    
    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }
    
    public Vaccine getVaccine() {
        return vaccine;
    }
    
    public void setVaccine(Vaccine vaccine) {
        this.vaccine = vaccine;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }
    
    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
    
    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }
    
    public void setManufacturingDate(LocalDate manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getSupplier() {
        return supplier;
    }
    
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
    
    public LocalDate getImportDate() {
        return importDate;
    }
    
    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }
    
    public VaccineLotStatus getStatus() {
        return status;
    }
    
    public void setStatus(VaccineLotStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<VaccinationRecord> getVaccinationRecords() {
        return vaccinationRecords;
    }
    
    public void setVaccinationRecords(List<VaccinationRecord> vaccinationRecords) {
        this.vaccinationRecords = vaccinationRecords;
    }
}


