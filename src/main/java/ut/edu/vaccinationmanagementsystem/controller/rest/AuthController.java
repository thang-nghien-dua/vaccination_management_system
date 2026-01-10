package ut.edu.vaccinationmanagementsystem.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.UserLoginDTO;
import ut.edu.vaccinationmanagementsystem.dto.UserRegisterDTO;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;
import ut.edu.vaccinationmanagementsystem.exception.EmailAlreadyExistsException;
import ut.edu.vaccinationmanagementsystem.service.EmailVerificationService;
import ut.edu.vaccinationmanagementsystem.service.PasswordResetService;
import ut.edu.vaccinationmanagementsystem.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    /**
     * POST /api/auth/register
     * Đăng ký tài khoản mới
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto) {
        try {
            User user = userService.register(dto);
            
            // Không trả về password
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EmailAlreadyExistsException e) {
            // Email đã tồn tại - trả về thông tin chi tiết
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Email already exists");
            error.put("email", e.getEmail());
            error.put("userStatus", e.getUserStatus().name());
            error.put("isActive", e.getUserStatus() == UserStatus.ACTIVE);
            error.put("isInactive", e.getUserStatus() == UserStatus.INACTIVE);
            
            if (e.getUserStatus() == UserStatus.ACTIVE) {
                error.put("message", "Email này đã được sử dụng. Bạn có muốn đăng nhập không?");
            } else {
                error.put("message", "Email này đã được đăng ký nhưng chưa xác thực.");
            }
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/auth/reregister
     * Đăng ký lại - Xóa user INACTIVE cũ và tạo user mới
     */
    @PostMapping("/reregister")
    public ResponseEntity<?> reregister(@RequestBody UserRegisterDTO dto) {
        try {
            User user = userService.reregister(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/auth/login
     * Đăng nhập bằng email/password
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        try {
            // Validate
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Password is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Authenticate - Nếu user INACTIVE, sẽ throw DisabledException
            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        dto.getEmail().toLowerCase(),
                        dto.getPassword()
                    )
                );
            } catch (org.springframework.security.authentication.DisabledException e) {
                // User chưa xác thực email
                User user = userService.getUserByEmail(dto.getEmail().toLowerCase());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email chưa được xác thực. Vui lòng kiểm tra email và click vào link xác thực.");
                error.put("email", user.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            } catch (org.springframework.security.authentication.LockedException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Lưu authentication vào SecurityContext
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            
            // Lưu SecurityContext vào Session để persist qua các request
            SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
            securityContextRepository.saveContext(securityContext, request, response);
            
            // Đảm bảo session được tạo và set cookie
            request.getSession(true);
            
            // Lấy thông tin user
            User user = userService.getUserByEmail(dto.getEmail().toLowerCase());
            
            Map<String, Object> loginResponse = new HashMap<>();
            loginResponse.put("message", "Login successful");
            loginResponse.put("userId", user.getId());
            loginResponse.put("email", user.getEmail());
            loginResponse.put("fullName", user.getFullName());
            loginResponse.put("role", user.getRole().name());
            
            return ResponseEntity.ok(loginResponse);
        } catch (org.springframework.security.core.AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/auth/logout
     * Đăng xuất
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        
        Map<String, String> message = new HashMap<>();
        message.put("message", "Logout successful");
        return ResponseEntity.ok(message);
    }
    
    /**
     * GET /api/auth/oauth2/google
     * Redirect đến Google OAuth2 login
     */
    @GetMapping("/oauth2/google")
    public ResponseEntity<?> googleLogin() {
        Map<String, String> message = new HashMap<>();
        message.put("message", "Redirect to Google OAuth2");
        message.put("url", "/oauth2/authorization/google");
        return ResponseEntity.ok(message);
    }
    
    /**
     * GET /api/auth/oauth2/facebook
     * Redirect đến Facebook OAuth2 login
     */
    @GetMapping("/oauth2/facebook")
    public ResponseEntity<?> facebookLogin() {
        Map<String, String> message = new HashMap<>();
        message.put("message", "Redirect to Facebook OAuth2");
        message.put("url", "/oauth2/authorization/facebook");
        return ResponseEntity.ok(message);
    }
    
    /**
     * GET /api/auth/verify-email
     * Xác thực email bằng token
     * Redirect đến trang thành công hoặc lỗi
     */
    @GetMapping("/verify-email")
    public org.springframework.web.servlet.ModelAndView verifyEmail(@RequestParam String token) {
        try {
            emailVerificationService.verifyEmail(token);
            // Redirect đến trang thành công
            return new org.springframework.web.servlet.ModelAndView("redirect:/verify-email-success?verified=true");
        } catch (RuntimeException e) {
            // Redirect đến trang lỗi với thông báo
            org.springframework.web.servlet.ModelAndView mav = new org.springframework.web.servlet.ModelAndView("verify-email-success");
            mav.addObject("error", e.getMessage());
            mav.addObject("verified", false);
            return mav;
        }
    }
    
    /**
     * POST /api/auth/resend-verification
     * Gửi lại email xác thực
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            emailVerificationService.resendVerificationEmail(email.trim().toLowerCase());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email xác thực đã được gửi lại. Vui lòng kiểm tra hộp thư của bạn.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * POST /api/auth/forgot-password
     * Yêu cầu reset password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            passwordResetService.resendPasswordResetEmail(email.trim().toLowerCase());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn.");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * POST /api/auth/reset-password
     * Đặt lại mật khẩu bằng token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");
            
            if (token == null || token.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "New password is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (!newPassword.equals(confirmPassword)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Passwords do not match");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            passwordResetService.resetPassword(token, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mật khẩu đã được đặt lại thành công. Bạn có thể đăng nhập ngay bây giờ.");
            response.put("redirect", "/login?passwordReset=true");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

