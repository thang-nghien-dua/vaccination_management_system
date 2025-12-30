package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.ShiftType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Lịch làm việc của nhân viên tại các trung tâm
 */
@Entity
@Table(name = "work_schedule")
public class WorkSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Nhân viên (RECEPTIONIST, DOCTOR)
    
    @ManyToOne
    @JoinColumn(name = "center_id", nullable = false)
    private VaccinationCenter center; // Trung tâm làm việc
    
    @Column(nullable = false)
    private LocalDate workDate; // Ngày làm việc
    
    @Column(nullable = false)
    private LocalTime startTime; // Giờ bắt đầu ca làm việc
    
    @Column(nullable = false)
    private LocalTime endTime; // Giờ kết thúc ca làm việc
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftType shiftType; // Loại ca (MORNING, AFTERNOON, FULL_DAY)
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo lịch làm việc
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public VaccinationCenter getCenter() {
        return center;
    }
    
    public void setCenter(VaccinationCenter center) {
        this.center = center;
    }
    
    public LocalDate getWorkDate() {
        return workDate;
    }
    
    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
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
    
    public ShiftType getShiftType() {
        return shiftType;
    }
    
    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

