package ut.edu.vaccinationmanagementsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.VaccineDTO;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;
import ut.edu.vaccinationmanagementsystem.service.VaccineService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/vaccines")
public class VaccineController {
    
    @Autowired
    private VaccineService vaccineService;
    

    @GetMapping
    public ResponseEntity<List<Vaccine>> getAllVaccines() {
        try {
            List<Vaccine> vaccines = vaccineService.getAllVaccines();
            return ResponseEntity.ok(vaccines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/vaccines/{id}
     * Xem chi tiết vaccine theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVaccineById(@PathVariable Long id) {
        try {
            Vaccine vaccine = vaccineService.getVaccineById(id);
            return ResponseEntity.ok(vaccine);
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
     * GET /api/vaccines/search?keyword={keyword}
     * Tìm kiếm vaccine theo từ khóa
     */
    @GetMapping("/search")
    public ResponseEntity<List<Vaccine>> searchVaccines(@RequestParam(required = false) String keyword) {
        try {
            List<Vaccine> vaccines = vaccineService.searchVaccines(keyword);
            return ResponseEntity.ok(vaccines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/vaccines
     * Tạo vaccine mới
     */
    @PostMapping
    public ResponseEntity<?> createVaccine(@RequestBody VaccineDTO dto) {
        try {
            Vaccine createdVaccine = vaccineService.createVaccine(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVaccine);
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
     * PUT /api/vaccines/{id}
     * Cập nhật vaccine
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVaccine(@PathVariable Long id, @RequestBody VaccineDTO dto) {
        try {
            Vaccine updatedVaccine = vaccineService.updateVaccine(id, dto);
            return ResponseEntity.ok(updatedVaccine);
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
     * DELETE /api/vaccines/{id}
     * Xóa vaccine
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVaccine(@PathVariable Long id) {
        try {
            vaccineService.deleteVaccine(id);
            Map<String, String> message = new HashMap<>();
            message.put("message", "Vaccine deleted successfully");
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

