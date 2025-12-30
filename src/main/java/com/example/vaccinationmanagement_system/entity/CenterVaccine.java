package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Bảng trung gian quản lý vaccine có tại từng trung tâm
 * Một trung tâm có thể có nhiều vaccine, một vaccine có thể có ở nhiều trung tâm
 */
@Entity
@Table(name = "center_vaccine", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"center_id", "vaccine_id"}))
public class CenterVaccine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "center_id", nullable = false)
    private VaccinationCenter center; // Trung tâm y tế
    
    @ManyToOne
    @JoinColumn(name = "vaccine_id", nullable = false)
    private Vaccine vaccine; // Vaccine
    
    @Column(nullable = true)
    private Integer stockQuantity; // Số lượng vaccine tồn kho tại trung tâm này
    
    @Column(nullable = true)
    private LocalDateTime lastRestocked; // Thời gian nhập kho lần cuối
    
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
    
    public Vaccine getVaccine() {
        return vaccine;
    }
    
    public void setVaccine(Vaccine vaccine) {
        this.vaccine = vaccine;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public LocalDateTime getLastRestocked() {
        return lastRestocked;
    }
    
    public void setLastRestocked(LocalDateTime lastRestocked) {
        this.lastRestocked = lastRestocked;
    }
}

