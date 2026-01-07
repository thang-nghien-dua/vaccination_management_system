package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.CenterVaccineDTO;
import ut.edu.vaccinationmanagementsystem.entity.CenterVaccine;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;
import ut.edu.vaccinationmanagementsystem.repository.CenterVaccineRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationCenterRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccineRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CenterVaccineService {
    
    @Autowired
    private CenterVaccineRepository centerVaccineRepository;
    
    @Autowired
    private VaccinationCenterRepository vaccinationCenterRepository;
    
    @Autowired
    private VaccineRepository vaccineRepository;
    
    //Lấy danh sách vaccine có tại trung tâm
    public List<CenterVaccine> getVaccinesByCenterId(Long centerId) {
        return centerVaccineRepository.findByCenterId(centerId);
    }
    
    //Lấy chi tiết center vaccine theo ID
    public CenterVaccine getCenterVaccineById(Long id) {
        return centerVaccineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Center vaccine not found with id: " + id));
    }
    
    //Thêm vaccine vào trung tâm
    public CenterVaccine addVaccineToCenter(Long centerId, CenterVaccineDTO dto) {
        // Validate các field bắt buộc
        if (dto.getVaccineId() == null) {
            throw new RuntimeException("Vaccine ID is required");
        }
        
        // Kiểm tra center có tồn tại không
        VaccinationCenter center = vaccinationCenterRepository.findById(centerId)
                .orElseThrow(() -> new RuntimeException("Vaccination center not found with id: " + centerId));
        
        // Kiểm tra vaccine có tồn tại không
        Vaccine vaccine = vaccineRepository.findById(dto.getVaccineId())
                .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + dto.getVaccineId()));
        
        // Kiểm tra đã có vaccine này tại trung tâm chưa
        if (centerVaccineRepository.existsByCenterAndVaccine(center, vaccine)) {
            throw new RuntimeException("Vaccine already exists in this center");
        }
        
        // Convert DTO sang Entity
        CenterVaccine centerVaccine = new CenterVaccine();
        centerVaccine.setCenter(center);
        centerVaccine.setVaccine(vaccine);
        centerVaccine.setStockQuantity(dto.getStockQuantity());
        centerVaccine.setLastRestocked(java.time.LocalDateTime.now());
        
        try {
            return centerVaccineRepository.save(centerVaccine);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save center vaccine: " + e.getMessage(), e);
        }
    }
    
    //Cập nhật số lượng vaccine tại trung tâm
    public CenterVaccine updateCenterVaccine(Long id, CenterVaccineDTO dto) {
        CenterVaccine centerVaccine = getCenterVaccineById(id);
        
        // Nếu đổi vaccine, kiểm tra vaccine mới
        if (dto.getVaccineId() != null && !centerVaccine.getVaccine().getId().equals(dto.getVaccineId())) {
            Vaccine vaccine = vaccineRepository.findById(dto.getVaccineId())
                    .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + dto.getVaccineId()));
            
            // Kiểm tra vaccine mới đã có tại trung tâm này chưa
            VaccinationCenter center = centerVaccine.getCenter();
            if (centerVaccineRepository.existsByCenterAndVaccine(center, vaccine)) {
                Optional<CenterVaccine> existing = centerVaccineRepository.findByCenterAndVaccine(center, vaccine);
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    throw new RuntimeException("Vaccine already exists in this center");
                }
            }
            centerVaccine.setVaccine(vaccine);
        }
        
        // Cập nhật số lượng
        if (dto.getStockQuantity() != null) {
            centerVaccine.setStockQuantity(dto.getStockQuantity());
            centerVaccine.setLastRestocked(java.time.LocalDateTime.now());
        }
        
        return centerVaccineRepository.save(centerVaccine);
    }
    
    //Xóa vaccine khỏi trung tâm
    public void deleteCenterVaccine(Long id) {
        CenterVaccine centerVaccine = getCenterVaccineById(id);
        centerVaccineRepository.delete(centerVaccine);
    }
    
    //Kiểm tra center vaccine có tồn tại không
    public boolean existsById(Long id) {
        return centerVaccineRepository.existsById(id);
    }
}


