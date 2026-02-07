package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.ClinicRoom;

import java.util.List;

@Repository
public interface ClinicRoomRepository extends JpaRepository<ClinicRoom, Long> {
    

    List<ClinicRoom> findByCenterId(Long centerId);

    List<ClinicRoom> findByCenterIdAndIsActive(Long centerId, Boolean isActive);

    ClinicRoom findByCenterIdAndRoomNumber(Long centerId, String roomNumber);
}


