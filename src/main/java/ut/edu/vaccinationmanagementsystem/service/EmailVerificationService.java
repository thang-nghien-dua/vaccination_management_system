package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.EmailVerificationToken;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;
import ut.edu.vaccinationmanagementsystem.repository.EmailVerificationTokenRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@Transactional
public class EmailVerificationService {
    
    @Autowired
    private EmailVerificationTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
    

    public EmailVerificationToken createVerificationToken(User user) {
        // Xóa token cũ nếu có
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        
        // Tạo token mới
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(24)); // Hết hạn sau 24 giờ
        token.setUsed(false);
        
        token = tokenRepository.save(token);
        
        // Gửi email xác thực
        emailService.sendVerificationEmail(token);
        
        return token;
    }
    

    public boolean verifyEmail(String tokenString) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        if (!token.isValid()) {
            if (token.isExpired()) {
                throw new RuntimeException("Verification token has expired. Please request a new one.");
            }
            if (token.getUsed()) {
                throw new RuntimeException("Verification token has already been used.");
            }
        }
        
        // Đánh dấu token đã sử dụng
        token.setUsed(true);
        tokenRepository.save(token);
        
        // Kích hoạt user
        User user = token.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user); // Lưu user sau khi cập nhật status
        
        return true;
    }
    

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Kiểm tra user đã xác thực chưa
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("Email has already been verified");
        }
        
        // Tạo token mới và gửi email
        createVerificationToken(user);
    }
}

