package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.VaccinationCenterDTO;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.service.VaccinationCenterService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/centers")
public class VaccinationCenterController {
    
    @Autowired
    private VaccinationCenterService vaccinationCenterService;
    
    /**
     * GET /api/centers
     * Xem danh sách trung tâm
     */
    @GetMapping
    public ResponseEntity<List<VaccinationCenter>> getAllCenters() {
        try {
            List<VaccinationCenter> centers = vaccinationCenterService.getAllCenters();
            return ResponseEntity.ok(centers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/centers/{id}
     * Chi tiết trung tâm
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCenterById(@PathVariable Long id) {
        try {
            VaccinationCenter center = vaccinationCenterService.getCenterById(id);
            return ResponseEntity.ok(center);
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
     * POST /api/centers
     * Tạo trung tâm mới
     */
    @PostMapping
    public ResponseEntity<?> createCenter(@RequestBody VaccinationCenterDTO dto) {
        try {
            VaccinationCenter createdCenter = vaccinationCenterService.createCenter(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCenter);
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
     * PUT /api/centers/{id}
     * Cập nhật trung tâm
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCenter(@PathVariable Long id, @RequestBody VaccinationCenterDTO dto) {
        try {
            VaccinationCenter updatedCenter = vaccinationCenterService.updateCenter(id, dto);
            return ResponseEntity.ok(updatedCenter);
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
     * DELETE /api/centers/{id}
     * Xóa trung tâm
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCenter(@PathVariable Long id) {
        try {
            vaccinationCenterService.deleteCenter(id);
            Map<String, String> message = new HashMap<>();
            message.put("message", "Vaccination center deleted successfully");
            return ResponseEntity.ok(message);
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
}


