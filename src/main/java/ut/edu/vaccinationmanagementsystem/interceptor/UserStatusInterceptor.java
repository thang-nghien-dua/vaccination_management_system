package ut.edu.vaccinationmanagementsystem.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

/**
 * Interceptor để kiểm tra trạng thái user trong mỗi request
 * Nếu user bị khóa (LOCKED), sẽ logout và redirect về login
 */
@Component
public class UserStatusInterceptor implements HandlerInterceptor {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Bỏ qua các request không cần authentication
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/login") || 
            path.startsWith("/register") ||
            path.startsWith("/oauth2/") ||
            path.startsWith("/error") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/") ||
            path.equals("/") ||
            path.equals("/logout")) {
            return true;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            User currentUser = null;
            
            // Lấy user từ authentication
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                currentUser = customOAuth2User.getUser();
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                currentUser = customUserDetails.getUser();
            } else {
                // Lấy từ database
                String email = authentication.getName();
                currentUser = userRepository.findByEmail(email).orElse(null);
            }
            
            // Kiểm tra user status
            if (currentUser != null) {
                // Refresh user từ database để lấy status mới nhất
                User freshUser = userRepository.findById(currentUser.getId()).orElse(null);
                
                if (freshUser != null && freshUser.getStatus() == UserStatus.LOCKED) {
                    // User bị khóa, logout và redirect về login
                    SecurityContextHolder.clearContext();
                    request.getSession().invalidate();
                    
                    // Redirect về login với thông báo
                    String loginUrl = "/login?locked=true";
                    if (request.getHeader("X-Requested-With") != null && 
                        request.getHeader("X-Requested-With").equals("XMLHttpRequest")) {
                        // Nếu là AJAX request, trả về JSON
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Tài khoản của bạn đã bị khóa. Vui lòng liên hệ thangtv5280@gmail.com để được hỗ trợ.\",\"redirect\":\"/login?locked=true\"}");
                        return false;
                    } else {
                        // Redirect về login
                        response.sendRedirect(loginUrl);
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
}

