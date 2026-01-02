package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clinic_rooms")
public class ClinicRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "center_id", nullable = false)
    private VaccinationCenter center; // Trung tâm y tế chứa phòng này
    
    @Column(nullable = false)
    private String roomNumber; // Số phòng (ví dụ: "P101", "P102")
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String description; // Mô tả phòng (ví dụ: "Phòng tiêm chủng số 1")
    
    @Column(nullable = false)
    private Boolean isActive; // Phòng có đang hoạt động không
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo
    
    // Relationships
    @OneToMany(mappedBy = "room")
    private List<Appointment> appointments; // Danh sách lịch hẹn sử dụng phòng này
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public VaccinationCenter getCenter() {
        return center;
    }
    
    public void setCenter(VaccinationCenter center) {
        this.center = center;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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


