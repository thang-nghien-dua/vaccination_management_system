package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.ClinicRoomDTO;
import ut.edu.vaccinationmanagementsystem.entity.ClinicRoom;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter;
import ut.edu.vaccinationmanagementsystem.repository.ClinicRoomRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationCenterRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ClinicRoomService {
    
    @Autowired
    private ClinicRoomRepository clinicRoomRepository;
    
    @Autowired
    private VaccinationCenterRepository vaccinationCenterRepository;
    
    public List<ClinicRoom> getAllRooms() {
        return clinicRoomRepository.findAll();
    }
    
    public List<ClinicRoom> getRoomsByCenterId(Long centerId) {
        return clinicRoomRepository.findByCenterId(centerId);
    }
    
    public ClinicRoom getRoomById(Long id) {
        return clinicRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic room not found with id: " + id));
    }
    
    public ClinicRoom createRoom(ClinicRoomDTO dto) {
        // Validate
        if (dto.getCenterId() == null) {
            throw new RuntimeException("Center ID is required");
        }
        if (dto.getRoomNumber() == null || dto.getRoomNumber().trim().isEmpty()) {
            throw new RuntimeException("Room number is required");
        }
        
        // Kiểm tra center có tồn tại không
        VaccinationCenter center = vaccinationCenterRepository.findById(dto.getCenterId())
                .orElseThrow(() -> new RuntimeException("Vaccination center not found with id: " + dto.getCenterId()));
        
        // Kiểm tra room number đã tồn tại trong center chưa
        if (clinicRoomRepository.findByCenterIdAndRoomNumber(dto.getCenterId(), dto.getRoomNumber()) != null) {
            throw new RuntimeException("Room number already exists in this center");
        }
        
        // Convert DTO sang Entity
        ClinicRoom room = new ClinicRoom();
        room.setCenter(center);
        room.setRoomNumber(dto.getRoomNumber().trim());
        room.setDescription(dto.getDescription());
        room.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        room.setCreatedAt(LocalDateTime.now());
        
        return clinicRoomRepository.save(room);
    }
    
    public ClinicRoom updateRoom(Long id, ClinicRoomDTO dto) {
        ClinicRoom room = getRoomById(id);
        
        // Cập nhật room number nếu có thay đổi
        if (dto.getRoomNumber() != null && !dto.getRoomNumber().trim().isEmpty()) {
            // Kiểm tra room number mới có trùng với room khác trong cùng center không
            ClinicRoom existingRoom = clinicRoomRepository.findByCenterIdAndRoomNumber(room.getCenter().getId(), dto.getRoomNumber());
            if (existingRoom != null && !existingRoom.getId().equals(id)) {
                throw new RuntimeException("Room number already exists in this center");
            }
            room.setRoomNumber(dto.getRoomNumber().trim());
        }
        
        // Cập nhật description
        if (dto.getDescription() != null) {
            room.setDescription(dto.getDescription());
        }
        
        // Cập nhật isActive
        if (dto.getIsActive() != null) {
            room.setIsActive(dto.getIsActive());
        }
        
        return clinicRoomRepository.save(room);
    }
    
    public void deleteRoom(Long id) {
        ClinicRoom room = getRoomById(id);
        clinicRoomRepository.delete(room);
    }
}

