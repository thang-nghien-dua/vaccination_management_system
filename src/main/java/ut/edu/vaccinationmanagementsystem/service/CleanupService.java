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


@Service
public class CleanupService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    
    @Autowired
    private UserService userService;
    

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
                
                // Xóa user (sử dụng deleteUserByAdmin để xử lý foreign key constraints)
                userService.deleteUserByAdmin(user.getId());
                deletedCount++;
            } catch (Exception e) {
                System.err.println("Failed to delete user " + user.getEmail() + ": " + e.getMessage());
            }
        }
        
        if (deletedCount > 0) {
            System.out.println("Cleaned up " + deletedCount + " inactive users older than 30 days");
        }
    }
    

    @Scheduled(cron = "0 0 3 * * ?") // Chạy mỗi ngày lúc 3 giờ sáng
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        emailVerificationTokenRepository.deleteByExpiresAtBefore(now);
        System.out.println("Cleaned up expired email verification tokens");
    }
}

