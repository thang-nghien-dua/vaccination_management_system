package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.ScreeningResult;

import java.time.LocalDateTime;

/**
 * Khám sàng lọc - Kết quả khám sàng lọc trước khi tiêm
 */
@Entity
@Table(name = "screenings")
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true, nullable = false)
    private Appointment appointment; // Lịch hẹn liên kết (One-to-One)
    
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor; // Bác sĩ thực hiện khám sàng lọc
    
    @Column(nullable = true)
    private Double bodyTemperature; // Nhiệt độ cơ thể (ví dụ: 36.5)
    
    @Column(nullable = true)
    private String bloodPressure; // Huyết áp (ví dụ: "120/80")
    
    @Column(nullable = true)
    private Integer heartRate; // Nhịp tim (ví dụ: 72 bpm)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScreeningResult screeningResult; // Kết quả khám (APPROVED, REJECTED)
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String rejectionReason; // Lý do không đủ điều kiện (nếu REJECTED)
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String notes; // Ghi chú của bác sĩ
    
    @Column(nullable = false)
    private LocalDateTime screenedAt; // Thời gian khám sàng lọc
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public Appointment getAppointment() {
        return appointment;
    }
    
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }
    
    public User getDoctor() {
        return doctor;
    }
    
    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }
    
    public Double getBodyTemperature() {
        return bodyTemperature;
    }
    
    public void setBodyTemperature(Double bodyTemperature) {
        this.bodyTemperature = bodyTemperature;
    }
    
    public String getBloodPressure() {
        return bloodPressure;
    }
    
    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }
    
    public Integer getHeartRate() {
        return heartRate;
    }
    
    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }
    
    public ScreeningResult getScreeningResult() {
        return screeningResult;
    }
    
    public void setScreeningResult(ScreeningResult screeningResult) {
        this.screeningResult = screeningResult;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getScreenedAt() {
        return screenedAt;
    }
    
    public void setScreenedAt(LocalDateTime screenedAt) {
        this.screenedAt = screenedAt;
    }
}


