package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.AppointmentSlotDTO;
import ut.edu.vaccinationmanagementsystem.entity.AppointmentSlot;
import ut.edu.vaccinationmanagementsystem.entity.ClinicRoom;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentSlotRepository;
import ut.edu.vaccinationmanagementsystem.repository.ClinicRoomRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationCenterRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service xử lý business logic cho AppointmentSlot
 */
@Service
@Transactional
public class AppointmentSlotService {
    
    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;
    
    @Autowired
    private VaccinationCenterRepository vaccinationCenterRepository;
    
    @Autowired
    private ClinicRoomRepository clinicRoomRepository;
    
    // Lấy tất cả danh sách slot
    public List<AppointmentSlot> getAllAppointmentSlots() {
        return appointmentSlotRepository.findAll();
    }
    
    // Lấy slot theo ID
    public AppointmentSlot getAppointmentSlotById(Long id) {
        return appointmentSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment slot not found with id: " + id));
    }
    
    // Lấy tất cả slot theo center ID
    public List<AppointmentSlot> getAppointmentSlotsByCenterId(Long centerId) {
        return appointmentSlotRepository.findByCenterId(centerId);
    }
    
    // Lấy slot theo center ID và ngày
    public List<AppointmentSlot> getAppointmentSlotsByCenterAndDate(Long centerId, LocalDate date) {
        return appointmentSlotRepository.findByCenterIdAndDate(centerId, date);
    }
    
