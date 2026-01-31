package ut.edu.vaccinationmanagementsystem.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;

import java.io.IOException;

/**
 * Handler xử lý sau khi đăng nhập OAuth2 thành công
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    @Autowired
    private UserService userService;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        // Lấy CustomOAuth2User từ authentication
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            User user = customOAuth2User.getUser();
            
            // Refresh user từ database để lấy status mới nhất
            User freshUser = userService.getUserById(user.getId());
            
            // Kiểm tra nếu user bị khóa
            if (freshUser.getStatus() == UserStatus.LOCKED) {
                // Logout và redirect về login với thông báo
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();
                response.sendRedirect("/login?locked=true");
                return;
            }
            
            // Redirect dựa trên role
            String redirectUrl = determineRedirectUrl(freshUser);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
    
    private String determineRedirectUrl(User user) {
        // Redirect dựa trên role
        if (user.getRole() == Role.DOCTOR) {
            return "/doctor/home";
        } else if (user.getRole() == Role.ADMIN) {
            return "/admin/home";
        } else if (user.getRole() == Role.NURSE) {
            return "/nurse/dashboard";
        } else if (user.getRole() == Role.RECEPTIONIST) {
            return "/receptionist/dashboard";
        } else {
            return "/home";
        }
    }
}

