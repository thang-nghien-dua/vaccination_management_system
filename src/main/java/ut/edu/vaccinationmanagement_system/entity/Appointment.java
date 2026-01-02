package ut.edu.vaccinationmanagement_system.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagement_system.entity.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Lịch hẹn tiêm chủng
 */
@Entity
@Table(name = "appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @Column(unique = true, nullable = false)
    private String bookingCode; // Mã booking (duy nhất, ví dụ: "BK-20241216-001")
    
    @ManyToOne
    @JoinColumn(name = "booked_by_user_id", nullable = false)
    private User bookedByUser; // Người đặt lịch (người đăng nhập và thực hiện đặt)
    
    @ManyToOne
    @JoinColumn(name = "booked_for_user_id", nullable = true)
    private User bookedForUser; // Đặt cho ai (null = tự đặt cho mình, có giá trị = đặt cho người thân)
    
    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine; // Vaccine muốn tiêm
    
    @ManyToOne
    @JoinColumn(name = "center_id", nullable = false)
    private VaccinationCenter center; // Trung tâm y tế đặt lịch
    
    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private AppointmentSlot slot; // Slot thời gian đã chọn
    
    @Column(nullable = false)
    private LocalDate appointmentDate; // Ngày hẹn tiêm
    
    @Column(nullable = false)
    private LocalTime appointmentTime; // Giờ hẹn tiêm
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status; // Trạng thái lịch hẹn
    
    @Column(nullable = false)
    private Integer doseNumber; // Mũi thứ mấy (ví dụ: 1, 2, 3)
    
    @Column(nullable = true)
    private Integer queueNumber; // Số thứ tự khi check-in
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String notes; // Ghi chú đặc biệt
    
    @Column(nullable = true)
    private String qrCode; // Mã QR code để check-in
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String cancellationReason; // Lý do hủy lịch
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian đặt lịch
    
    @Column(nullable = false)
    private LocalDateTime updatedAt; // Thời gian cập nhật cuối cùng
    
    // Relationships
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private MedicalDeclaration medicalDeclaration; // Khai báo y tế
    
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private Payment payment; // Thông tin thanh toán
    
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private Screening screening; // Kết quả khám sàng lọc
    
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private VaccinationRecord vaccinationRecord; // Hồ sơ tiêm chủng
    
    @OneToMany(mappedBy = "appointment")
    private List<AppointmentHistory> appointmentHistories; // Lịch sử thay đổi trạng thái
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBookingCode() {
        return bookingCode;
    }
    
    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }
    
    public User getBookedByUser() {
        return bookedByUser;
    }
    
    public void setBookedByUser(User bookedByUser) {
        this.bookedByUser = bookedByUser;
    }
    
    public User getBookedForUser() {
        return bookedForUser;
    }
    
    public void setBookedForUser(User bookedForUser) {
        this.bookedForUser = bookedForUser;
    }
    
    public Vaccine getVaccine() {
        return vaccine;
    }
    
    public void setVaccine(Vaccine vaccine) {
        this.vaccine = vaccine;
    }
    
    public VaccinationCenter getCenter() {
        return center;
    }
    
    public void setCenter(VaccinationCenter center) {
        this.center = center;
    }
    
    public AppointmentSlot getSlot() {
        return slot;
    }
    
    public void setSlot(AppointmentSlot slot) {
        this.slot = slot;
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
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    
    public Integer getDoseNumber() {
        return doseNumber;
    }
    
    public void setDoseNumber(Integer doseNumber) {
        this.doseNumber = doseNumber;
    }
    
    public Integer getQueueNumber() {
        return queueNumber;
    }
    
    public void setQueueNumber(Integer queueNumber) {
        this.queueNumber = queueNumber;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getQrCode() {
        return qrCode;
    }
    
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public MedicalDeclaration getMedicalDeclaration() {
        return medicalDeclaration;
    }
    
    public void setMedicalDeclaration(MedicalDeclaration medicalDeclaration) {
        this.medicalDeclaration = medicalDeclaration;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    
    public Screening getScreening() {
        return screening;
    }
    
    public void setScreening(Screening screening) {
        this.screening = screening;
    }
    
    public VaccinationRecord getVaccinationRecord() {
        return vaccinationRecord;
    }
    
    public void setVaccinationRecord(VaccinationRecord vaccinationRecord) {
        this.vaccinationRecord = vaccinationRecord;
    }
    
    public List<AppointmentHistory> getAppointmentHistories() {
        return appointmentHistories;
    }
    
    public void setAppointmentHistories(List<AppointmentHistory> appointmentHistories) {
        this.appointmentHistories = appointmentHistories;
    }
}

