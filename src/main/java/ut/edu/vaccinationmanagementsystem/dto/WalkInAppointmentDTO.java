package ut.edu.vaccinationmanagementsystem.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO cho đăng ký walk-in (không đặt trước)
 */
public class WalkInAppointmentDTO {
    private Long vaccineId; // Vaccine muốn tiêm (bắt buộc)
    private Long centerId; // Trung tâm y tế (bắt buộc)
    private Long slotId; // Slot ID (bắt buộc cho walk-in)
    private LocalDate appointmentDate; // Ngày tiêm (bắt buộc)
    private LocalTime appointmentTime; // Giờ tiêm (bắt buộc)
    private Long roomId; // Phòng khám (tùy chọn)
    
    // Thông tin khách hàng
    private String fullName; // Họ tên (bắt buộc)
    private String phoneNumber; // Số điện thoại (bắt buộc)
    private String email; // Email (tùy chọn)
    private LocalDate dayOfBirth; // Ngày sinh (tùy chọn)
    private String gender; // Giới tính (tùy chọn: MALE, FEMALE, OTHER)
    
    private Integer doseNumber; // Mũi thứ mấy (mặc định 1)
    private String notes; // Ghi chú thêm (tùy chọn)
    private String paymentMethod; // Phương thức thanh toán (CASH, VNPAY)
    
    // Getters and Setters
    public Long getVaccineId() {
        return vaccineId;
    }
    
    public void setVaccineId(Long vaccineId) {
        this.vaccineId = vaccineId;
    }
    
    public Long getCenterId() {
        return centerId;
    }
    
    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }
    
    public Long getSlotId() {
        return slotId;
    }
    
    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }
    
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public Long getRoomId() {
        return roomId;
    }
    
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
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
    
    public LocalDate getDayOfBirth() {
        return dayOfBirth;
    }
    
    public void setDayOfBirth(LocalDate dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public Integer getDoseNumber() {
        return doseNumber;
    }
    
    public void setDoseNumber(Integer doseNumber) {
        this.doseNumber = doseNumber;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
