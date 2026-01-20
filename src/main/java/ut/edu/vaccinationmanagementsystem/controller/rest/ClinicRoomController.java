package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.ClinicRoomDTO;
import ut.edu.vaccinationmanagementsystem.entity.ClinicRoom;
import ut.edu.vaccinationmanagementsystem.service.ClinicRoomService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clinic-rooms")
public class ClinicRoomController {
    
    @Autowired
    private ClinicRoomService clinicRoomService;
    
    /**
     * GET /api/clinic-rooms
     * Lấy danh sách tất cả phòng khám
     */
    @GetMapping
    public ResponseEntity<List<ClinicRoom>> getAllRooms(@RequestParam(required = false) Long centerId) {
        try {
            List<ClinicRoom> rooms;
            if (centerId != null) {
                rooms = clinicRoomService.getRoomsByCenterId(centerId);
            } else {
                rooms = clinicRoomService.getAllRooms();
            }
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/clinic-rooms/{id}
     * Chi tiết phòng khám
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        try {
            ClinicRoom room = clinicRoomService.getRoomById(id);
            return ResponseEntity.ok(room);
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
     * POST /api/clinic-rooms
     * Tạo phòng khám mới
     */
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody ClinicRoomDTO dto) {
        try {
            ClinicRoom createdRoom = clinicRoomService.createRoom(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
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
     * PUT /api/clinic-rooms/{id}
     * Cập nhật phòng khám
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody ClinicRoomDTO dto) {
        try {
            ClinicRoom updatedRoom = clinicRoomService.updateRoom(id, dto);
            return ResponseEntity.ok(updatedRoom);
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
     * DELETE /api/clinic-rooms/{id}
     * Xóa phòng khám
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            clinicRoomService.deleteRoom(id);
            Map<String, String> message = new HashMap<>();
            message.put("message", "Clinic room deleted successfully");
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

