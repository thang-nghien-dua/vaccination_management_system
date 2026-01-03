package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.VaccineDTO;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;
import ut.edu.vaccinationmanagementsystem.repository.VaccineRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service xử lý business logic cho Vaccine
 */
@Service
@Transactional
public class VaccineService {
    
    @Autowired
    private VaccineRepository vaccineRepository;
    
    //Lấy tất cả danh sách vaccine
    public List<Vaccine> getAllVaccines() {
        return vaccineRepository.findAll();
    }
    
    //Lấy vaccine theo ID
    public Vaccine getVaccineById(Long id) {
        return vaccineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + id));
    }
    

    //Tìm kiếm vaccine theo từ khóa
    public List<Vaccine> searchVaccines(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVaccines();
        }
        return vaccineRepository.searchByKeyword(keyword.trim());
    }
    

    //Tạo vaccine mới từ DTO
    public Vaccine createVaccine(VaccineDTO dto) {
        // Validate các field bắt buộc
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("Vaccine name is required");
        }
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new RuntimeException("Vaccine code is required");
        }
        if (dto.getPrice() == null) {
            throw new RuntimeException("Vaccine price is required");
        }
        if (dto.getDosesRequired() == null) {
            throw new RuntimeException("Doses required is required");
        }
        if (dto.getStatus() == null) {
            throw new RuntimeException("Vaccine status is required");
        }
        
        // Kiểm tra mã vaccine đã tồn tại chưa
        if (vaccineRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Vaccine code already exists: " + dto.getCode());
        }
        
        // Convert DTO sang Entity
        Vaccine vaccine = new Vaccine();
        vaccine.setName(dto.getName().trim());
        vaccine.setCode(dto.getCode().trim());
        vaccine.setManufacturer(dto.getManufacturer());
        vaccine.setOrigin(dto.getOrigin());
        vaccine.setDescription(dto.getDescription());
        vaccine.setPrice(dto.getPrice());
        vaccine.setMinAge(dto.getMinAge());
        vaccine.setMaxAge(dto.getMaxAge());
        vaccine.setDosesRequired(dto.getDosesRequired());
        vaccine.setDaysBetweenDoses(dto.getDaysBetweenDoses());
        vaccine.setContraindications(dto.getContraindications());
        vaccine.setStorageTemperature(dto.getStorageTemperature());
        vaccine.setStatus(dto.getStatus());
        vaccine.setCreatedAt(java.time.LocalDateTime.now());
        
        try {
            return vaccineRepository.save(vaccine);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save vaccine: " + e.getMessage(), e);
        }
    }
    
    //Cập nhật vaccine từ DTO
    public Vaccine updateVaccine(Long id, VaccineDTO dto) {
        Vaccine vaccine = getVaccineById(id);

        // Kiểm tra nếu đổi mã code và mã mới đã tồn tại
        if (!vaccine.getCode().equals(dto.getCode()) && 
            vaccineRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Vaccine code already exists: " + dto.getCode());
        }
        
        // Cập nhật các thông tin từ DTO
        vaccine.setName(dto.getName());
        vaccine.setCode(dto.getCode());
        vaccine.setManufacturer(dto.getManufacturer());
        vaccine.setOrigin(dto.getOrigin());
        vaccine.setDescription(dto.getDescription());
        vaccine.setPrice(dto.getPrice());
        vaccine.setMinAge(dto.getMinAge());
        vaccine.setMaxAge(dto.getMaxAge());
        vaccine.setDosesRequired(dto.getDosesRequired());
        vaccine.setDaysBetweenDoses(dto.getDaysBetweenDoses());
        vaccine.setContraindications(dto.getContraindications());
        vaccine.setStorageTemperature(dto.getStorageTemperature());
        vaccine.setStatus(dto.getStatus());
        
        return vaccineRepository.save(vaccine);
    }
    
    //Xóa vaccine
    public void deleteVaccine(Long id) {
        Vaccine vaccine = getVaccineById(id);
        vaccineRepository.delete(vaccine);
    }
    
    //Kiểm tra vaccine có tồn tại không
    public boolean existsById(Long id) {
        return vaccineRepository.existsById(id);
    }
}

