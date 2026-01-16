package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Token reset password
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String token; // Token reset password (UUID)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User cần reset password
    
    @Column(nullable = false)
    private LocalDateTime expiresAt; // Thời gian hết hạn (1 giờ)
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo token
    
    @Column(nullable = false)
    private Boolean used = false; // Đã sử dụng chưa
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Boolean getUsed() {
        return used;
    }
    
    public void setUsed(Boolean used) {
        this.used = used;
    }
    
    /**
     * Kiểm tra token đã hết hạn chưa
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Kiểm tra token có hợp lệ không (chưa dùng và chưa hết hạn)
     */
    public boolean isValid() {
        return !used && !isExpired();
    }
}




