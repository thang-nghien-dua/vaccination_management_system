package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.CenterWorkingHoursDTO;
import ut.edu.vaccinationmanagementsystem.entity.CenterWorkingHours;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.repository.CenterWorkingHoursRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationCenterRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CenterWorkingHoursService {
    
    @Autowired
    private CenterWorkingHoursRepository centerWorkingHoursRepository;
    
    @Autowired
    private VaccinationCenterRepository vaccinationCenterRepository;
    
    public List<CenterWorkingHours> getAllWorkingHours() {
        return centerWorkingHoursRepository.findAll();
    }
    
    public CenterWorkingHours getWorkingHoursById(Long id) {
        return centerWorkingHoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Working hours not found with id: " + id));
    }
    
    public List<CenterWorkingHours> getWorkingHoursByCenterId(Long centerId) {
        return centerWorkingHoursRepository.findByCenterId(centerId);
    }
    
    public CenterWorkingHours createWorkingHours(CenterWorkingHoursDTO dto) {
        // Validate các field bắt buộc
        if (dto.getCenterId() == null) {
            throw new RuntimeException("Center ID is required");
        }
        if (dto.getDayOfWeek() == null) {
            throw new RuntimeException("Day of week is required");
        }
        if (dto.getStartTime() == null) {
            throw new RuntimeException("Start time is required");
        }
        if (dto.getEndTime() == null) {
            throw new RuntimeException("End time is required");
        }
        if (dto.getIsActive() == null) {
            throw new RuntimeException("Is active is required");
        }
        
        // Validate startTime < endTime
        if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().equals(dto.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }
        
        // Kiểm tra center có tồn tại không
        VaccinationCenter center = vaccinationCenterRepository.findById(dto.getCenterId())
                .orElseThrow(() -> new RuntimeException("Vaccination center not found with id: " + dto.getCenterId()));
        
        // Kiểm tra đã có giờ làm việc cho center và dayOfWeek này chưa
        if (centerWorkingHoursRepository.existsByCenterAndDayOfWeek(center, dto.getDayOfWeek())) {
            throw new RuntimeException("Working hours already exist for this center and day: " + dto.getDayOfWeek());
        }
        
        // Convert DTO sang Entity
        CenterWorkingHours workingHours = new CenterWorkingHours();
        workingHours.setCenter(center);
        workingHours.setDayOfWeek(dto.getDayOfWeek());
        workingHours.setStartTime(dto.getStartTime());
        workingHours.setEndTime(dto.getEndTime());
        workingHours.setIsActive(dto.getIsActive());
        
        try {
            return centerWorkingHoursRepository.save(workingHours);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save working hours: " + e.getMessage(), e);
        }
    }
    
    public CenterWorkingHours updateWorkingHours(Long id, CenterWorkingHoursDTO dto) {
        CenterWorkingHours workingHours = getWorkingHoursById(id);
        
        // Validate startTime < endTime
        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (dto.getStartTime().isAfter(dto.getEndTime()) || dto.getStartTime().equals(dto.getEndTime())) {
                throw new RuntimeException("Start time must be before end time");
            }
        }
        
        // Nếu đổi center, validate center mới
        if (dto.getCenterId() != null && !workingHours.getCenter().getId().equals(dto.getCenterId())) {
            VaccinationCenter center = vaccinationCenterRepository.findById(dto.getCenterId())
                    .orElseThrow(() -> new RuntimeException("Vaccination center not found with id: " + dto.getCenterId()));
            workingHours.setCenter(center);
        }
        
        // Nếu đổi dayOfWeek, kiểm tra trùng
        if (dto.getDayOfWeek() != null && !workingHours.getDayOfWeek().equals(dto.getDayOfWeek())) {
            VaccinationCenter center = workingHours.getCenter();
            if (centerWorkingHoursRepository.existsByCenterAndDayOfWeek(center, dto.getDayOfWeek())) {
                // Kiểm tra xem có phải chính record này không (khi update cùng record)
                Optional<CenterWorkingHours> existing = centerWorkingHoursRepository.findByCenterAndDayOfWeek(center, dto.getDayOfWeek());
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    throw new RuntimeException("Working hours already exist for this center and day: " + dto.getDayOfWeek());
                }
            }
            workingHours.setDayOfWeek(dto.getDayOfWeek());
        }
        
        // Cập nhật các thông tin từ DTO
        if (dto.getStartTime() != null) {
            workingHours.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            workingHours.setEndTime(dto.getEndTime());
        }
        if (dto.getIsActive() != null) {
            workingHours.setIsActive(dto.getIsActive());
        }
        
        return centerWorkingHoursRepository.save(workingHours);
    }
    
    public void deleteWorkingHours(Long id) {
        CenterWorkingHours workingHours = getWorkingHoursById(id);
        centerWorkingHoursRepository.delete(workingHours);
    }
    
    public boolean existsById(Long id) {
        return centerWorkingHoursRepository.existsById(id);
    }
}

