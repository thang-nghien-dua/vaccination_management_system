package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Hồ sơ tiêm chủng - Ghi nhận thông tin tiêm vaccine
 */
@Entity
@Table(name = "vaccination_record")
public class VaccinationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true, nullable = false)
    private Appointment appointment; // Lịch hẹn liên kết (One-to-One)
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người được tiêm
    
    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine; // Vaccine đã tiêm
    
    @ManyToOne
    @JoinColumn(name = "vaccine_lot_id", nullable = false)
    private VaccineLot vaccineLot; // Lô vaccine đã sử dụng
    
    @ManyToOne
    @JoinColumn(name = "nurse_id", nullable = false)
    private User nurse; // Bác sĩ/y tá thực hiện tiêm
    
    @Column(nullable = false)
    private LocalDate injectionDate; // Ngày tiêm
    
    @Column(nullable = false)
    private LocalTime injectionTime; // Giờ tiêm
    
    @Column(nullable = false)
    private Integer doseNumber; // Mũi thứ mấy (1, 2, 3, ...)
    
    @Column(nullable = true)
    private String injectionSite; // Vị trí tiêm (ví dụ: "LEFT_ARM", "RIGHT_ARM")
    
    @Column(nullable = false)
    private String batchNumber; // Số lô vaccine (từ VaccineLot)
    
    @Column(nullable = true)
    private Double doseAmount; // Liều lượng (ví dụ: 0.5ml)
    
    @Column(nullable = true, unique = true)
    private String certificateNumber; // Số chứng nhận tiêm chủng (duy nhất)
    
    @Column(nullable = true)
    private LocalDate nextDoseDate; // Ngày tiêm mũi tiếp theo (nếu có)
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo hồ sơ
    
    // Relationships
    @OneToMany(mappedBy = "vaccinationRecord")
    private List<AdverseReaction> adverseReactions; // Danh sách phản ứng phụ (nếu có)
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Appointment getAppointment() {
        return appointment;
    }
    
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Vaccine getVaccine() {
        return vaccine;
    }
    
    public void setVaccine(Vaccine vaccine) {
        this.vaccine = vaccine;
    }
    
    public VaccineLot getVaccineLot() {
        return vaccineLot;
    }
    
    public void setVaccineLot(VaccineLot vaccineLot) {
        this.vaccineLot = vaccineLot;
    }
    
    public User getNurse() {
        return nurse;
    }
    
    public void setNurse(User nurse) {
        this.nurse = nurse;
    }
    
    public LocalDate getInjectionDate() {
        return injectionDate;
    }
    
    public void setInjectionDate(LocalDate injectionDate) {
        this.injectionDate = injectionDate;
    }
    
    public LocalTime getInjectionTime() {
        return injectionTime;
    }
    
    public void setInjectionTime(LocalTime injectionTime) {
        this.injectionTime = injectionTime;
    }
    
    public Integer getDoseNumber() {
        return doseNumber;
    }
    
    public void setDoseNumber(Integer doseNumber) {
        this.doseNumber = doseNumber;
    }
    
    public String getInjectionSite() {
        return injectionSite;
    }
    
    public void setInjectionSite(String injectionSite) {
        this.injectionSite = injectionSite;
    }
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
    
    public Double getDoseAmount() {
        return doseAmount;
    }
    
    public void setDoseAmount(Double doseAmount) {
        this.doseAmount = doseAmount;
    }
    
    public String getCertificateNumber() {
        return certificateNumber;
    }
    
    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }
    
    public LocalDate getNextDoseDate() {
        return nextDoseDate;
    }
    
    public void setNextDoseDate(LocalDate nextDoseDate) {
        this.nextDoseDate = nextDoseDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<AdverseReaction> getAdverseReactions() {
        return adverseReactions;
    }
    
    public void setAdverseReactions(List<AdverseReaction> adverseReactions) {
        this.adverseReactions = adverseReactions;
    }
}

