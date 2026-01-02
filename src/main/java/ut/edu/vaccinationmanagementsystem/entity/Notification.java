package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.NotificationStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * Thông báo gửi cho người dùng
 */
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user; // Người nhận thông báo (có thể null nếu gửi cho tất cả)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // Loại thông báo (EMAIL, IN_APP)
    
    @Column(nullable = false)
    private String title; // Tiêu đề thông báo
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // Nội dung thông báo
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status; // Trạng thái (PENDING, SENT, FAILED)
    
    @Column(nullable = true)
    private LocalDateTime sentAt; // Thời gian gửi thông báo
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // Thời gian tạo thông báo
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public NotificationStatus getStatus() {
        return status;
    }
    
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


