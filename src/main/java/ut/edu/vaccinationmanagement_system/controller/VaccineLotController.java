package ut.edu.vaccinationmanagement_system.controller;

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

    @GetMapping
    public List<VaccineLotDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public VaccineLotDTO getDetail(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public VaccineLotDTO create(@RequestBody VaccineLotDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public VaccineLotDTO update(
            @PathVariable Long id,
            @RequestBody VaccineLotDTO dto) {
        return service.update(id, dto);
    }

    @GetMapping("/alerts/expiry")
    public List<VaccineLotDTO> expiringSoon(
            @RequestParam(defaultValue = "30") int days) {
        return service.expiringSoon(days);
    }

    @GetMapping("/alerts/low-stock")
    public List<VaccineLotDTO> lowStock(
            @RequestParam(defaultValue = "10") int threshold) {
        return service.lowStock(threshold);
    }
}