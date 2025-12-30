package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.DayOfWeek;

import java.time.LocalTime;

/**
 * Giờ làm việc của trung tâm y tế theo từng ngày trong tuần
 */
@Entity
@Table(name = "center_working_hours")
public class CenterWorkingHours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "center_id", nullable = false)
    private VaccinationCenter center; // Trung tâm y tế
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek; // Thứ trong tuần (MONDAY, TUESDAY, ...)
    
    @Column(nullable = false)
    private LocalTime startTime; // Giờ bắt đầu làm việc (ví dụ: 08:00)
    
    @Column(nullable = false)
    private LocalTime endTime; // Giờ kết thúc làm việc (ví dụ: 17:00)
    
    @Column(nullable = false)
    private Boolean isActive; // Có đang áp dụng giờ làm việc này không
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public VaccinationCenter getCenter() {
        return center;
    }
    
    public void setCenter(VaccinationCenter center) {
        this.center = center;
    }
    
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

