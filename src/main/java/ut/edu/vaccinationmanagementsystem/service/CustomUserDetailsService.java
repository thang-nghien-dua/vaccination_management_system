package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;

import java.util.ArrayList;
import java.util.Collection;

/**
 * UserDetailsService để Spring Security authenticate user bằng email/password
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserService userService;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userService.getUserByEmail(email);
            
            // Chỉ cho phép đăng nhập bằng email/password (không phải OAuth2)
            if (user.getAuthProvider() != ut.edu.vaccinationmanagementsystem.entity.enums.AuthProvider.EMAIL) {
                throw new UsernameNotFoundException("Please use OAuth2 login for this account");
            }
            
            // Kiểm tra user status - QUAN TRỌNG: User INACTIVE không thể đăng nhập
            if (user.getStatus() == UserStatus.INACTIVE) {
                throw new org.springframework.security.authentication.DisabledException("Email chưa được xác thực. Vui lòng kiểm tra email và click vào link xác thực.");
            }
            
            if (user.getStatus() == UserStatus.LOCKED) {
                throw new org.springframework.security.authentication.LockedException("Tài khoản đã bị khóa");
            }
            
            return new CustomUserDetails(user);
        } catch (RuntimeException e) {
            throw new UsernameNotFoundException("User not found with email: " + email, e);
        }
    }
}


