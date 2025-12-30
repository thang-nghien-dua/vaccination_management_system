package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Khung giờ đặt lịch - Mỗi slot có giới hạn số người
 */
@Entity
@Table(name = "appointment_slot")
public class AppointmentSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "center_id", nullable = false)
    private VaccinationCenter center; // Trung tâm y tế
    
    @Column(nullable = false)
    private LocalDate date; // Ngày của slot (ví dụ: 2024-12-16)
    
    @Column(nullable = false)
    private LocalTime startTime; // Giờ bắt đầu (ví dụ: 08:00)
    
    @Column(nullable = false)
    private LocalTime endTime; // Giờ kết thúc (ví dụ: 08:30)
    
    @Column(nullable = false)
    private Integer maxCapacity; // Số người tối đa trong slot này (ví dụ: 10 người)
    
    @Column(nullable = false)
    private Integer currentBookings; // Số người đã đặt trong slot này
    
    @Column(nullable = false)
    private Boolean isAvailable; // Slot có còn khả dụng không
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo slot
    
    // Relationships
    @OneToMany(mappedBy = "slot")
    private List<Appointment> appointments; // Danh sách lịch hẹn trong slot này
    
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
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
    
    public Integer getMaxCapacity() {
        return maxCapacity;
    }
    
    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    
    public Integer getCurrentBookings() {
        return currentBookings;
    }
    
    public void setCurrentBookings(Integer currentBookings) {
        this.currentBookings = currentBookings;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<Appointment> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}

