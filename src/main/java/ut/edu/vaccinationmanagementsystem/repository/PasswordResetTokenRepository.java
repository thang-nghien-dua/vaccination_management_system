package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.PasswordResetToken;
import ut.edu.vaccinationmanagementsystem.entity.User;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    /**
     * Tìm token theo token string
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Tìm token theo user
     */
    Optional<PasswordResetToken> findByUser(User user);
    
    /**
     * Xóa token đã hết hạn
     */
    void deleteByExpiresAtBefore(java.time.LocalDateTime now);
}




