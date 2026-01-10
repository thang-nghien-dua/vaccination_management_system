package ut.edu.vaccinationmanagementsystem.dto;

import java.time.LocalDateTime;

/**
 * DTO cho CenterVaccine - Dùng chung cho cả Create và Update
 * Chỉ chứa các field cần thiết, không có id, lastRestocked
 */
public class CenterVaccineDTO {
    private Long centerId;
    private Long vaccineId;
    private Integer stockQuantity;
    
    // Getters and Setters
    public Long getCenterId() {
        return centerId;
    }
    
    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }
    
    public Long getVaccineId() {
        return vaccineId;
    }
    
    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}


