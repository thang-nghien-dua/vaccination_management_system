package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.VaccineDTO;
import ut.edu.vaccinationmanagementsystem.entity.Disease;
import ut.edu.vaccinationmanagementsystem.entity.Promotion;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;
import ut.edu.vaccinationmanagementsystem.repository.CenterVaccineRepository;
import ut.edu.vaccinationmanagementsystem.repository.DiseaseRepository;
import ut.edu.vaccinationmanagementsystem.repository.PromotionRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationRecordRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccineLotRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccineRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationCenterRepository;
import ut.edu.vaccinationmanagementsystem.dto.CenterVaccineDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ut.edu.vaccinationmanagementsystem.entity.CenterVaccine;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;

/**
 * Service xử lý business logic cho Vaccine
 */
@Service
@Transactional
public class VaccineService {
    
    @Autowired
    private VaccineRepository vaccineRepository;
    
    @Autowired
    private DiseaseRepository diseaseRepository;
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private VaccineLotRepository vaccineLotRepository;
    
    @Autowired
    private CenterVaccineRepository centerVaccineRepository;
    
    @Autowired
    private VaccinationCenterRepository vaccinationCenterRepository;
    
    @Autowired
    private CenterVaccineService centerVaccineService;
    
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
        vaccine.setImageUrl(dto.getImageUrl());
        vaccine.setStatus(dto.getStatus());
        vaccine.setCreatedAt(java.time.LocalDateTime.now());
        
