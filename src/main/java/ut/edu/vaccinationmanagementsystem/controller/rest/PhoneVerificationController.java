package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.entity.FamilyMember;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.repository.FamilyMemberRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.PhoneVerificationService;
import ut.edu.vaccinationmanagementsystem.service.SmsException;
import ut.edu.vaccinationmanagementsystem.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/phone")
public class PhoneVerificationController {
    
    @Autowired
    private PhoneVerificationService phoneVerificationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FamilyMemberRepository familyMemberRepository;
    
    /**
     * Lấy user hiện tại từ SecurityContext
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
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getUser();
        }
        
        // Fallback
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
    
    /**
     * POST /api/phone/request-verification
     * Yêu cầu gửi mã OTP cho user hiện tại
     */
    @PostMapping("/request-verification")
    public ResponseEntity<?> requestVerification(@RequestBody Map<String, String> request) {
        try {
            User currentUser = getCurrentUser();
            String phoneNumber = request.get("phoneNumber");
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Số điện thoại không được để trống");
                return ResponseEntity.badRequest().body(error);
            }
            
            phoneVerificationService.sendVerificationCodeForUser(currentUser.getId(), phoneNumber);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mã xác thực đã được gửi đến số điện thoại của bạn");
            return ResponseEntity.ok(response);
            
        } catch (SmsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể gửi mã xác thực: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/phone/request-verification/family-member/{familyMemberId}
     * Yêu cầu gửi mã OTP cho family member
     */
    @PostMapping("/request-verification/family-member/{familyMemberId}")
    public ResponseEntity<?> requestVerificationForFamilyMember(
            @PathVariable Long familyMemberId,
            @RequestBody Map<String, String> request) {
        try {
            User currentUser = getCurrentUser();
            String phoneNumber = request.get("phoneNumber");
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Số điện thoại không được để trống");
                return ResponseEntity.badRequest().body(error);
            }
            
            phoneVerificationService.sendVerificationCodeForFamilyMember(familyMemberId, phoneNumber);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mã xác thực đã được gửi đến số điện thoại");
            return ResponseEntity.ok(response);
            
        } catch (SmsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Không thể gửi mã xác thực: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/phone/verify
     * Xác thực mã OTP cho user hiện tại
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        try {
            User currentUser = getCurrentUser();
            String code = request.get("code");
            
            if (code == null || code.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mã xác thực không được để trống");
                return ResponseEntity.badRequest().body(error);
            }
            
            boolean verified = phoneVerificationService.verifyCodeForUser(currentUser.getId(), code);
            
            if (verified) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Xác thực số điện thoại thành công");
                response.put("verified", "true");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mã xác thực không đúng hoặc đã hết hạn");
                return ResponseEntity.badRequest().body(error);
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/phone/verify/family-member/{familyMemberId}
     * Xác thực mã OTP cho family member
     */
    @PostMapping("/verify/family-member/{familyMemberId}")
    public ResponseEntity<?> verifyCodeForFamilyMember(
            @PathVariable Long familyMemberId,
            @RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            
            if (code == null || code.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mã xác thực không được để trống");
                return ResponseEntity.badRequest().body(error);
            }
            
            boolean verified = phoneVerificationService.verifyCodeForFamilyMember(familyMemberId, code);
            
            if (verified) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Xác thực số điện thoại thành công");
                response.put("verified", "true");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mã xác thực không đúng hoặc đã hết hạn");
                return ResponseEntity.badRequest().body(error);
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/phone/verification-status
     * Kiểm tra trạng thái xác thực số điện thoại của user hiện tại
     */
    @GetMapping("/verification-status")
    public ResponseEntity<?> getVerificationStatus() {
        try {
            User currentUser = getCurrentUser();
            boolean verified = phoneVerificationService.isPhoneVerifiedForUser(currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("verified", verified);
            response.put("phoneNumber", currentUser.getPhoneNumber());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/phone/verification-status/family-member/{familyMemberId}
     * Kiểm tra trạng thái xác thực số điện thoại của family member
     */
    @GetMapping("/verification-status/family-member/{familyMemberId}")
    public ResponseEntity<?> getVerificationStatusForFamilyMember(@PathVariable Long familyMemberId) {
        try {
            // Lấy family member để lấy phone number
            User currentUser = getCurrentUser();
            FamilyMember familyMember = 
                familyMemberRepository.findById(familyMemberId)
                    .orElseThrow(() -> new RuntimeException("Family member not found"));
            
            // Kiểm tra family member thuộc về current user
            if (!familyMember.getUser().getId().equals(currentUser.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Family member does not belong to current user");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            boolean verified = phoneVerificationService.isPhoneVerifiedForFamilyMember(familyMemberId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("verified", verified);
            response.put("phoneNumber", familyMember.getPhoneNumber());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

