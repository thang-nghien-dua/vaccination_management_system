package ut.edu.vaccinationmanagement_system.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagement_system.entity.enums.AppointmentStatus;

import java.time.LocalDateTime;

/**
 * Lịch sử thay đổi trạng thái của lịch hẹn (Audit log)
 */
@Entity
@Table(name = "appointment_history")
public class AppointmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment; // Lịch hẹn liên quan
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private AppointmentStatus oldStatus; // Trạng thái cũ (null nếu là lần đầu tạo)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus newStatus; // Trạng thái mới
    
    @ManyToOne
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy; // Người thay đổi trạng thái (user, receptionist, doctor, admin)
    
    @Column(nullable = false)
    private LocalDateTime changedAt; // Thời gian thay đổi
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String reason; // Lý do thay đổi trạng thái
    
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
    
    public AppointmentStatus getOldStatus() {
        return oldStatus;
    }
    
    public void setOldStatus(AppointmentStatus oldStatus) {
        this.oldStatus = oldStatus;
    }
    
    public AppointmentStatus getNewStatus() {
        return newStatus;
    }
    
    public void setNewStatus(AppointmentStatus newStatus) {
        this.newStatus = newStatus;
    }
    
    public User getChangedBy() {
        return changedBy;
    }
    
    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}

