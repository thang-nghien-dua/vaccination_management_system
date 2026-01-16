package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.ClinicRoom;

import java.util.List;

@Repository
public interface ClinicRoomRepository extends JpaRepository<ClinicRoom, Long> {
    
    /**
     * Tìm tất cả phòng khám theo center ID
     */
    List<ClinicRoom> findByCenterId(Long centerId);
    
    /**
     * Tìm phòng khám active theo center ID
     */
    List<ClinicRoom> findByCenterIdAndIsActive(Long centerId, Boolean isActive);
    
    /**
     * Tìm phòng khám theo center ID và room number
     */
    ClinicRoom findByCenterIdAndRoomNumber(Long centerId, String roomNumber);
}


