package ut.edu.vaccinationmanagementsystem.dto;

/**
 * DTO cho ClinicRoom - Dùng chung cho cả Create và Update
 */
public class ClinicRoomDTO {
    private Long centerId;
    private String roomNumber;
    private String description;
    private Boolean isActive;
    
    // Getters and Setters
    public Long getCenterId() {
        return centerId;
    }
    
    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

