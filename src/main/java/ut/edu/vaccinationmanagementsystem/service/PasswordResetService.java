package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.PasswordResetToken;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.AuthProvider;
import ut.edu.vaccinationmanagementsystem.repository.PasswordResetTokenRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public PasswordResetToken createPasswordResetToken(User user) {
        // Xóa token cũ nếu có
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        
        // Tạo token mới
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusHours(1)); // Hết hạn sau 1 giờ
        token.setUsed(false);
        
        token = tokenRepository.save(token);
        
        // Gửi email reset password
        emailService.sendPasswordResetEmail(token);
        
        return token;
    }

    public PasswordResetToken validateToken(String tokenString) {
        PasswordResetToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new RuntimeException("Invalid reset password token"));
        
        if (!token.isValid()) {
            if (token.isExpired()) {
                throw new RuntimeException("Reset password token has expired. Please request a new one.");
            }
            if (token.getUsed()) {
                throw new RuntimeException("Reset password token has already been used.");
            }
        }
        
        return token;
    }

    public void resetPassword(String tokenString, String newPassword) {
        // Validate token
        PasswordResetToken token = validateToken(tokenString);
        
        // Validate password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        
        // Đánh dấu token đã sử dụng
        token.setUsed(true);
        tokenRepository.save(token);
        
        // Cập nhật password
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    

    public void resendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Chỉ cho phép reset password cho email/password users
        if (user.getAuthProvider() != AuthProvider.EMAIL) {
            throw new RuntimeException("Cannot reset password for OAuth2 accounts. Please use your OAuth2 provider to login.");
        }
        
        // Tạo token mới và gửi email
        createPasswordResetToken(user);
    }
}

