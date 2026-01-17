package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.VaccineLotDTO;
import ut.edu.vaccinationmanagementsystem.entity.VaccineLot;
import ut.edu.vaccinationmanagementsystem.service.VaccineLotService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vaccine-lots")
public class VaccineLotController {
    
    @Autowired
    private VaccineLotService vaccineLotService;
    
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
     * GET /api/vaccine-lots/available?vaccineId={}&centerId={}
     * Lấy danh sách lô vaccine có sẵn theo vaccineId và centerId (cho Nurse)
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableVaccineLots(
            @RequestParam(required = false) Long vaccineId,
            @RequestParam(required = false) Long centerId) {
        try {
            if (vaccineId == null || centerId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "vaccineId and centerId are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            List<VaccineLot> vaccineLots = vaccineLotService.getAvailableVaccineLots(vaccineId, centerId);
            
            // Convert to Map để tránh circular reference khi serialize JSON
            List<Map<String, Object>> lotDTOs = vaccineLots.stream().map(lot -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", lot.getId());
                dto.put("lotNumber", lot.getLotNumber());
                dto.put("quantity", lot.getQuantity());
                dto.put("remainingQuantity", lot.getRemainingQuantity());
                dto.put("manufacturingDate", lot.getManufacturingDate());
                dto.put("expiryDate", lot.getExpiryDate());
                dto.put("supplier", lot.getSupplier());
                dto.put("importDate", lot.getImportDate());
                dto.put("status", lot.getStatus() != null ? lot.getStatus().name() : null);
                dto.put("createdAt", lot.getCreatedAt());
                
                // Vaccine info (chỉ ID và name để tránh circular reference)
                if (lot.getVaccine() != null) {
                    Map<String, Object> vaccine = new HashMap<>();
                    vaccine.put("id", lot.getVaccine().getId());
                    vaccine.put("name", lot.getVaccine().getName());
                    vaccine.put("code", lot.getVaccine().getCode());
                    dto.put("vaccine", vaccine);
                }
                
                return dto;
            }).collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(lotDTOs);
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
}

