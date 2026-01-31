package ut.edu.vaccinationmanagementsystem.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO cho AppointmentSlot - Dùng chung cho cả Create và Update
 * Chỉ chứa các field cần thiết, không có id, createdAt, relationships
 */
public class AppointmentSlotDTO {
    private Long centerId; // ID của vaccination center
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxCapacity;
    private Integer currentBookings;
    private Boolean isAvailable;
    private Long roomId; // ID của phòng khám (optional - có thể null)
    
    // Getters and Setters
    public Long getCenterId() {
        return centerId;
    }
    
    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getMaxCapacity() {
        return maxCapacity;
    }
    
    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    
    public Integer getCurrentBookings() {
        return currentBookings;
    }
    
    public void setCurrentBookings(Integer currentBookings) {
        this.currentBookings = currentBookings;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public Long getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}






