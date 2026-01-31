package ut.edu.vaccinationmanagementsystem.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Bảng trung gian để theo dõi user nào đã sử dụng voucher nào
 * Đảm bảo mỗi user chỉ dùng được 1 lần cho mỗi voucher
 */
@Entity
@Table(name = "user_vouchers", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "voucher_id"}))
public class UserVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // User đã sử dụng voucher
    
    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher; // Voucher đã được sử dụng
    
    @Column(nullable = false)
    private LocalDateTime usedAt; // Thời gian sử dụng voucher
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Voucher getVoucher() {
        return voucher;
    }
    
    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }
    
    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}




