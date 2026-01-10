package ut.edu.vaccinationmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.CenterStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Trung tâm y tế - Nơi thực hiện tiêm chủng
 */
@Entity
@Table(name = "vaccination_centers")
public class VaccinationCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @Column(nullable = false)
    private String name; // Tên trung tâm (ví dụ: "Trung tâm Y tế Quận 1")
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String address; // Địa chỉ trung tâm
    
    @Column(nullable = true)
    private String phoneNumber; // Số điện thoại liên hệ
    
    @Column(nullable = true)
    private String email; // Email liên hệ
    
    @Column(nullable = true)
    private Integer capacity; // Sức chứa tối đa số người tiêm/ngày
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CenterStatus status; // Trạng thái (ACTIVE, INACTIVE)
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo
    
    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "center")
    private List<AppointmentSlot> appointmentSlots; // Danh sách slot đặt lịch
    
    @JsonIgnore
    @OneToMany(mappedBy = "center")
    private List<Appointment> appointments; // Danh sách lịch hẹn tại trung tâm này
    
    @JsonIgnore
    @OneToMany(mappedBy = "center")
    private List<WorkSchedule> workSchedules; // Lịch làm việc của nhân viên tại trung tâm
    
    @JsonIgnore
    @OneToMany(mappedBy = "center")
    private List<CenterWorkingHours> workingHours; // Giờ làm việc của trung tâm
    
    @JsonIgnore
    @OneToMany(mappedBy = "center")
    private List<ClinicRoom> clinicRooms; // Danh sách phòng khám của trung tâm
    
    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "center_vaccine",
        joinColumns = @JoinColumn(name = "center_id"),
        inverseJoinColumns = @JoinColumn(name = "vaccine_id")
    )
    private List<Vaccine> vaccines; // Danh sách vaccine có tại trung tâm này
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public CenterStatus getStatus() {
        return status;
    }
    
    public void setStatus(CenterStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<AppointmentSlot> getAppointmentSlots() {
        return appointmentSlots;
    }
    
    public void setAppointmentSlots(List<AppointmentSlot> appointmentSlots) {
        this.appointmentSlots = appointmentSlots;
    }
    
    public List<Appointment> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
    
    public List<WorkSchedule> getWorkSchedules() {
        return workSchedules;
    }
    
    public void setWorkSchedules(List<WorkSchedule> workSchedules) {
        this.workSchedules = workSchedules;
    }
    
    public List<CenterWorkingHours> getWorkingHours() {
        return workingHours;
    }
    
    public void setWorkingHours(List<CenterWorkingHours> workingHours) {
        this.workingHours = workingHours;
    }
    
    public List<ClinicRoom> getClinicRooms() {
        return clinicRooms;
    }
    
    public void setClinicRooms(List<ClinicRoom> clinicRooms) {
        this.clinicRooms = clinicRooms;
    }
    
    public List<Vaccine> getVaccines() {
        return vaccines;
    }
    
    public void setVaccines(List<Vaccine> vaccines) {
        this.vaccines = vaccines;
    }
}


