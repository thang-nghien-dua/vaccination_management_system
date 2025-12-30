package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Thông tin bổ sung cho nhân viên (RECEPTIONIST, DOCTOR, ADMIN)
 * Chỉ có khi User có role là nhân viên
 */
@Entity
@Table(name = "staff_info")
public class StaffInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user; // User liên kết (One-to-One với User)
    
    @Column(unique = true, nullable = false)
    private String employeeId; // Mã nhân viên (duy nhất, bắt buộc)
    
    @Column(nullable = true)
    private String specialization; // Chuyên khoa (cho bác sĩ, ví dụ: "Nhi khoa", "Y tế công cộng")
    
    @Column(nullable = true)
    private String licenseNumber; // Số chứng chỉ hành nghề (cho bác sĩ)
    
    @Column(nullable = true)
    private LocalDate hireDate; // Ngày vào làm
    
    @Column(nullable = true)
    private String department; // Phòng ban
    
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
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public LocalDate getHireDate() {
        return hireDate;
    }
    
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
}