        try {
            Vaccine savedVaccine = vaccineRepository.save(vaccine);
            
            // Nếu có số lượng tồn kho, tự động tạo CenterVaccine cho trung tâm tổng (centerId = 1)
            if (dto.getStockQuantity() != null && dto.getStockQuantity() > 0) {
                try {
                    // Tìm trung tâm tổng (giả định là centerId = 1, hoặc trung tâm đầu tiên)
                    Long mainCenterId = 1L;
                    VaccinationCenter mainCenter = null;
                    
                    if (vaccinationCenterRepository.existsById(mainCenterId)) {
                        mainCenter = vaccinationCenterRepository.findById(mainCenterId).get();
                    } else {
                        // Nếu không có centerId = 1, lấy trung tâm đầu tiên
                        List<VaccinationCenter> centers = vaccinationCenterRepository.findAll();
                        if (!centers.isEmpty()) {
                            mainCenter = centers.get(0);
                            mainCenterId = mainCenter.getId();
                        }
                    }
                    
                    if (mainCenter != null) {
                        // Kiểm tra xem đã có vaccine này tại trung tâm tổng chưa
                        Optional<CenterVaccine> existing = centerVaccineRepository.findByCenterAndVaccine(mainCenter, savedVaccine);
                        
                        if (existing.isPresent()) {
                            // Nếu đã có, cập nhật số lượng (cộng thêm)
                            CenterVaccine cv = existing.get();
                            cv.setStockQuantity((cv.getStockQuantity() != null ? cv.getStockQuantity() : 0) + dto.getStockQuantity());
                            cv.setLastRestocked(java.time.LocalDateTime.now());
                            centerVaccineRepository.save(cv);
                        } else {
                            // Nếu chưa có, tạo mới
                            CenterVaccine centerVaccine = new CenterVaccine();
                            centerVaccine.setCenter(mainCenter);
                            centerVaccine.setVaccine(savedVaccine);
                            centerVaccine.setStockQuantity(dto.getStockQuantity());
                            centerVaccine.setLastRestocked(java.time.LocalDateTime.now());
                            centerVaccineRepository.save(centerVaccine);
                        }
                    }
                } catch (Exception e) {
                    // Log lỗi nhưng không throw để vaccine vẫn được tạo thành công
                    System.err.println("Warning: Failed to create center vaccine for main center: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return savedVaccine;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save vaccine: " + e.getMessage(), e);
        }
    }
    
    //Cập nhật vaccine từ DTO
    public Vaccine updateVaccine(Long id, VaccineDTO dto) {
        Vaccine vaccine = getVaccineById(id);

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

        // Kiểm tra nếu đổi mã code và mã mới đã tồn tại
        if (dto.getCode() != null && !vaccine.getCode().equals(dto.getCode().trim()) && 
            vaccineRepository.existsByCode(dto.getCode().trim())) {
            throw new RuntimeException("Vaccine code already exists: " + dto.getCode());
        }
        
        // Cập nhật các thông tin từ DTO
        vaccine.setName(dto.getName().trim());
        vaccine.setCode(dto.getCode().trim());
        vaccine.setManufacturer(dto.getManufacturer() != null ? dto.getManufacturer().trim() : null);
        vaccine.setOrigin(dto.getOrigin() != null ? dto.getOrigin().trim() : null);
        vaccine.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        vaccine.setPrice(dto.getPrice());
        vaccine.setMinAge(dto.getMinAge());
        vaccine.setMaxAge(dto.getMaxAge());
        vaccine.setDosesRequired(dto.getDosesRequired());
        vaccine.setDaysBetweenDoses(dto.getDaysBetweenDoses());
        vaccine.setContraindications(dto.getContraindications() != null ? dto.getContraindications().trim() : null);
        vaccine.setStorageTemperature(dto.getStorageTemperature() != null ? dto.getStorageTemperature().trim() : null);
        vaccine.setImageUrl(dto.getImageUrl() != null ? dto.getImageUrl().trim() : null);
        vaccine.setStatus(dto.getStatus());
        
        try {
            return vaccineRepository.save(vaccine);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update vaccine: " + e.getMessage(), e);
        }
    }
    
    //Xóa vaccine
    public void deleteVaccine(Long id) {
        Vaccine vaccine = getVaccineById(id);
        
        // Kiểm tra xem vaccine có đang được sử dụng không bằng cách query trực tiếp
        // Kiểm tra appointments
        long appointmentCount = appointmentRepository.findAll().stream()
                .filter(a -> a.getVaccine() != null && a.getVaccine().getId().equals(id))
                .count();
        if (appointmentCount > 0) {
            throw new RuntimeException("Không thể xóa vaccine này vì đang có " + appointmentCount + " lịch hẹn đang sử dụng vaccine này. Vui lòng đổi trạng thái vaccine thành DISCONTINUED thay vì xóa.");
        }
        
        // Kiểm tra vaccination records
        long recordCount = vaccinationRecordRepository.countByVaccine(vaccine);
        if (recordCount > 0) {
            throw new RuntimeException("Không thể xóa vaccine này vì đang có " + recordCount + " hồ sơ tiêm chủng đang sử dụng vaccine này. Vui lòng đổi trạng thái vaccine thành DISCONTINUED thay vì xóa.");
        }
        
        // Kiểm tra vaccine lots
        long lotCount = vaccineLotRepository.findByVaccineId(id).size();
        if (lotCount > 0) {
            throw new RuntimeException("Không thể xóa vaccine này vì đang có " + lotCount + " lô vaccine. Vui lòng đổi trạng thái vaccine thành DISCONTINUED thay vì xóa.");
        }
        
        // Kiểm tra center vaccines
        long centerVaccineCount = centerVaccineRepository.findByVaccineId(id).size();
        if (centerVaccineCount > 0) {
            throw new RuntimeException("Không thể xóa vaccine này vì đang có " + centerVaccineCount + " trung tâm đang sử dụng vaccine này. Vui lòng đổi trạng thái vaccine thành DISCONTINUED thay vì xóa.");
        }
        
        try {
            vaccineRepository.delete(vaccine);
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa vaccine: " + e.getMessage() + ". Vaccine này đang được sử dụng trong hệ thống. Vui lòng đổi trạng thái thành DISCONTINUED thay vì xóa.");
        }
    }
    
    //Kiểm tra vaccine có tồn tại không
    public boolean existsById(Long id) {
        return vaccineRepository.existsById(id);
    }
    
    //Lấy tất cả diseases
    public List<Disease> getAllDiseases() {
        return diseaseRepository.findAll();
    }
    
    //Lấy promotion đang hoạt động cho một vaccine
    public List<Promotion> getActivePromotionsForVaccine(Long vaccineId) {
        return promotionRepository.findActivePromotionsByVaccine(vaccineId, LocalDateTime.now());
    }
    
    //Tính số lượng đã tiêm cho một vaccine (để xác định "bán chạy nhất")
    public long getVaccinationCountForVaccine(Long vaccineId) {
        Vaccine vaccine = getVaccineById(vaccineId);
        return vaccinationRecordRepository.countByVaccine(vaccine);
    }
    
    //Lấy map vaccineId -> vaccinationCount để tính "bán chạy nhất"
    public Map<Long, Long> getVaccinationCountMap() {
        Map<Long, Long> countMap = new HashMap<>();
        List<Object[]> results = vaccinationRecordRepository.findVaccinesWithVaccinationCount();
        for (Object[] result : results) {
            Vaccine vaccine = (Vaccine) result[0];
            Long count = (Long) result[1];
            countMap.put(vaccine.getId(), count);
        }
        return countMap;
    }
}

