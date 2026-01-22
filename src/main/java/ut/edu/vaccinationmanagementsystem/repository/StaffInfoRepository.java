package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.StaffInfo;
import ut.edu.vaccinationmanagementsystem.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffInfoRepository extends JpaRepository<StaffInfo, Long> {
    Optional<StaffInfo> findByUser(User user);
    Optional<StaffInfo> findByUserId(Long userId);
    
    // Sử dụng query rõ ràng để tìm theo center_id
    @Query("SELECT si FROM StaffInfo si WHERE si.center.id = :centerId")
    List<StaffInfo> findByCenterId(@Param("centerId") Long centerId);
    
    Optional<StaffInfo> findByEmployeeId(String employeeId);
}
