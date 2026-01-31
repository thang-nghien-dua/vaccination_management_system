package ut.edu.vaccinationmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.VaccineStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Thông tin vaccine
 */
@Entity
@Table(name = "vaccines")
public class Vaccine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @Column(nullable = false)
    private String name; // Tên vaccine (ví dụ: "Vaccine COVID-19")
    
    @Column(unique = true, nullable = false)
    private String code; // Mã vaccine (duy nhất, ví dụ: "COVID19-VN")
    
    @Column(nullable = true)
    private String manufacturer; // Nhà sản xuất (ví dụ: "Pfizer", "Moderna")
    
    @Column(nullable = true)
    private String origin; // Xuất xứ (ví dụ: "Mỹ", "Việt Nam")
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết về vaccine
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Giá vaccine
    
    @Column(nullable = true)
    private Integer minAge; // Độ tuổi tối thiểu được tiêm (ví dụ: 18)
    
    @Column(nullable = true)
    private Integer maxAge; // Độ tuổi tối đa được tiêm (ví dụ: 65, null = không giới hạn)
    
    @Column(nullable = false)
    private Integer dosesRequired; // Số mũi cần thiết (ví dụ: 2 mũi)
    
    @Column(nullable = true)
    private Integer daysBetweenDoses; // Khoảng cách giữa các mũi (ví dụ: 21 ngày)
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String contraindications; // Chống chỉ định (ví dụ: "Không tiêm cho phụ nữ mang thai")
    
    @Column(nullable = true)
    private String storageTemperature; // Nhiệt độ bảo quản (ví dụ: "2-8°C")
    
    @Column(nullable = true, length = 500)
    private String imageUrl; // URL hình ảnh vaccine
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VaccineStatus status; // Trạng thái (AVAILABLE, UNAVAILABLE, DISCONTINUED)
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo
    
    // Relationships
    @OneToMany(mappedBy = "vaccine")
    @JsonIgnore
    private List<VaccineLot> vaccineLots; // Danh sách lô vaccine
    
    @OneToMany(mappedBy = "vaccine")
    @JsonIgnore
    private List<VaccinationRecord> vaccinationRecords; // Lịch sử tiêm vaccine này
    
    @OneToMany(mappedBy = "vaccine")
    @JsonIgnore
    private List<Appointment> appointments; // Danh sách lịch hẹn tiêm vaccine này
    
    @ManyToMany(mappedBy = "vaccines")
    private List<VaccinationCenter> centers; // Danh sách trung tâm có vaccine này
    
    @ManyToMany
    @JoinTable(
        name = "vaccine_disease",
        joinColumns = @JoinColumn(name = "vaccine_id"),
        inverseJoinColumns = @JoinColumn(name = "disease_id")
    )
    private List<Disease> diseases; // Danh sách bệnh mà vaccine này phòng ngừa
    
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
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getMinAge() {
        return minAge;
    }
    
    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }
    
    public Integer getMaxAge() {
        return maxAge;
    }
    
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }
    
    public Integer getDosesRequired() {
        return dosesRequired;
    }
    
    public void setDosesRequired(Integer dosesRequired) {
        this.dosesRequired = dosesRequired;
    }
    
    public Integer getDaysBetweenDoses() {
        return daysBetweenDoses;
    }
    
    public void setDaysBetweenDoses(Integer daysBetweenDoses) {
        this.daysBetweenDoses = daysBetweenDoses;
    }
    
    public String getContraindications() {
        return contraindications;
    }
    
    public void setContraindications(String contraindications) {
        this.contraindications = contraindications;
    }
    
    public String getStorageTemperature() {
        return storageTemperature;
    }
    
    public void setStorageTemperature(String storageTemperature) {
        this.storageTemperature = storageTemperature;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public VaccineStatus getStatus() {
        return status;
    }
    
    public void setStatus(VaccineStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<VaccineLot> getVaccineLots() {
        return vaccineLots;
    }
    
    public void setVaccineLots(List<VaccineLot> vaccineLots) {
        this.vaccineLots = vaccineLots;
    }
    
    public List<VaccinationRecord> getVaccinationRecords() {
        return vaccinationRecords;
    }
    
    public void setVaccinationRecords(List<VaccinationRecord> vaccinationRecords) {
        this.vaccinationRecords = vaccinationRecords;
    }
    
    public List<Appointment> getAppointments() {
        return appointments;
    }
    
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
    
    public List<VaccinationCenter> getCenters() {
        return centers;
    }
    
    public void setCenters(List<VaccinationCenter> centers) {
        this.centers = centers;
    }
    
    public List<Disease> getDiseases() {
        return diseases;
    }
    
    public void setDiseases(List<Disease> diseases) {
        this.diseases = diseases;
    }
}


