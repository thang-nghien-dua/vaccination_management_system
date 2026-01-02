package ut.edu.vaccinationmanagementsystem.dto;

import ut.edu.vaccinationmanagementsystem.entity.enums.VaccineStatus;

import java.math.BigDecimal;

/**
 * DTO cho Vaccine - Dùng chung cho cả Create và Update
 * Chỉ chứa các field cần thiết, không có id, createdAt, relationships
 */
public class VaccineDTO {
    private String name;
    private String code;
    private String manufacturer;
    private String origin;
    private String description;
    private BigDecimal price;
    private Integer minAge;
    private Integer maxAge;
    private Integer dosesRequired;
    private Integer daysBetweenDoses;
    private String contraindications;
    private String storageTemperature;
    private VaccineStatus status;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getMinAge() {
        return minAge;
    }
    
    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }
    
    public Integer getMaxAge() {
        return maxAge;
    }
    
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }
    
    public Integer getDosesRequired() {
        return dosesRequired;
    }
    
    public void setDosesRequired(Integer dosesRequired) {
        this.dosesRequired = dosesRequired;
    }
    
    public Integer getDaysBetweenDoses() {
        return daysBetweenDoses;
    }
    
    public void setDaysBetweenDoses(Integer daysBetweenDoses) {
        this.daysBetweenDoses = daysBetweenDoses;
    }
    
    public String getContraindications() {
        return contraindications;
    }
    
    public void setContraindications(String contraindications) {
        this.contraindications = contraindications;
    }
    
    public String getStorageTemperature() {
        return storageTemperature;
    }
    
    public void setStorageTemperature(String storageTemperature) {
        this.storageTemperature = storageTemperature;
    }
    
    public VaccineStatus getStatus() {
        return status;
    }
    
    public void setStatus(VaccineStatus status) {
        this.status = status;
    }
}



