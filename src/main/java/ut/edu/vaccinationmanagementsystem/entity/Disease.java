package ut.edu.vaccinationmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

/**
 * Loại bệnh - Danh mục các bệnh có thể phòng ngừa bằng vaccine
 */
@Entity
@Table(name = "diseases")
public class Disease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @Column(unique = true, nullable = false, length = 50)
    private String code; // Mã bệnh (ví dụ: "FLU", "COVID19", "HEPATITIS_B")
    
    @Column(nullable = false)
    private String name; // Tên bệnh (ví dụ: "Cúm mùa", "COVID-19", "Viêm gan B")
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String description; // Mô tả về bệnh
    
    @Column(nullable = true, length = 500)
    private String iconUrl; // URL icon đại diện cho bệnh
    
    // Relationships
    @ManyToMany(mappedBy = "diseases")
    @JsonIgnore
    private List<Vaccine> vaccines; // Danh sách vaccine phòng bệnh này
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIconUrl() {
        return iconUrl;
    }
    
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    
    public List<Vaccine> getVaccines() {
        return vaccines;
    }
    
    public void setVaccines(List<Vaccine> vaccines) {
        this.vaccines = vaccines;
    }
}




