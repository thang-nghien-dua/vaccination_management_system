package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.VaccineLotDTO;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;
import ut.edu.vaccinationmanagementsystem.entity.VaccineLot;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.entity.CenterVaccine;
import ut.edu.vaccinationmanagementsystem.entity.enums.VaccineLotStatus;
import ut.edu.vaccinationmanagementsystem.repository.VaccineLotRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccineRepository;
import ut.edu.vaccinationmanagementsystem.repository.CenterVaccineRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationCenterRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service xử lý business logic cho VaccineLot
 */
@Service
@Transactional
public class VaccineLotService {
    
    @Autowired
    private VaccineLotRepository vaccineLotRepository;
    
    @Autowired
    private VaccineRepository vaccineRepository;
    
    // Lấy tất cả danh sách lô vaccine
    public List<VaccineLot> getAllVaccineLots() {
        return vaccineLotRepository.findAll();
    }
    
    // Lấy lô vaccine theo ID
    public VaccineLot getVaccineLotById(Long id) {
        return vaccineLotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccine lot not found with id: " + id));
    }
    
    // Lấy lô vaccine theo số lô
    public VaccineLot getVaccineLotByLotNumber(String lotNumber) {
        return vaccineLotRepository.findByLotNumber(lotNumber)
                .orElseThrow(() -> new RuntimeException("Vaccine lot not found with lot number: " + lotNumber));
    }
    
    // Lấy tất cả lô vaccine theo vaccine ID
    public List<VaccineLot> getVaccineLotsByVaccineId(Long vaccineId) {
        return vaccineLotRepository.findByVaccineId(vaccineId);
    }
    
