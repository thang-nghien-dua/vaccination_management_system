package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;
import ut.edu.vaccinationmanagementsystem.repository.EmailVerificationTokenRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service để cleanup dữ liệu cũ
 */
@Service
public class CleanupService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    
    /**
     * Xóa user INACTIVE sau 30 ngày không xác thực
     * Chạy mỗi ngày lúc 2 giờ sáng
     */
    @Scheduled(cron = "0 0 2 * * ?") // Chạy mỗi ngày lúc 2 giờ sáng
    @Transactional
    public void cleanupInactiveUsers() {
        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        
        // Tìm tất cả user INACTIVE được tạo trước 30 ngày
        List<User> inactiveUsers = userRepository.findInactiveUsersBeforeDate(UserStatus.INACTIVE, cutoffDate);
        
        int deletedCount = 0;
        for (User user : inactiveUsers) {
            try {
                // Xóa token xác thực email
                emailVerificationTokenRepository.findByUser(user).ifPresent(emailVerificationTokenRepository::delete);
                
                // Xóa user
                userRepository.delete(user);
                deletedCount++;
            } catch (Exception e) {
                System.err.println("Failed to delete user " + user.getEmail() + ": " + e.getMessage());
            }
        }
        
        if (deletedCount > 0) {
            System.out.println("Cleaned up " + deletedCount + " inactive users older than 30 days");
        }
    }
    
    /**
     * Xóa token đã hết hạn
     * Chạy mỗi ngày lúc 3 giờ sáng
     */
    @Scheduled(cron = "0 0 3 * * ?") // Chạy mỗi ngày lúc 3 giờ sáng
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        emailVerificationTokenRepository.deleteByExpiresAtBefore(now);
        System.out.println("Cleaned up expired email verification tokens");
    }
}

