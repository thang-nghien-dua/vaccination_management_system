package ut.edu.vaccinationmanagement_system.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagement_system.entity.enums.Gender;
import ut.edu.vaccinationmanagement_system.entity.enums.Relationship;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Danh sách người thân của user
 * Cho phép user đặt lịch tiêm cho người thân
 */
@Entity
@Table(name = "family_member")
public class FamilyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Chủ tài khoản (người quản lý danh sách người thân)
    
    @Column(nullable = false)
    private String fullName; // Họ và tên người thân
    
    @Column(nullable = true)
    private LocalDate dateOfBirth; // Ngày sinh người thân
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender; // Giới tính người thân
    
    @Column(nullable = true)
    private String citizenId; // CMND/CCCD của người thân
    
    @Column(nullable = true)
    private String phoneNumber; // Số điện thoại người thân
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Relationship relationship; // Quan hệ (CHILD, PARENT, SPOUSE, OTHER)
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian thêm vào danh sách
    
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
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public String getCitizenId() {
        return citizenId;
    }
    
    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Relationship getRelationship() {
        return relationship;
    }
    
    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

