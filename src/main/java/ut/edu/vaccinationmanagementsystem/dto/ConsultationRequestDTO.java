package ut.edu.vaccinationmanagementsystem.dto;

import ut.edu.vaccinationmanagementsystem.entity.enums.Gender;

import java.time.LocalDate;

/**
 * DTO cho yêu cầu tư vấn tiêm chủng
 * Hỗ trợ cả user đã đăng nhập và guest chưa đăng nhập
 */
public class ConsultationRequestDTO {
    // Cho user đã đăng nhập
    private Long bookedForUserId; // null = bản thân, có giá trị = người thân
    private Long vaccineId; // Vaccine quan tâm (nullable)
    
    // Cho guest chưa đăng nhập
    private String guestFullName;
    private String guestEmail;
    private LocalDate guestDayOfBirth;
    private Gender guestGender;
    
    // Chung cho cả 2 loại
    private String consultationPhone; // SĐT để lễ tân gọi
    private String workUnit; // Đơn vị công tác (optional)
    private String reason; // Lý do cần tư vấn
    private String notes; // Ghi chú thêm
    
    // Getters and Setters
    public Long getBookedForUserId() {
        return bookedForUserId;
    }
    
    public void setBookedForUserId(Long bookedForUserId) {
        this.bookedForUserId = bookedForUserId;
    }
    
    public Long getVaccineId() {
        return vaccineId;
    }
    
    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }
    
    public String getGuestFullName() {
        return guestFullName;
    }
    
    public void setGuestFullName(String guestFullName) {
        this.guestFullName = guestFullName;
    }
    
    public String getGuestEmail() {
        return guestEmail;
    }
    
    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }
    
    public LocalDate getGuestDayOfBirth() {
        return guestDayOfBirth;
    }
    
    public void setGuestDayOfBirth(LocalDate guestDayOfBirth) {
        this.guestDayOfBirth = guestDayOfBirth;
    }
    
    public Gender getGuestGender() {
        return guestGender;
    }
    
    public void setGuestGender(Gender guestGender) {
        this.guestGender = guestGender;
    }
    
    public String getConsultationPhone() {
        return consultationPhone;
    }
    
    public void setConsultationPhone(String consultationPhone) {
        this.consultationPhone = consultationPhone;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getWorkUnit() {
        return workUnit;
    }
    
    public void setWorkUnit(String workUnit) {
        this.workUnit = workUnit;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}




