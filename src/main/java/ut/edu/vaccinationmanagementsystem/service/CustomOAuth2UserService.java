package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.AuthProvider;
import ut.edu.vaccinationmanagementsystem.entity.enums.Gender;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    private UserService userService;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // Lấy thông tin từ OAuth2 provider
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = null;
        String name = null;
        String providerId = null;
        Gender gender = null;
        AuthProvider authProvider = null;
        
        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            providerId = (String) attributes.get("sub"); // Google user ID
            
            if (providerId == null || providerId.isEmpty()) {
                OAuth2Error oauth2Error = new OAuth2Error("google_id_not_found", 
                    "Google user ID not found. Please try again.", null);
                throw new OAuth2AuthenticationException(oauth2Error);
            }
            
            // Google gender: "male", "female", hoặc null
            String genderStr = (String) attributes.get("gender");
            gender = mapGenderFromProvider(genderStr);
            
            authProvider = AuthProvider.GOOGLE;
        } else if ("facebook".equals(registrationId)) {
            // Facebook attributes structure
            providerId = (String) attributes.get("id"); // Facebook user ID
            name = (String) attributes.get("name");
            email = (String) attributes.get("email");
            
            if (providerId == null || providerId.isEmpty()) {
                OAuth2Error oauth2Error = new OAuth2Error("facebook_id_not_found", 
                    "Facebook user ID not found. Please try again.", null);
                throw new OAuth2AuthenticationException(oauth2Error);
            }
            
            // Facebook có thể không trả về email nếu user chưa cấp quyền email
            // Hoặc nếu email không được public
            if (email == null || email.isEmpty()) {
                // Tạo email tạm từ Facebook ID để user có thể cập nhật sau
                email = "fb_" + providerId + "@facebook.temp";
            }
            
            // Facebook gender: "male", "female", hoặc null
            String genderStr = (String) attributes.get("gender");
            gender = mapGenderFromProvider(genderStr);
            
            authProvider = AuthProvider.FACEBOOK;
        } else {
            OAuth2Error oauth2Error = new OAuth2Error("unsupported_provider", 
                "Unsupported OAuth2 provider: " + registrationId, null);
            throw new OAuth2AuthenticationException(oauth2Error);
        }
        
        if (email == null || email.isEmpty()) {
            OAuth2Error oauth2Error = new OAuth2Error("email_not_found", 
                "Email not found in OAuth2 user attributes. Please ensure you have granted email permission.", null);
            throw new OAuth2AuthenticationException(oauth2Error);
        }
        
        // Tạo hoặc cập nhật user trong database
        try {
            User user = userService.processOAuth2User(email, name, providerId, authProvider, gender);
            
            // Kiểm tra user status - không cho phép đăng nhập nếu bị khóa
            if (user.getStatus() == UserStatus.LOCKED) {
                OAuth2Error oauth2Error = new OAuth2Error("account_locked", 
                    "Tài khoản của bạn đã bị khóa. Vui lòng gửi email đến thangtv5280@gmail.com để được hỗ trợ.", null);
                throw new OAuth2AuthenticationException(oauth2Error);
            }
            
            // Tạo CustomOAuth2User với thông tin từ database
            return new CustomOAuth2User(oAuth2User, user);
        } catch (OAuth2AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            OAuth2Error oauth2Error = new OAuth2Error("oauth2_processing_error", "Failed to process OAuth2 user: " + e.getMessage(), null);
            throw new OAuth2AuthenticationException(oauth2Error, e);
        }
    }
    

    private Gender mapGenderFromProvider(String genderStr) {
        if (genderStr == null || genderStr.isEmpty()) {
            return null;
        }
        
        String lowerGender = genderStr.toLowerCase().trim();
        switch (lowerGender) {
            case "male":
            case "m":
                return Gender.MALE;
            case "female":
            case "f":
                return Gender.FEMALE;
            default:
                return Gender.OTHER;
        }
    }
}

