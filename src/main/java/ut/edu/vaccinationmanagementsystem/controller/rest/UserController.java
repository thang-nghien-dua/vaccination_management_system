package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.UserUpdateDTO;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        // Nếu là OAuth2 user
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return customOAuth2User.getUser();
        }
        
        // Nếu là email/password login
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
    
    /**
     * GET /api/users/profile
     * Xem profile của user hiện tại
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            User user = getCurrentUser();
            
            // Không trả về password
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("email", user.getEmail());
            profile.put("fullName", user.getFullName());
            profile.put("phoneNumber", user.getPhoneNumber());
            profile.put("dayOfBirth", user.getDayOfBirth());
            profile.put("gender", user.getGender());
            profile.put("address", user.getAddress());
            profile.put("citizenId", user.getCitizenId());
            profile.put("role", user.getRole().name());
            profile.put("status", user.getStatus().name());
            profile.put("authProvider", user.getAuthProvider().name());
            profile.put("createAt", user.getCreateAt());
            
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * PUT /api/users/profile
     * Cập nhật profile của user hiện tại
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateDTO dto) {
        try {
            User currentUser = getCurrentUser();
            User updatedUser = userService.updateProfile(currentUser.getId(), dto);
            
            // Không trả về password
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", updatedUser.getId());
            profile.put("email", updatedUser.getEmail());
            profile.put("fullName", updatedUser.getFullName());
            profile.put("phoneNumber", updatedUser.getPhoneNumber());
            profile.put("phoneVerified", updatedUser.getPhoneVerified());
            profile.put("dayOfBirth", updatedUser.getDayOfBirth());
            profile.put("gender", updatedUser.getGender());
            profile.put("address", updatedUser.getAddress());
            profile.put("citizenId", updatedUser.getCitizenId());
            profile.put("role", updatedUser.getRole().name());
            profile.put("status", updatedUser.getStatus().name());
            profile.put("authProvider", updatedUser.getAuthProvider().name());
            profile.put("createAt", updatedUser.getCreateAt());
            
            // Kiểm tra nếu số điện thoại mới chưa được xác thực
            if (dto.getPhoneNumber() != null && 
                (updatedUser.getPhoneVerified() == null || !updatedUser.getPhoneVerified())) {
                profile.put("message", "Profile updated successfully. Please verify your phone number.");
                profile.put("requiresPhoneVerification", true);
            } else {
                profile.put("message", "Profile updated successfully");
                profile.put("requiresPhoneVerification", false);
            }
            
            return ResponseEntity.ok(profile);
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
}

