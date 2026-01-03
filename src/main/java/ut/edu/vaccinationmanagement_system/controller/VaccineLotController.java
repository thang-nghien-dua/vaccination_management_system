package ut.edu.vaccinationmanagement_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagement_system.dto.VaccineLotDTO;
import ut.edu.vaccinationmanagement_system.service.VaccineLotService;

import java.util.List;

@RestController
@RequestMapping("/api/vaccine-lots")
public class VaccineLotController {

    private final VaccineLotService service;

    public VaccineLotController(VaccineLotService service) {
        this.service = service;
    }
    //GET    /api/vaccine-lots - Xem danh sách lô vaccine
    @GetMapping
    public List<VaccineLotDTO> getAll() {
        return service.getAll();
    }

    //GET    /api/vaccine-lots/{id} - Chi tiết lô vaccine
    @GetMapping("/{id}")
    public VaccineLotDTO getDetail(@PathVariable Long id) {
        return service.getById(id);
    }

    //POST   /api/vaccine-lots  - Nhập lô vaccine mới
    @PostMapping
    public VaccineLotDTO create(@RequestBody VaccineLotDTO dto) {
        return service.create(dto);
    }

    //PUT    /api/vaccine-lots/{id} - Cập nhật lô vaccine
    @PutMapping("/{id}")
    public VaccineLotDTO update(
            @PathVariable Long id,
            @RequestBody VaccineLotDTO dto) {
        return service.update(id, dto);
    }

    //api/vaccine-lots/expiring-soon?days=30 - Cảnh báo sắp hết hạn
    @GetMapping("/expiring-soon")
    public List<VaccineLotDTO> expiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        return service.expiringSoon(days);
    }

    //GET    /api/vaccine-lots/low-stock?threshold=10  - Cảnh báo sắp hết
    @GetMapping("/low-stock")
    public List<VaccineLotDTO> lowStock(
            @RequestParam(defaultValue = "10") int threshold) {
        return service.lowStock(threshold);
    }
}