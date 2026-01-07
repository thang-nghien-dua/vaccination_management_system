package ut.edu.vaccinationmanagementsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.CenterVaccineDTO;
import ut.edu.vaccinationmanagementsystem.entity.CenterVaccine;
import ut.edu.vaccinationmanagementsystem.service.CenterVaccineService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CenterVaccineController {
    
    @Autowired
    private CenterVaccineService centerVaccineService;
    
    /**
     * GET /api/centers/{centerId}/vaccines
     * Xem vaccine có tại trung tâm
     */
    @GetMapping("/centers/{centerId}/vaccines")
    public ResponseEntity<List<CenterVaccine>> getVaccinesByCenterId(@PathVariable Long centerId) {
        try {
            List<CenterVaccine> centerVaccines = centerVaccineService.getVaccinesByCenterId(centerId);
            return ResponseEntity.ok(centerVaccines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/centers/{centerId}/vaccines
     * Thêm vaccine vào trung tâm
     */
    @PostMapping("/centers/{centerId}/vaccines")
    public ResponseEntity<?> addVaccineToCenter(@PathVariable Long centerId, @RequestBody CenterVaccineDTO dto) {
        try {
            CenterVaccine createdCenterVaccine = centerVaccineService.addVaccineToCenter(centerId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCenterVaccine);
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
     * PUT /api/center-vaccines/{id}
     * Cập nhật số lượng
     */
    @PutMapping("/center-vaccines/{id}")
    public ResponseEntity<?> updateCenterVaccine(@PathVariable Long id, @RequestBody CenterVaccineDTO dto) {
        try {
            CenterVaccine updatedCenterVaccine = centerVaccineService.updateCenterVaccine(id, dto);
            return ResponseEntity.ok(updatedCenterVaccine);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * DELETE /api/center-vaccines/{id}
     * Xóa vaccine khỏi trung tâm
     */
    @DeleteMapping("/center-vaccines/{id}")
    public ResponseEntity<?> deleteCenterVaccine(@PathVariable Long id) {
        try {
            centerVaccineService.deleteCenterVaccine(id);
            Map<String, String> message = new HashMap<>();
            message.put("message", "Center vaccine deleted successfully");
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


