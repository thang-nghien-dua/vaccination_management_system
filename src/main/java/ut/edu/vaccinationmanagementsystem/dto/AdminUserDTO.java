package ut.edu.vaccinationmanagementsystem.dto;

import ut.edu.vaccinationmanagementsystem.entity.enums.Gender;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;

import java.time.LocalDate;

/**
 * DTO cho admin tạo/cập nhật user
 */
public class AdminUserDTO {
    private String email;
    private String password; // Chỉ cần khi tạo mới
    private String fullName;
    private String phoneNumber;
    private LocalDate dayOfBirth;
    private Gender gender;
    private String address;
    private String citizenId;
    private Role role;
    private UserStatus status;
    
    // Staff-related fields
    private Long centerId;
    private String employeeId;
    private String specialization;
    private String licenseNumber;
    private LocalDate hireDate;
    private String department;
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public LocalDate getDayOfBirth() {
        return dayOfBirth;
    }
    
    public void setDayOfBirth(LocalDate dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCitizenId() {
        return citizenId;
    }
    
    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public Long getCenterId() {
        return centerId;
    }
    
    public void setCenterId(Long centerId) {
        this.centerId = centerId;
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

