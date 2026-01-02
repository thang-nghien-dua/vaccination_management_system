package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Khai báo y tế - Thông tin khai báo trước khi tiêm
 */
@Entity
@Table(name = "medical_declarations")
public class MedicalDeclaration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true, nullable = false)
    private Appointment appointment; // Lịch hẹn liên kết (One-to-One)
    
    @Column(nullable = false)
    private Boolean hasAllergies; // Có tiền sử dị ứng không
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String allergyDetails; // Chi tiết dị ứng (nếu có)
    
    @Column(nullable = false)
    private Boolean isPregnant; // Đang mang thai không (cho nữ)
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String currentMedications; // Đang dùng thuốc gì
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String underlyingDiseases; // Bệnh nền (tim mạch, tiểu đường, ...)
    
    @Column(nullable = false)
    private Boolean hasFever; // Có sốt không
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String otherSymptoms; // Triệu chứng khác
    
    @Column(nullable = false)
    private LocalDateTime declaredAt; // Thời gian khai báo
    
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
    
    public Boolean getHasAllergies() {
        return hasAllergies;
    }
    
    public void setHasAllergies(Boolean hasAllergies) {
        this.hasAllergies = hasAllergies;
    }
    
    public String getAllergyDetails() {
        return allergyDetails;
    }
    
    public void setAllergyDetails(String allergyDetails) {
        this.allergyDetails = allergyDetails;
    }
    
    public Boolean getIsPregnant() {
        return isPregnant;
    }
    
    public void setIsPregnant(Boolean isPregnant) {
        this.isPregnant = isPregnant;
    }
    
    public String getCurrentMedications() {
        return currentMedications;
    }
    
    public void setCurrentMedications(String currentMedications) {
        this.currentMedications = currentMedications;
    }
    
    public String getUnderlyingDiseases() {
        return underlyingDiseases;
    }
    
    public void setUnderlyingDiseases(String underlyingDiseases) {
        this.underlyingDiseases = underlyingDiseases;
    }
    
    public Boolean getHasFever() {
        return hasFever;
    }
    
    public void setHasFever(Boolean hasFever) {
        this.hasFever = hasFever;
    }
    
    public String getOtherSymptoms() {
        return otherSymptoms;
    }
    
    public void setOtherSymptoms(String otherSymptoms) {
        this.otherSymptoms = otherSymptoms;
    }
    
    public LocalDateTime getDeclaredAt() {
        return declaredAt;
    }
    
    public void setDeclaredAt(LocalDateTime declaredAt) {
        this.declaredAt = declaredAt;
    }
}


