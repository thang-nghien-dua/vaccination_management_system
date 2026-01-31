package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.EmailVerificationToken;
import ut.edu.vaccinationmanagementsystem.entity.User;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    /**
     * Tìm token theo token string
     */
    Optional<EmailVerificationToken> findByToken(String token);
    
    /**
     * Tìm token theo user
     */
    Optional<EmailVerificationToken> findByUser(User user);
    
    /**
     * Xóa token đã hết hạn
     */
    void deleteByExpiresAtBefore(java.time.LocalDateTime now);
}




