package ut.edu.vaccinationmanagementsystem.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;


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
            
            // Redirect dựa trên role
            String redirectUrl = determineRedirectUrl(user);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
    
    private String determineRedirectUrl(User user) {
        // Redirect dựa trên role
        if (user.getRole() == null) {
            return "/home";
        }
        
        switch (user.getRole()) {
            case RECEPTIONIST:
                // Receptionist -> receptionist dashboard
                return "/receptionist/dashboard";
            case NURSE:
                // Nurse -> nurse dashboard
                return "/nurse/dashboard";
            case DOCTOR:
            case ADMIN:
                // Other staff roles -> home dashboard
                return "/home";
            case CUSTOMER:
            default:
                // Customer -> home
                return "/home";
        }
    }
}

