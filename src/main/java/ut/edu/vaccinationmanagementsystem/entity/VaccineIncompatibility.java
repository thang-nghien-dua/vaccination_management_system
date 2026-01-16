package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Bảng lưu trữ các vaccine không tương thích và thời gian cách nhau tối thiểu
 */
@Entity
@Table(name = "vaccine_incompatibility")
public class VaccineIncompatibility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "vaccine1_id", nullable = false)
    private Vaccine vaccine1;
    
    @ManyToOne
    @JoinColumn(name = "vaccine2_id", nullable = false)
    private Vaccine vaccine2;
    
    @Column(nullable = false)
    private Integer minDaysBetween; // Số ngày tối thiểu giữa 2 vaccine
    
    @Column(columnDefinition = "TEXT")
    private String reason; // Lý do không tương thích
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Vaccine getVaccine1() {
        return vaccine1;
    }
    
    public void setVaccine1(Vaccine vaccine1) {
        this.vaccine1 = vaccine1;
    }
    
    public Vaccine getVaccine2() {
        return vaccine2;
    }
    
    public void setVaccine2(Vaccine vaccine2) {
        this.vaccine2 = vaccine2;
    }
    
    public Integer getMinDaysBetween() {
        return minDaysBetween;
    }
    
    public void setMinDaysBetween(Integer minDaysBetween) {
        this.minDaysBetween = minDaysBetween;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


