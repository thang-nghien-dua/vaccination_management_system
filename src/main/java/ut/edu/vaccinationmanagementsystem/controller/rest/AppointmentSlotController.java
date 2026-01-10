package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.AppointmentSlotDTO;
import ut.edu.vaccinationmanagementsystem.entity.AppointmentSlot;
import ut.edu.vaccinationmanagementsystem.service.AppointmentSlotService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointment-slots")
public class AppointmentSlotController {
    
    @Autowired
    private AppointmentSlotService appointmentSlotService;
    
    /**
     * GET /api/appointment-slots
     * Xem danh sách tất cả slot
     */
    @GetMapping
    public ResponseEntity<List<AppointmentSlot>> getAllAppointmentSlots() {
        try {
            List<AppointmentSlot> slots = appointmentSlotService.getAllAppointmentSlots();
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/appointment-slots/{id}
     * Xem chi tiết slot theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentSlotById(@PathVariable Long id) {
        try {
            AppointmentSlot slot = appointmentSlotService.getAppointmentSlotById(id);
            return ResponseEntity.ok(slot);
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
     * GET /api/appointment-slots/center/{centerId}
     * Xem danh sách slot theo center ID
     */
    @GetMapping("/center/{centerId}")
    public ResponseEntity<List<AppointmentSlot>> getAppointmentSlotsByCenterId(@PathVariable Long centerId) {
        try {
            List<AppointmentSlot> slots = appointmentSlotService.getAppointmentSlotsByCenterId(centerId);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/appointment-slots/center/{centerId}/date/{date}
     * Xem danh sách slot theo center ID và ngày
     */
    @GetMapping("/center/{centerId}/date/{date}")
    public ResponseEntity<List<AppointmentSlot>> getAppointmentSlotsByCenterAndDate(
            @PathVariable Long centerId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AppointmentSlot> slots = appointmentSlotService.getAppointmentSlotsByCenterAndDate(centerId, date);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/appointment-slots/available
     * Xem danh sách slot trống (tất cả)
     */
    @GetMapping("/available")
    public ResponseEntity<List<AppointmentSlot>> getAvailableSlots() {
        try {
            List<AppointmentSlot> slots = appointmentSlotService.getAvailableSlots();
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/appointment-slots/available/center/{centerId}
     * Xem danh sách slot trống theo center ID
     */
    @GetMapping("/available/center/{centerId}")
    public ResponseEntity<List<AppointmentSlot>> getAvailableSlotsByCenter(@PathVariable Long centerId) {
        try {
            List<AppointmentSlot> slots = appointmentSlotService.getAvailableSlotsByCenter(centerId);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/appointment-slots/available/center/{centerId}/date/{date}
     * Xem danh sách slot trống theo center ID và ngày
     */
    @GetMapping("/available/center/{centerId}/date/{date}")
    public ResponseEntity<List<AppointmentSlot>> getAvailableSlotsByCenterAndDate(
            @PathVariable Long centerId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AppointmentSlot> slots = appointmentSlotService.getAvailableSlotsByCenterAndDate(centerId, date);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/appointment-slots/available/center/{centerId}/date-range?startDate={startDate}&endDate={endDate}
     * Xem danh sách slot trống trong khoảng thời gian
     */
    @GetMapping("/available/center/{centerId}/date-range")
    public ResponseEntity<?> getAvailableSlotsByDateRange(
            @PathVariable Long centerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AppointmentSlot> slots = appointmentSlotService.getAvailableSlotsByDateRange(
                    centerId, startDate, endDate);
            return ResponseEntity.ok(slots);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace(); // Log error để debug
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/appointment-slots/available/center/{centerId}/date/{date}/time-range?startTime={startTime}&endTime={endTime}
     * Xem danh sách slot trống theo center, ngày và khoảng thời gian
     */
    @GetMapping("/available/center/{centerId}/date/{date}/time-range")
    public ResponseEntity<?> getAvailableSlotsByCenterDateAndTimeRange(
            @PathVariable Long centerId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        try {
            List<AppointmentSlot> slots = appointmentSlotService.getAvailableSlotsByCenterDateAndTimeRange(
                    centerId, date, startTime, endTime);
            return ResponseEntity.ok(slots);
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
     * POST /api/appointment-slots
     * Tạo slot mới
     */
    @PostMapping
    public ResponseEntity<?> createAppointmentSlot(@RequestBody AppointmentSlotDTO dto) {
        try {
            AppointmentSlot slot = appointmentSlotService.createAppointmentSlot(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(slot);
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
     * PUT /api/appointment-slots/{id}
     * Cập nhật slot
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointmentSlot(@PathVariable Long id, @RequestBody AppointmentSlotDTO dto) {
        try {
            AppointmentSlot slot = appointmentSlotService.updateAppointmentSlot(id, dto);
            return ResponseEntity.ok(slot);
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
     * DELETE /api/appointment-slots/{id}
     * Xóa slot
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointmentSlot(@PathVariable Long id) {
        try {
            appointmentSlotService.deleteAppointmentSlot(id);
            Map<String, String> message = new HashMap<>();
            message.put("message", "Appointment slot deleted successfully");
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
     * POST /api/appointment-slots/update-availabilities
     * Tự động cập nhật availability cho tất cả slot (có thể gọi từ cron job)
     */
    @PostMapping("/update-availabilities")
    public ResponseEntity<?> updateAllSlotAvailabilities() {
        try {
            appointmentSlotService.updateAllSlotAvailabilities();
            Map<String, String> message = new HashMap<>();
            message.put("message", "All appointment slot availabilities updated successfully");
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