    // Tìm kiếm lô vaccine theo từ khóa
    public List<VaccineLot> searchVaccineLots(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVaccineLots();
        }
        return vaccineLotRepository.searchByKeyword(keyword.trim());
    }
    
    // Tạo lô vaccine mới từ DTO
    public VaccineLot createVaccineLot(VaccineLotDTO dto) {
        // Validate các field bắt buộc
        if (dto.getLotNumber() == null || dto.getLotNumber().trim().isEmpty()) {
            throw new RuntimeException("Lot number is required");
        }
        if (dto.getVaccineId() == null) {
            throw new RuntimeException("Vaccine ID is required");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
        if (dto.getManufacturingDate() == null) {
            throw new RuntimeException("Manufacturing date is required");
        }
        if (dto.getExpiryDate() == null) {
            throw new RuntimeException("Expiry date is required");
        }
        if (dto.getImportDate() == null) {
            throw new RuntimeException("Import date is required");
        }
        if (dto.getStatus() == null) {
            throw new RuntimeException("Status is required");
        }
        
        // Kiểm tra ngày hết hạn phải sau ngày sản xuất
        if (dto.getExpiryDate().isBefore(dto.getManufacturingDate())) {
            throw new RuntimeException("Expiry date must be after manufacturing date");
        }
        
        // Kiểm tra số lô đã tồn tại chưa
        if (vaccineLotRepository.existsByLotNumber(dto.getLotNumber().trim())) {
            throw new RuntimeException("Lot number already exists: " + dto.getLotNumber());
        }
        
        // Kiểm tra vaccine có tồn tại không
        Vaccine vaccine = vaccineRepository.findById(dto.getVaccineId())
                .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + dto.getVaccineId()));
        
        // Validate remainingQuantity
        Integer remainingQuantity = dto.getRemainingQuantity();
        if (remainingQuantity == null) {
            remainingQuantity = dto.getQuantity(); // Mặc định bằng quantity khi tạo mới
        }
        if (remainingQuantity < 0 || remainingQuantity > dto.getQuantity()) {
            throw new RuntimeException("Remaining quantity must be between 0 and quantity");
        }
        
        // Convert DTO sang Entity
        VaccineLot vaccineLot = new VaccineLot();
        vaccineLot.setLotNumber(dto.getLotNumber().trim());
        vaccineLot.setVaccine(vaccine);
        vaccineLot.setQuantity(dto.getQuantity());
        vaccineLot.setRemainingQuantity(remainingQuantity);
        vaccineLot.setManufacturingDate(dto.getManufacturingDate());
        vaccineLot.setExpiryDate(dto.getExpiryDate());
        vaccineLot.setSupplier(dto.getSupplier());
        vaccineLot.setImportDate(dto.getImportDate());
        vaccineLot.setStatus(dto.getStatus());
        vaccineLot.setCreatedAt(LocalDateTime.now());
        
        // Tự động cập nhật status nếu cần
        updateLotStatusIfNeeded(vaccineLot);
        
        try {
            return vaccineLotRepository.save(vaccineLot);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save vaccine lot: " + e.getMessage(), e);
        }
    }
    
    // Cập nhật lô vaccine từ DTO
    public VaccineLot updateVaccineLot(Long id, VaccineLotDTO dto) {
        VaccineLot vaccineLot = getVaccineLotById(id);
        
        // Kiểm tra nếu đổi số lô và số lô mới đã tồn tại
        if (!vaccineLot.getLotNumber().equals(dto.getLotNumber()) && 
            vaccineLotRepository.existsByLotNumber(dto.getLotNumber())) {
            throw new RuntimeException("Lot number already exists: " + dto.getLotNumber());
        }
        
        // Validate
        if (dto.getQuantity() != null && dto.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }
        if (dto.getExpiryDate() != null && dto.getManufacturingDate() != null &&
            dto.getExpiryDate().isBefore(dto.getManufacturingDate())) {
            throw new RuntimeException("Expiry date must be after manufacturing date");
        }
        
        // Cập nhật vaccine nếu có thay đổi
        if (dto.getVaccineId() != null && !vaccineLot.getVaccine().getId().equals(dto.getVaccineId())) {
            Vaccine vaccine = vaccineRepository.findById(dto.getVaccineId())
                    .orElseThrow(() -> new RuntimeException("Vaccine not found with id: " + dto.getVaccineId()));
            vaccineLot.setVaccine(vaccine);
        }
        
        // Cập nhật các thông tin từ DTO
        if (dto.getLotNumber() != null) {
            vaccineLot.setLotNumber(dto.getLotNumber().trim());
        }
        if (dto.getQuantity() != null) {
            vaccineLot.setQuantity(dto.getQuantity());
        }
        if (dto.getRemainingQuantity() != null) {
            if (dto.getRemainingQuantity() < 0 || 
                (vaccineLot.getQuantity() != null && dto.getRemainingQuantity() > vaccineLot.getQuantity())) {
                throw new RuntimeException("Remaining quantity must be between 0 and quantity");
            }
            vaccineLot.setRemainingQuantity(dto.getRemainingQuantity());
        }
        if (dto.getManufacturingDate() != null) {
            vaccineLot.setManufacturingDate(dto.getManufacturingDate());
        }
        if (dto.getExpiryDate() != null) {
            vaccineLot.setExpiryDate(dto.getExpiryDate());
        }
        if (dto.getSupplier() != null) {
            vaccineLot.setSupplier(dto.getSupplier());
        }
        if (dto.getImportDate() != null) {
            vaccineLot.setImportDate(dto.getImportDate());
        }
        if (dto.getStatus() != null) {
            vaccineLot.setStatus(dto.getStatus());
        }
        
        // Tự động cập nhật status nếu cần
        updateLotStatusIfNeeded(vaccineLot);
        
        return vaccineLotRepository.save(vaccineLot);
    }
    
    // Xóa lô vaccine
    public void deleteVaccineLot(Long id) {
        VaccineLot vaccineLot = getVaccineLotById(id);
        
        // Kiểm tra xem lô đã được sử dụng trong vaccination records chưa
        if (vaccineLot.getVaccinationRecords() != null && !vaccineLot.getVaccinationRecords().isEmpty()) {
            throw new RuntimeException("Cannot delete vaccine lot that has been used in vaccination records");
        }
        
        vaccineLotRepository.delete(vaccineLot);
    }
    
    // Tự động cập nhật status dựa trên expiryDate và remainingQuantity
    private void updateLotStatusIfNeeded(VaccineLot vaccineLot) {
        LocalDate today = LocalDate.now();
        
        // Kiểm tra hết hạn
        if (vaccineLot.getExpiryDate().isBefore(today)) {
            vaccineLot.setStatus(VaccineLotStatus.EXPIRED);
        }
        // Kiểm tra hết hàng
        else if (vaccineLot.getRemainingQuantity() == 0) {
            vaccineLot.setStatus(VaccineLotStatus.DEPLETED);
        }
        // Nếu chưa hết hạn và còn hàng thì AVAILABLE
        else if (vaccineLot.getStatus() != VaccineLotStatus.AVAILABLE) {
            vaccineLot.setStatus(VaccineLotStatus.AVAILABLE);
        }
    }
    
    // Lấy danh sách lô vaccine sắp hết hạn (trong vòng X ngày, mặc định 30 ngày)
    public List<VaccineLot> getExpiringSoonLots(Integer days) {
        if (days == null || days <= 0) {
            days = 30; // Mặc định 30 ngày
        }
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(days);
        return vaccineLotRepository.findExpiringSoon(today, warningDate);
    }
    
    // Lấy danh sách lô vaccine sắp hết (remainingQuantity <= threshold, mặc định 50)
    public List<VaccineLot> getLowStockLots(Integer threshold) {
        if (threshold == null || threshold < 0) {
            threshold = 50; // Mặc định 50
        }
        return vaccineLotRepository.findLowStock(threshold);
    }
    
    // Lấy tất cả cảnh báo (sắp hết hạn + sắp hết)
    public List<VaccineLot> getAllWarnings(Integer expiryDays, Integer stockThreshold) {
        List<VaccineLot> expiringSoon = getExpiringSoonLots(expiryDays);
        List<VaccineLot> lowStock = getLowStockLots(stockThreshold);
        
        // Gộp 2 danh sách và loại bỏ trùng lặp
        expiringSoon.addAll(lowStock);
        return expiringSoon.stream()
                .distinct()
                .sorted((a, b) -> {
                    // Sắp xếp theo mức độ ưu tiên: hết hạn trước, sau đó hết hàng
                    if (a.getExpiryDate().isBefore(b.getExpiryDate())) return -1;
                    if (a.getExpiryDate().isAfter(b.getExpiryDate())) return 1;
                    return a.getRemainingQuantity().compareTo(b.getRemainingQuantity());
                })
                .collect(Collectors.toList());
    }
    
    // Tự động cập nhật status cho tất cả lô vaccine (cron job có thể gọi)
    public void updateAllLotStatuses() {
        LocalDate today = LocalDate.now();
        
        // Cập nhật lô đã hết hạn
        List<VaccineLot> expiredLots = vaccineLotRepository.findExpiredLots(today);
        expiredLots.forEach(lot -> lot.setStatus(VaccineLotStatus.EXPIRED));
        vaccineLotRepository.saveAll(expiredLots);
        
        // Cập nhật lô đã hết
        List<VaccineLot> depletedLots = vaccineLotRepository.findDepletedLots();
        depletedLots.forEach(lot -> lot.setStatus(VaccineLotStatus.DEPLETED));
        vaccineLotRepository.saveAll(depletedLots);
    }
    
    // Kiểm tra lô vaccine có tồn tại không
    public boolean existsById(Long id) {
        return vaccineLotRepository.existsById(id);
    }
    
    @Autowired
    private CenterVaccineRepository centerVaccineRepository;
    
    @Autowired
    private VaccinationCenterRepository vaccinationCenterRepository;
    
    /**
     * Lấy danh sách lô vaccine có sẵn theo vaccineId và centerId
     * - Kiểm tra center có vaccine đó không (nếu có CenterVaccine record thì kiểm tra stock, nếu không thì vẫn cho phép nếu có lots)
     * - Lọc các lots có status AVAILABLE và remainingQuantity > 0
     * - Kiểm tra lot chưa hết hạn
     */
    public List<VaccineLot> getAvailableVaccineLots(Long vaccineId, Long centerId) {
        // Kiểm tra center có vaccine này không
        VaccinationCenter center = vaccinationCenterRepository.findById(centerId)
                .orElseThrow(() -> new RuntimeException("Center not found"));
        
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new RuntimeException("Vaccine not found"));
        
        // Kiểm tra center có vaccine này trong CenterVaccine không (optional check)
        Optional<CenterVaccine> centerVaccineOpt = centerVaccineRepository.findByCenterAndVaccine(center, vaccine);
        if (centerVaccineOpt.isPresent()) {
            CenterVaccine centerVaccine = centerVaccineOpt.get();
            // Nếu có record và stockQuantity được set và <= 0 thì không có vaccine
            if (centerVaccine.getStockQuantity() != null && centerVaccine.getStockQuantity() <= 0) {
                return List.of(); // Center đã hết hàng theo CenterVaccine
            }
        }
        
        // Lấy tất cả lots của vaccine này
        List<VaccineLot> allLots = vaccineLotRepository.findByVaccineId(vaccineId);
        
        if (allLots.isEmpty()) {
            return List.of(); // Không có lots nào
        }
        
        // Lọc các lots có sẵn (AVAILABLE, remainingQuantity > 0, chưa hết hạn)
        LocalDate today = LocalDate.now();
        
        List<VaccineLot> availableLots = new ArrayList<>();
        for (VaccineLot lot : allLots) {
            boolean passed = true;
            
            // Kiểm tra status
            if (lot.getStatus() != VaccineLotStatus.AVAILABLE) {
                passed = false;
            }
            
            // Kiểm tra remainingQuantity
            if (passed && (lot.getRemainingQuantity() == null || lot.getRemainingQuantity() <= 0)) {
                passed = false;
            }
            
            // Kiểm tra expiryDate
            if (passed && lot.getExpiryDate() == null) {
                passed = false;
            } else if (passed && lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(today)) {
                // Lot đã hết hạn - bỏ qua
                passed = false;
            }
            
            if (passed) {
                availableLots.add(lot);
            }
        }
        
        return availableLots;
    }
}

