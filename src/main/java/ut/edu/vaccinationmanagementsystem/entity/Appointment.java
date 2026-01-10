package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Lịch hẹn tiêm chủng
 */
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @Column(unique = true, nullable = false)
    private String bookingCode; // Mã booking (duy nhất, ví dụ: "BK-20241216-001")
    
    @ManyToOne
    @JoinColumn(name = "booked_by_user_id", nullable = true)
    private User bookedByUser; // Người đặt lịch (null = guest chưa đăng nhập, có giá trị = user đã đăng nhập)
    
    @ManyToOne
    @JoinColumn(name = "booked_for_user_id", nullable = true)
    private User bookedForUser; // Đặt cho ai (null = tự đặt cho mình, có giá trị = đặt cho người thân)
    
    @ManyToOne
    @JoinColumn(name = "family_member_id", nullable = true)
    private FamilyMember familyMember; // Đặt cho người thân (nullable, chỉ có khi đặt cho người thân)
    
    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = true)
    private Vaccine vaccine; // Vaccine muốn tiêm (nullable khi consultation request)
    
    @ManyToOne
    @JoinColumn(name = "center_id", nullable = true)
    private VaccinationCenter center; // Trung tâm y tế đặt lịch (nullable khi consultation request)
    
    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = true)
    private AppointmentSlot slot; // Slot thời gian đã chọn (nullable khi consultation request)
    
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = true)
    private ClinicRoom room; // Phòng khám được chỉ định (nullable vì có thể gán khi check-in)
    
    @Column(nullable = true)
    private LocalDate appointmentDate; // Ngày hẹn tiêm (nullable khi consultation request)
    
    @Column(nullable = true)
    private LocalTime appointmentTime; // Giờ hẹn tiêm (nullable khi consultation request)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status; // Trạng thái lịch hẹn
    
    @Column(nullable = false)
    private Integer doseNumber; // Mũi thứ mấy (ví dụ: 1, 2, 3)
    
    @Column(nullable = true)
    private Integer queueNumber; // Số thứ tự khi check-in
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String notes; // Ghi chú đặc biệt
    
    @Column(nullable = false)
    private Boolean requiresConsultation; // true = cần tư vấn qua điện thoại, false = tự đặt trực tiếp
    
    @Column(nullable = true)
    private String consultationPhone; // Số điện thoại ưu tiên để lễ tân gọi tư vấn (nullable)
    
    // Thông tin guest (chỉ dùng khi bookedByUser = null)
    @Column(nullable = true)
    private String guestFullName; // Họ tên người guest (khi chưa đăng nhập)
    
    @Column(nullable = true)
    private String guestEmail; // Email người guest (khi chưa đăng nhập)
    
    @Column(nullable = true)
    private LocalDate guestDayOfBirth; // Ngày sinh người guest
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender guestGender; // Giới tính người guest
    
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
    
    public FamilyMember getFamilyMember() {
        return familyMember;
    }
    
    public void setFamilyMember(FamilyMember familyMember) {
        this.familyMember = familyMember;
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
    
    public ClinicRoom getRoom() {
        return room;
    }
    
    public void setRoom(ClinicRoom room) {
        this.room = room;
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
    
    public Boolean getRequiresConsultation() {
        return requiresConsultation;
    }
    
    public void setRequiresConsultation(Boolean requiresConsultation) {
        this.requiresConsultation = requiresConsultation;
    }
    
    public String getConsultationPhone() {
        return consultationPhone;
    }
    
    public void setConsultationPhone(String consultationPhone) {
        this.consultationPhone = consultationPhone;
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


