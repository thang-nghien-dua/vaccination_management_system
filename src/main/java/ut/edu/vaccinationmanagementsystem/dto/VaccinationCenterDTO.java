package ut.edu.vaccinationmanagementsystem.dto;

import ut.edu.vaccinationmanagementsystem.entity.enums.CenterStatus;

/**
 * DTO cho VaccinationCenter - Dùng chung cho cả Create và Update
 * Chỉ chứa các field cần thiết, không có id, createdAt, relationships
 */
public class VaccinationCenterDTO {
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private Integer capacity;
    private CenterStatus status;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public CenterStatus getStatus() {
        return status;
    }
    
    public void setStatus(CenterStatus status) {
        this.status = status;
    }
}


