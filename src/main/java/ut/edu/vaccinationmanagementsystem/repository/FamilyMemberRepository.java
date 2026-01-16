package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.FamilyMember;
import ut.edu.vaccinationmanagementsystem.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    
    /**
     * Tìm tất cả người thân của một user
     */
    List<FamilyMember> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Tìm người thân theo ID và user (để đảm bảo user chỉ có thể xem/sửa người thân của mình)
     */
    Optional<FamilyMember> findByIdAndUser(Long id, User user);
    
    /**
     * Kiểm tra xem user đã có người thân với citizenId này chưa
     */
    boolean existsByUserAndCitizenId(User user, String citizenId);
    
    /**
     * Đếm số lượng người thân của một user
     */
    long countByUser(User user);
}




