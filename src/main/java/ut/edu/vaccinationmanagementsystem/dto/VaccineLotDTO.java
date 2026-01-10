package ut.edu.vaccinationmanagementsystem.dto;

import ut.edu.vaccinationmanagementsystem.entity.enums.VaccineLotStatus;

import java.time.LocalDate;

/**
 * DTO cho VaccineLot - Dùng chung cho cả Create và Update
 * Chỉ chứa các field cần thiết, không có id, createdAt, relationships
 */
public class VaccineLotDTO {
    private String lotNumber;
    private Long vaccineId; // ID của vaccine
    private Integer quantity;
    private Integer remainingQuantity;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private String supplier;
    private LocalDate importDate;
    private VaccineLotStatus status;
    
    // Getters and Setters
    public String getLotNumber() {
        return lotNumber;
    }
    
    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }
    
    public Long getVaccineId() {
        return vaccineId;
    }
    
    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
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
}

