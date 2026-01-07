package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.VaccinationCenterDTO;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationCenterRepository;

import java.util.List;

/**
 * Service xử lý business logic cho VaccinationCenter
 */
@Service
@Transactional
public class VaccinationCenterService {
    
    @Autowired
    private VaccinationCenterRepository vaccinationCenterRepository;
    
    //Lấy tất cả danh sách trung tâm
    public List<VaccinationCenter> getAllCenters() {
        return vaccinationCenterRepository.findAll();
    }
    
    //Lấy trung tâm theo ID
    public VaccinationCenter getCenterById(Long id) {
        return vaccinationCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccination center not found with id: " + id));
    }
    
    //Tạo trung tâm mới từ DTO
    public VaccinationCenter createCenter(VaccinationCenterDTO dto) {
        // Validate các field bắt buộc
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("Center name is required");
        }
        if (dto.getStatus() == null) {
            throw new RuntimeException("Center status is required");
        }
        
        // Convert DTO sang Entity
        VaccinationCenter center = new VaccinationCenter();
        center.setName(dto.getName().trim());
        center.setAddress(dto.getAddress());
        center.setPhoneNumber(dto.getPhoneNumber());
        center.setEmail(dto.getEmail());
        center.setCapacity(dto.getCapacity());
        center.setStatus(dto.getStatus());
        center.setCreatedAt(java.time.LocalDateTime.now());
        
        try {
            return vaccinationCenterRepository.save(center);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save vaccination center: " + e.getMessage(), e);
        }
    }
    
    //Cập nhật trung tâm từ DTO
    public VaccinationCenter updateCenter(Long id, VaccinationCenterDTO dto) {
        VaccinationCenter center = getCenterById(id);
        
        // Validate name nếu có thay đổi
        if (dto.getName() != null && dto.getName().trim().isEmpty()) {
            throw new RuntimeException("Center name cannot be empty");
        }
        
        // Cập nhật các thông tin từ DTO
        if (dto.getName() != null) {
            center.setName(dto.getName().trim());
        }
        if (dto.getAddress() != null) {
            center.setAddress(dto.getAddress());
        }
        if (dto.getPhoneNumber() != null) {
            center.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getEmail() != null) {
            center.setEmail(dto.getEmail());
        }
        if (dto.getCapacity() != null) {
            center.setCapacity(dto.getCapacity());
        }
        if (dto.getStatus() != null) {
            center.setStatus(dto.getStatus());
        }
        
        return vaccinationCenterRepository.save(center);
    }
    
    //Xóa trung tâm
    public void deleteCenter(Long id) {
        VaccinationCenter center = getCenterById(id);
        vaccinationCenterRepository.delete(center);
    }
    
    //Kiểm tra trung tâm có tồn tại không
    public boolean existsById(Long id) {
        return vaccinationCenterRepository.existsById(id);
    }
}


