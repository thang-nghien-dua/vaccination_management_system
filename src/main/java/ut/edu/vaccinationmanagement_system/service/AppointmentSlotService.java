package ut.edu.vaccinationmanagement_system.service;

import org.springframework.stereotype.Service;
import ut.edu.vaccinationmanagement_system.dto.AppointmentSlotDTO;
import ut.edu.vaccinationmanagement_system.entity.AppointmentSlot;
import ut.edu.vaccinationmanagement_system.repository.AppointmentSlotRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentSlotService {

    private final AppointmentSlotRepository repository;

    public AppointmentSlotService(AppointmentSlotRepository repository) {
        this.repository = repository;
    }

    // 1. Xem slot trống theo ngày
    public List<AppointmentSlotDTO> getSlots(
            Long centerId, LocalDate date) {
        return repository
                .findByCenter_IdAndDate(centerId, date)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // 2. Tạo slot mới
    public AppointmentSlotDTO create(AppointmentSlotDTO dto) {
        AppointmentSlot slot = new AppointmentSlot();

        slot.setDate(dto.getDate());
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setMaxCapacity(dto.getMaxCapacity());
        slot.setCurrentBookings(0);
        slot.setIsAvailable(true);
        slot.setCenter(dto.getCenter());
        slot.setCreatedAt(LocalDateTime.now());

        return toDTO(repository.save(slot));
    }

    // 3. Cập nhật slot
    public AppointmentSlotDTO update(Long id, AppointmentSlotDTO dto) {
        AppointmentSlot slot = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy slot"));

        slot.setDate(dto.getDate());
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setMaxCapacity(dto.getMaxCapacity());
        slot.setIsAvailable(dto.getAvailable());

        return toDTO(repository.save(slot));
    }

    // 4. Xóa slot
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // 5. Slot còn trống (theo ngày)
    public List<AppointmentSlotDTO> getAvailableSlots(
            Long centerId,
            LocalDate startDate,
            LocalDate endDate) {

        return repository
                .findAvailableSlots(centerId, startDate, endDate)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ===== Mapping =====
    private AppointmentSlotDTO toDTO(AppointmentSlot slot) {
        AppointmentSlotDTO dto = new AppointmentSlotDTO();
        dto.setId(slot.getId());
        dto.setDate(slot.getDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setMaxCapacity(slot.getMaxCapacity());
        dto.setCurrentBookings(slot.getCurrentBookings());
        dto.setAvailable(slot.getCurrentBookings() < slot.getMaxCapacity());
        dto.setCenter(slot.getCenter());
        dto.setCreatedAt(slot.getCreatedAt());
        return dto;
    }
}
