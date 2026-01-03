package ut.edu.vaccinationmanagementsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.CenterWorkingHoursDTO;
import ut.edu.vaccinationmanagementsystem.entity.CenterWorkingHours;
import ut.edu.vaccinationmanagementsystem.service.CenterWorkingHoursService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/center-working-hours")
public class CenterWorkingHoursController {
    
    @Autowired
    private CenterWorkingHoursService centerWorkingHoursService;
    
    @GetMapping
    public ResponseEntity<List<CenterWorkingHours>> getAllWorkingHours() {
        try {
            List<CenterWorkingHours> workingHours = centerWorkingHoursService.getAllWorkingHours();
            return ResponseEntity.ok(workingHours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkingHoursById(@PathVariable Long id) {
        try {
            CenterWorkingHours workingHours = centerWorkingHoursService.getWorkingHoursById(id);
            return ResponseEntity.ok(workingHours);
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
    
    @GetMapping("/center/{centerId}")
    public ResponseEntity<List<CenterWorkingHours>> getWorkingHoursByCenterId(@PathVariable Long centerId) {
        try {
            List<CenterWorkingHours> workingHours = centerWorkingHoursService.getWorkingHoursByCenterId(centerId);
            return ResponseEntity.ok(workingHours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createWorkingHours(@RequestBody CenterWorkingHoursDTO dto) {
        try {
            CenterWorkingHours createdWorkingHours = centerWorkingHoursService.createWorkingHours(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkingHours);
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
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkingHours(@PathVariable Long id, @RequestBody CenterWorkingHoursDTO dto) {
        try {
            CenterWorkingHours updatedWorkingHours = centerWorkingHoursService.updateWorkingHours(id, dto);
            return ResponseEntity.ok(updatedWorkingHours);
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
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkingHours(@PathVariable Long id) {
        try {
            centerWorkingHoursService.deleteWorkingHours(id);
            Map<String, String> message = new HashMap<>();
            message.put("message", "Working hours deleted successfully");
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

