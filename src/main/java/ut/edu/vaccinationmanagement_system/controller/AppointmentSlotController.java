package ut.edu.vaccinationmanagement_system.controller;

import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagement_system.dto.AppointmentSlotDTO;
import ut.edu.vaccinationmanagement_system.service.AppointmentSlotService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class AppointmentSlotController {

    private final AppointmentSlotService service;

    public AppointmentSlotController(AppointmentSlotService service) {
        this.service = service;
    }

    // GET /api/slots?centerId=&date= - Xem slot trống
    @GetMapping
    public List<AppointmentSlotDTO> getSlots(
            @RequestParam Long centerId,
            @RequestParam LocalDate date) {
        return service.getSlots(centerId, date);
    }

    // POST /api/slots - Tạo slot mới
    @PostMapping
    public AppointmentSlotDTO create(
            @RequestBody AppointmentSlotDTO dto) {
        return service.create(dto);
    }

    // PUT /api/slots/{id} - Cập nhật slot
    @PutMapping("/{id}")
    public AppointmentSlotDTO update(
            @PathVariable Long id,
            @RequestBody AppointmentSlotDTO dto) {
        return service.update(id, dto);
    }

    // DELETE /api/slots/{id} - Xóa slot
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // GET /api/slots/available?centerId=&startDate=&endDate= - Slot còn trống
    @GetMapping("/available")
    public List<AppointmentSlotDTO> available(
            @RequestParam Long centerId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return service.getAvailableSlots(
                centerId, startDate, endDate);
    }
}
