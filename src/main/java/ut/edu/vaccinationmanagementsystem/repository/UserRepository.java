package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.AuthProvider;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Tìm user theo email
    Optional<User> findByEmail(String email);
    
    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);
    
    // Tìm user theo email và authProvider
    Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);
    
    // Tìm user theo providerUserId
    Optional<User> findByProviderUserId(String providerUserId);
    
    // Kiểm tra providerUserId đã tồn tại chưa
    boolean existsByProviderUserId(String providerUserId);
    
    // Tìm user INACTIVE được tạo trước ngày chỉ định
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.createAt < :cutoffDate")
    List<User> findInactiveUsersBeforeDate(@Param("status") UserStatus status, @Param("cutoffDate") LocalDate cutoffDate);
    
    // Tìm user theo citizenId (CMND/CCCD)
    Optional<User> findByCitizenId(String citizenId);
}