    // Tạo slot mới từ DTO
    public AppointmentSlot createAppointmentSlot(AppointmentSlotDTO dto) {
        // Validate các field bắt buộc
        if (dto.getCenterId() == null) {
            throw new RuntimeException("Center ID is required");
        }
        if (dto.getDate() == null) {
            throw new RuntimeException("Date is required");
        }
        if (dto.getStartTime() == null) {
            throw new RuntimeException("Start time is required");
        }
        if (dto.getEndTime() == null) {
            throw new RuntimeException("End time is required");
        }
        if (dto.getMaxCapacity() == null || dto.getMaxCapacity() <= 0) {
            throw new RuntimeException("Max capacity must be greater than 0");
        }
        
        // Kiểm tra endTime phải sau startTime
        if (dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().equals(dto.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }
        
        // Kiểm tra ngày không được trong quá khứ
        if (dto.getDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot create slot for past date");
        }
        
        // Kiểm tra center có tồn tại không
        VaccinationCenter center = vaccinationCenterRepository.findById(dto.getCenterId())
                .orElseThrow(() -> new RuntimeException("Vaccination center not found with id: " + dto.getCenterId()));
        
        // Kiểm tra slot có trùng lịch không
        List<AppointmentSlot> overlappingSlots = appointmentSlotRepository.findOverlappingSlots(
                dto.getCenterId(), 
                dto.getDate(), 
                dto.getStartTime(), 
                dto.getEndTime(), 
                -1L); // -1 để không exclude slot nào khi tạo mới
        
        if (!overlappingSlots.isEmpty()) {
            throw new RuntimeException("Slot overlaps with existing slot(s)");
        }
        
        // Validate currentBookings
        Integer currentBookings = dto.getCurrentBookings();
        if (currentBookings == null) {
            currentBookings = 0; // Mặc định = 0 khi tạo mới
        }
        if (currentBookings < 0) {
            throw new RuntimeException("Current bookings cannot be negative");
        }
        if (currentBookings > dto.getMaxCapacity()) {
            throw new RuntimeException("Current bookings cannot exceed max capacity");
        }
        
        // Convert DTO sang Entity
        AppointmentSlot slot = new AppointmentSlot();
        slot.setCenter(center);
        slot.setDate(dto.getDate());
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setMaxCapacity(dto.getMaxCapacity());
        slot.setCurrentBookings(currentBookings);
        slot.setCreatedAt(LocalDateTime.now());
        
        // Gán phòng nếu có roomId
        if (dto.getRoomId() != null) {
            ClinicRoom room = clinicRoomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Clinic room not found with id: " + dto.getRoomId()));
            
            // Validate: phòng phải thuộc center của slot
            if (!room.getCenter().getId().equals(center.getId())) {
                throw new RuntimeException("Clinic room does not belong to selected center");
            }
            
            // Validate: phòng phải đang active
            if (!room.getIsActive()) {
                throw new RuntimeException("Clinic room is not active");
            }
            
            slot.setRoom(room);
        }
        // Nếu không có roomId, để null (có thể gán sau)
        
        // Tự động cập nhật isAvailable
        updateSlotAvailability(slot);
        
        try {
            return appointmentSlotRepository.save(slot);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save appointment slot: " + e.getMessage(), e);
        }
    }
    
    // Cập nhật slot từ DTO
    public AppointmentSlot updateAppointmentSlot(Long id, AppointmentSlotDTO dto) {
        AppointmentSlot slot = getAppointmentSlotById(id);
        
        // Validate
        if (dto.getEndTime() != null && dto.getStartTime() != null) {
            if (dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().equals(dto.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
        }
        
        if (dto.getDate() != null && dto.getDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot update slot to past date");
        }
        
        if (dto.getMaxCapacity() != null && dto.getMaxCapacity() <= 0) {
            throw new RuntimeException("Max capacity must be greater than 0");
        }
        
        // Cập nhật center nếu có thay đổi
        if (dto.getCenterId() != null && !slot.getCenter().getId().equals(dto.getCenterId())) {
            VaccinationCenter center = vaccinationCenterRepository.findById(dto.getCenterId())
                    .orElseThrow(() -> new RuntimeException("Vaccination center not found with id: " + dto.getCenterId()));
            slot.setCenter(center);
        }
        
        // Cập nhật các thông tin từ DTO
        if (dto.getDate() != null) {
            slot.setDate(dto.getDate());
        }
        if (dto.getStartTime() != null) {
            slot.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            slot.setEndTime(dto.getEndTime());
        }
        if (dto.getMaxCapacity() != null) {
            slot.setMaxCapacity(dto.getMaxCapacity());
        }
        if (dto.getCurrentBookings() != null) {
            if (dto.getCurrentBookings() < 0) {
                throw new RuntimeException("Current bookings cannot be negative");
            }
            if (slot.getMaxCapacity() != null && dto.getCurrentBookings() > slot.getMaxCapacity()) {
                throw new RuntimeException("Current bookings cannot exceed max capacity");
            }
            slot.setCurrentBookings(dto.getCurrentBookings());
        }
        if (dto.getIsAvailable() != null) {
            slot.setIsAvailable(dto.getIsAvailable());
        }
        
        // Cập nhật phòng nếu có roomId
        if (dto.getRoomId() != null) {
            ClinicRoom room = clinicRoomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Clinic room not found with id: " + dto.getRoomId()));
            
            // Validate: phòng phải thuộc center của slot
            if (!room.getCenter().getId().equals(slot.getCenter().getId())) {
                throw new RuntimeException("Clinic room does not belong to slot's center");
            }
            
            // Validate: phòng phải đang active
            if (!room.getIsActive()) {
                throw new RuntimeException("Clinic room is not active");
            }
            
            slot.setRoom(room);
        } else if (dto.getRoomId() == null && slot.getRoom() != null) {
            // Nếu roomId là null trong DTO và slot đã có phòng, giữ nguyên phòng cũ
            // (không xóa phòng khi update các field khác)
        }
        
        // Kiểm tra trùng lịch nếu có thay đổi thời gian
        if (dto.getDate() != null || dto.getStartTime() != null || dto.getEndTime() != null) {
            LocalDate checkDate = dto.getDate() != null ? dto.getDate() : slot.getDate();
            LocalTime checkStartTime = dto.getStartTime() != null ? dto.getStartTime() : slot.getStartTime();
            LocalTime checkEndTime = dto.getEndTime() != null ? dto.getEndTime() : slot.getEndTime();
            
            List<AppointmentSlot> overlappingSlots = appointmentSlotRepository.findOverlappingSlots(
                    slot.getCenter().getId(),
                    checkDate,
                    checkStartTime,
                    checkEndTime,
                    id); // Exclude slot hiện tại
            
            if (!overlappingSlots.isEmpty()) {
                throw new RuntimeException("Slot overlaps with existing slot(s)");
            }
        }
        
        // Tự động cập nhật isAvailable
        updateSlotAvailability(slot);
        
        return appointmentSlotRepository.save(slot);
    }
    
    // Xóa slot
    public void deleteAppointmentSlot(Long id) {
        AppointmentSlot slot = getAppointmentSlotById(id);
        
        // Kiểm tra xem slot đã có appointment chưa
        if (slot.getAppointments() != null && !slot.getAppointments().isEmpty()) {
            throw new RuntimeException("Cannot delete slot that has appointments");
        }
        
        appointmentSlotRepository.delete(slot);
    }
    
    // Tự động cập nhật isAvailable dựa trên currentBookings và maxCapacity
    private void updateSlotAvailability(AppointmentSlot slot) {
        LocalDate today = LocalDate.now();
        
        // Nếu slot đã quá ngày hoặc đã hết chỗ thì không available
        if (slot.getDate().isBefore(today) || 
            slot.getCurrentBookings() >= slot.getMaxCapacity()) {
            slot.setIsAvailable(false);
        } else {
            slot.setIsAvailable(true);
        }
    }
    
    // Lấy danh sách slot trống (tất cả)
    public List<AppointmentSlot> getAvailableSlots() {
        return appointmentSlotRepository.findAvailableSlots();
    }
    
    // Lấy danh sách slot trống theo center ID
    public List<AppointmentSlot> getAvailableSlotsByCenter(Long centerId) {
        return appointmentSlotRepository.findAvailableSlotsByCenter(centerId);
    }
    
    // Lấy danh sách slot trống theo center ID và ngày
    public List<AppointmentSlot> getAvailableSlotsByCenterAndDate(Long centerId, LocalDate date) {
        return appointmentSlotRepository.findAvailableSlotsByCenterAndDate(centerId, date);
    }
    
    // Lấy danh sách slot trống trong khoảng thời gian
    public List<AppointmentSlot> getAvailableSlotsByDateRange(Long centerId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date must be before or equal to end date");
        }
        return appointmentSlotRepository.findAvailableSlotsByDateRange(centerId, startDate, endDate);
    }
    
    // Lấy danh sách slot trống theo center, ngày và khoảng thời gian
    public List<AppointmentSlot> getAvailableSlotsByCenterDateAndTimeRange(
            Long centerId, 
            LocalDate date, 
            LocalTime startTime, 
            LocalTime endTime) {
        if (date == null || startTime == null || endTime == null) {
            throw new RuntimeException("Date, start time and end time are required");
        }
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new RuntimeException("End time must be after start time");
        }
        return appointmentSlotRepository.findAvailableSlotsByCenterDateAndTimeRange(
                centerId, date, startTime, endTime);
    }
    
    // Tự động cập nhật availability cho tất cả slot (cron job có thể gọi)
    public void updateAllSlotAvailabilities() {
        LocalDate today = LocalDate.now();
        
        // Cập nhật slot đã quá ngày
        List<AppointmentSlot> pastSlots = appointmentSlotRepository.findPastSlots(today);
        pastSlots.forEach(slot -> slot.setIsAvailable(false));
        appointmentSlotRepository.saveAll(pastSlots);
        
        // Cập nhật slot đã hết chỗ
        List<AppointmentSlot> fullSlots = appointmentSlotRepository.findFullSlots();
        fullSlots.forEach(slot -> slot.setIsAvailable(false));
        appointmentSlotRepository.saveAll(fullSlots);
    }
    
    // Kiểm tra slot có tồn tại không
    public boolean existsById(Long id) {
        return appointmentSlotRepository.existsById(id);
    }
}






