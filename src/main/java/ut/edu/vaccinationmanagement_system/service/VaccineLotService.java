package ut.edu.vaccinationmanagement_system.service;

import org.springframework.stereotype.Service;
import ut.edu.vaccinationmanagement_system.dto.VaccineLotDTO;
import ut.edu.vaccinationmanagement_system.entity.Vaccine;
import ut.edu.vaccinationmanagement_system.entity.VaccineLot;
import ut.edu.vaccinationmanagement_system.entity.enums.VaccineLotStatus;
import ut.edu.vaccinationmanagement_system.repository.VaccineLotRepository;
import ut.edu.vaccinationmanagement_system.repository.VaccineRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VaccineLotService {

    private final VaccineLotRepository vaccineLotRepository;
    private final VaccineRepository vaccineRepository;

    public VaccineLotService(VaccineLotRepository vaccineLotRepository,
                             VaccineRepository vaccineRepository) {
        this.vaccineLotRepository = vaccineLotRepository;
        this.vaccineRepository = vaccineRepository;
    }
    // 1. Xem danh sách lô vaccine
    public List<VaccineLotDTO> getAll() {
        return vaccineLotRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // 2. Chi tiết lô vaccine
    public VaccineLotDTO getById(Long id) {
        VaccineLot lot = vaccineLotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lô vaccine"));
        return toDTO(lot);
    }

    // 3. Nhập lô vaccine mới
    public VaccineLotDTO create(VaccineLotDTO dto) {
        VaccineLot lot = new VaccineLot();

        Vaccine vaccine = vaccineRepository.findById(dto.getVaccine().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vaccine"));

        lot.setLotNumber(dto.getLotNumber());
        lot.setVaccine(vaccine);
        lot.setQuantity(dto.getQuantity());
        lot.setRemainingQuantity(dto.getQuantity());
        lot.setManufacturingDate(dto.getManufacturingDate());
        lot.setImportDate(dto.getImportDate());
        lot.setExpiryDate(dto.getExpiryDate());
        lot.setSupplier(dto.getSupplier());
        lot.setStatus(VaccineLotStatus.AVAILABLE);
        lot.setCreatedAt(LocalDateTime.now());

        return toDTO(vaccineLotRepository.save(lot));
    }

    // 4. Cập nhật lô vaccine
    public VaccineLotDTO update(Long id, VaccineLotDTO dto) {
        VaccineLot lot = vaccineLotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lô vaccine"));

        lot.setExpiryDate(dto.getExpiryDate());
        lot.setSupplier(dto.getSupplier());
        lot.setStatus(dto.getStatus());

        return toDTO(vaccineLotRepository.save(lot));
    }

    // 5. Cảnh báo sắp hết hạn
    public List<VaccineLotDTO> expiringSoon(int days) {
        LocalDate warningDate = LocalDate.now().plusDays(days);
        return vaccineLotRepository.findExpiringSoon(warningDate)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // 6. Cảnh báo sắp hết số lượng
    public List<VaccineLotDTO> lowStock(int threshold) {
        return vaccineLotRepository.findByRemainingQuantityLessThanEqual(threshold)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ===== Mapping =====
    private VaccineLotDTO toDTO(VaccineLot lot) {
        VaccineLotDTO dto = new VaccineLotDTO();
        dto.setId(lot.getId());
        dto.setLotNumber(lot.getLotNumber());
        dto.setVaccine(lot.getVaccine());
        dto.setQuantity(lot.getQuantity());
        dto.setRemainingQuantity(lot.getRemainingQuantity());
        dto.setManufacturingDate(lot.getManufacturingDate());
        dto.setImportDate(lot.getImportDate());
        dto.setExpiryDate(lot.getExpiryDate());
        dto.setSupplier(lot.getSupplier());
        dto.setStatus(lot.getStatus());
        dto.setCreatedAt(lot.getCreatedAt());
        return dto;
    }
}
