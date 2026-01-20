package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.VaccineLotDTO;
import ut.edu.vaccinationmanagementsystem.entity.VaccineLot;
import ut.edu.vaccinationmanagementsystem.entity.enums.VaccineLotStatus;
import ut.edu.vaccinationmanagementsystem.repository.VaccineLotRepository;
import ut.edu.vaccinationmanagementsystem.service.VaccineLotService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vaccine-lots")
public class VaccineLotController {
    
    @Autowired
    private VaccineLotService vaccineLotService;
    
    @Autowired
    private VaccineLotRepository vaccineLotRepository;
    
    /**
     * GET /api/vaccine-lots
     * Xem danh sách tất cả lô vaccine
     */
    @GetMapping
    public ResponseEntity<List<VaccineLot>> getAllVaccineLots() {
        try {
            List<VaccineLot> vaccineLots = vaccineLotService.getAllVaccineLots();
            return ResponseEntity.ok(vaccineLots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/vaccine-lots/{id}
     * Xem chi tiết lô vaccine theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVaccineLotById(@PathVariable Long id) {
        try {
            VaccineLot vaccineLot = vaccineLotService.getVaccineLotById(id);
            return ResponseEntity.ok(vaccineLot);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccine-lots/lot-number/{lotNumber}
     * Xem chi tiết lô vaccine theo số lô
     */
    @GetMapping("/lot-number/{lotNumber}")
    public ResponseEntity<?> getVaccineLotByLotNumber(@PathVariable String lotNumber) {
        try {
            VaccineLot vaccineLot = vaccineLotService.getVaccineLotByLotNumber(lotNumber);
            return ResponseEntity.ok(vaccineLot);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccine-lots/vaccine/{vaccineId}
     * Xem danh sách lô vaccine theo vaccine ID
     */
    @GetMapping("/vaccine/{vaccineId}")
    public ResponseEntity<List<VaccineLot>> getVaccineLotsByVaccineId(@PathVariable Long vaccineId) {
        try {
            List<VaccineLot> vaccineLots = vaccineLotService.getVaccineLotsByVaccineId(vaccineId);
            return ResponseEntity.ok(vaccineLots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/vaccine-lots/search?keyword={keyword}
     * Tìm kiếm lô vaccine theo từ khóa (số lô, nhà cung cấp)
     */
    @GetMapping("/search")
    public ResponseEntity<List<VaccineLot>> searchVaccineLots(@RequestParam(required = false) String keyword) {
        try {
            List<VaccineLot> vaccineLots = vaccineLotService.searchVaccineLots(keyword);
            return ResponseEntity.ok(vaccineLots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/vaccine-lots
     * Nhập lô vaccine mới
     */
    @PostMapping
    public ResponseEntity<?> createVaccineLot(@RequestBody VaccineLotDTO dto) {
        try {
            VaccineLot vaccineLot = vaccineLotService.createVaccineLot(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(vaccineLot);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * PUT /api/vaccine-lots/{id}
     * Cập nhật lô vaccine
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVaccineLot(@PathVariable Long id, @RequestBody VaccineLotDTO dto) {
        try {
            VaccineLot vaccineLot = vaccineLotService.updateVaccineLot(id, dto);
            return ResponseEntity.ok(vaccineLot);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * DELETE /api/vaccine-lots/{id}
     * Xóa lô vaccine
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVaccineLot(@PathVariable Long id) {
        try {
            vaccineLotService.deleteVaccineLot(id);
            Map<String, String> message = new HashMap<>();
            message.put("message", "Vaccine lot deleted successfully");
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccine-lots/warnings/expiring-soon?days={days}
     * Cảnh báo lô vaccine sắp hết hạn (trong vòng X ngày, mặc định 30 ngày)
     */
    @GetMapping("/warnings/expiring-soon")
    public ResponseEntity<List<VaccineLot>> getExpiringSoonLots(@RequestParam(required = false) Integer days) {
        try {
            List<VaccineLot> vaccineLots = vaccineLotService.getExpiringSoonLots(days);
            return ResponseEntity.ok(vaccineLots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/vaccine-lots/warnings/low-stock?threshold={threshold}
     * Cảnh báo lô vaccine sắp hết (remainingQuantity <= threshold, mặc định 50)
     */
    @GetMapping("/warnings/low-stock")
    public ResponseEntity<List<VaccineLot>> getLowStockLots(@RequestParam(required = false) Integer threshold) {
        try {
            List<VaccineLot> vaccineLots = vaccineLotService.getLowStockLots(threshold);
            return ResponseEntity.ok(vaccineLots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/vaccine-lots/warnings/all?expiryDays={expiryDays}&stockThreshold={stockThreshold}
     * Lấy tất cả cảnh báo (sắp hết hạn + sắp hết)
     */
    @GetMapping("/warnings/all")
    public ResponseEntity<List<VaccineLot>> getAllWarnings(
            @RequestParam(required = false) Integer expiryDays,
            @RequestParam(required = false) Integer stockThreshold) {
        try {
            List<VaccineLot> vaccineLots = vaccineLotService.getAllWarnings(expiryDays, stockThreshold);
            return ResponseEntity.ok(vaccineLots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/vaccine-lots/update-statuses
     * Tự động cập nhật status cho tất cả lô vaccine (có thể gọi từ cron job)
     */
    @PostMapping("/update-statuses")
    public ResponseEntity<?> updateAllLotStatuses() {
        try {
            vaccineLotService.updateAllLotStatuses();
            Map<String, String> message = new HashMap<>();
            message.put("message", "All vaccine lot statuses updated successfully");
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccine-lots/available?vaccineId={}&centerId={}
     * Lấy danh sách lô vaccine có sẵn (còn hàng, chưa hết hạn, status = AVAILABLE)
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableVaccineLots(
            @RequestParam(required = false) Long vaccineId,
            @RequestParam(required = false) Long centerId) {
        try {
            List<VaccineLot> allLots = vaccineLotRepository.findAll();
            LocalDate today = LocalDate.now();
            
            List<VaccineLot> availableLots = allLots.stream()
                    .filter(lot -> lot.getStatus() == VaccineLotStatus.AVAILABLE)
                    .filter(lot -> lot.getRemainingQuantity() != null && lot.getRemainingQuantity() > 0)
                    .filter(lot -> lot.getExpiryDate() != null && lot.getExpiryDate().isAfter(today))
                    .filter(lot -> vaccineId == null || (lot.getVaccine() != null && lot.getVaccine().getId().equals(vaccineId)))
                    .collect(Collectors.toList());
            
            // Nếu có centerId, filter thêm theo center (cần check CenterVaccine)
            if (centerId != null) {
                // TODO: Có thể filter thêm theo center nếu cần
                // Hiện tại chỉ filter theo vaccineId
            }
            
            List<Map<String, Object>> result = availableLots.stream().map(lot -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", lot.getId());
                map.put("lotNumber", lot.getLotNumber());
                map.put("vaccineId", lot.getVaccine() != null ? lot.getVaccine().getId() : null);
                map.put("vaccineName", lot.getVaccine() != null ? lot.getVaccine().getName() : null);
                map.put("remainingQuantity", lot.getRemainingQuantity());
                map.put("quantity", lot.getQuantity());
                map.put("manufacturingDate", lot.getManufacturingDate());
                map.put("expiryDate", lot.getExpiryDate());
                map.put("supplier", lot.getSupplier());
                map.put("status", lot.getStatus().name());
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

