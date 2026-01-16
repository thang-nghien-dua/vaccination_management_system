package ut.edu.vaccinationmanagementsystem.dto;

/**
 * DTO cho yêu cầu đặt lịch trực tiếp (không cần tư vấn)
 */
public class BookAppointmentDTO {
    private Long vaccineId; // Vaccine muốn tiêm (bắt buộc)
    private Long centerId; // Trung tâm y tế (bắt buộc)
    private Long slotId; // Slot thời gian đã chọn (bắt buộc)
    private Long bookedForUserId; // null = đặt cho bản thân, có giá trị = đặt cho người thân
    private String phoneNumber; // Số điện thoại từ form đặt lịch (bắt buộc)
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
    
    public Long getBookedForUserId() {
        return bookedForUserId;
    }
    
    public void setBookedForUserId(Long bookedForUserId) {
        this.bookedForUserId = bookedForUserId;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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


