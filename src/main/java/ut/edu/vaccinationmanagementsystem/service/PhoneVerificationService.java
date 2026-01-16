package ut.edu.vaccinationmanagementsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.FamilyMember;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.repository.FamilyMemberRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service xử lý xác thực số điện thoại
 */
@Service
public class PhoneVerificationService {
    
    private static final Logger log = LoggerFactory.getLogger(PhoneVerificationService.class);
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FamilyMemberRepository familyMemberRepository;
    
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;
    
    /**
     * Gửi mã xác thực cho user
     */
    @Transactional
    public void sendVerificationCodeForUser(Long userId, String phoneNumber) throws SmsException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate mã OTP
        String code = generateCode();
        
        // Normalize số điện thoại mới
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        
        // Kiểm tra xem số điện thoại có thay đổi không
        String currentNormalizedPhone = user.getPhoneNumber() != null 
            ? normalizePhoneNumber(user.getPhoneNumber()) 
            : "";
        boolean phoneNumberChanged = !normalizedPhone.equals(currentNormalizedPhone);
        
        // Chỉ reset phoneVerified nếu số điện thoại thay đổi
        // Nếu cùng số và đã xác thực, giữ nguyên trạng thái verified nhưng vẫn tạo mã mới
        if (phoneNumberChanged) {
            user.setPhoneVerified(false);
        }
        // Nếu cùng số, giữ nguyên phoneVerified (có thể là true hoặc false)
        
        // Lưu số điện thoại và mã OTP mới
        user.setPhoneNumber(normalizedPhone);
        user.setPhoneVerificationCode(code);
        user.setPhoneVerificationExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        userRepository.save(user);
        
        // Gửi SMS
        String message = String.format(
            "Ma xac thuc cua ban la: %s. Ma co hieu luc trong %d phut. VacciCare",
            code, CODE_EXPIRY_MINUTES
        );
        
        smsService.sendSms(phoneNumber, message);
        
        log.info("Verification code sent to user {} phone: {}", userId, phoneNumber);
    }
    
    /**
     * Gửi mã xác thực cho family member
     */
    @Transactional
    public void sendVerificationCodeForFamilyMember(Long familyMemberId, String phoneNumber) throws SmsException {
        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
            .orElseThrow(() -> new RuntimeException("Family member not found"));
        
        // Generate mã OTP
        String code = generateCode();
        
        // Normalize số điện thoại mới
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        
        // Kiểm tra xem số điện thoại có thay đổi không
        String currentNormalizedPhone = familyMember.getPhoneNumber() != null 
            ? normalizePhoneNumber(familyMember.getPhoneNumber()) 
            : "";
        boolean phoneNumberChanged = !normalizedPhone.equals(currentNormalizedPhone);
        
        // Chỉ reset phoneVerified nếu số điện thoại thay đổi
        // Nếu cùng số và đã xác thực, giữ nguyên trạng thái verified nhưng vẫn tạo mã mới
        if (phoneNumberChanged) {
            familyMember.setPhoneVerified(false);
        }
        // Nếu cùng số, giữ nguyên phoneVerified (có thể là true hoặc false)
        
        // Lưu số điện thoại và mã OTP mới
        familyMember.setPhoneNumber(normalizedPhone);
        familyMember.setPhoneVerificationCode(code);
        familyMember.setPhoneVerificationExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        familyMemberRepository.save(familyMember);
        
        // Gửi SMS
        String message = String.format(
            "Ma xac thuc cua ban la: %s. Ma co hieu luc trong %d phut. VacciCare",
            code, CODE_EXPIRY_MINUTES
        );
        
        smsService.sendSms(phoneNumber, message);
        
        log.info("Verification code sent to family member {} phone: {}", familyMemberId, phoneNumber);
    }
    
    /**
     * Xác thực mã OTP cho user
     */
    @Transactional
    public boolean verifyCodeForUser(Long userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getPhoneVerificationCode() == null) {
            log.warn("No verification code found for user {}", userId);
            return false;
        }
        
        if (user.getPhoneVerificationExpiresAt() == null || 
            LocalDateTime.now().isAfter(user.getPhoneVerificationExpiresAt())) {
            log.warn("Verification code expired for user {}", userId);
            return false;
        }
        
        if (!user.getPhoneVerificationCode().equals(code)) {
            log.warn("Invalid verification code for user {}", userId);
            return false;
        }
        
        // Xác thực thành công
        user.setPhoneVerified(true);
        user.setPhoneVerificationCode(null);
        user.setPhoneVerificationExpiresAt(null);
        userRepository.save(user);
        
        log.info("Phone verified successfully for user {}", userId);
        return true;
    }
    
    /**
     * Xác thực mã OTP cho family member
     */
    @Transactional
    public boolean verifyCodeForFamilyMember(Long familyMemberId, String code) {
        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
            .orElseThrow(() -> new RuntimeException("Family member not found"));
        
        if (familyMember.getPhoneVerificationCode() == null) {
            log.warn("No verification code found for family member {}", familyMemberId);
            return false;
        }
        
        if (familyMember.getPhoneVerificationExpiresAt() == null || 
            LocalDateTime.now().isAfter(familyMember.getPhoneVerificationExpiresAt())) {
            log.warn("Verification code expired for family member {}", familyMemberId);
            return false;
        }
        
        if (!familyMember.getPhoneVerificationCode().equals(code)) {
            log.warn("Invalid verification code for family member {}", familyMemberId);
            return false;
        }
        
        // Xác thực thành công
        familyMember.setPhoneVerified(true);
        familyMember.setPhoneVerificationCode(null);
        familyMember.setPhoneVerificationExpiresAt(null);
        familyMemberRepository.save(familyMember);
        
        log.info("Phone verified successfully for family member {}", familyMemberId);
        return true;
    }
    
    /**
     * Kiểm tra số điện thoại đã được xác thực chưa (cho user)
     */
    public boolean isPhoneVerifiedForUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return user.getPhoneVerified() != null && user.getPhoneVerified() 
            && user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty();
    }
    
    /**
     * Kiểm tra số điện thoại cụ thể đã được xác thực chưa (cho user)
     * @param userId User ID
     * @param phoneNumber Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại đã được xác thực và khớp với số trong form
     */
    public boolean isPhoneVerifiedForUser(Long userId, String phoneNumber) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            return false;
        }
        
        // Normalize phone numbers để so sánh (loại bỏ khoảng trắng, dấu gạch ngang)
        String normalizedUserPhone = normalizePhoneNumber(user.getPhoneNumber());
        String normalizedInputPhone = normalizePhoneNumber(phoneNumber);
        
        return user.getPhoneVerified() != null && user.getPhoneVerified()
            && normalizedUserPhone.equals(normalizedInputPhone);
    }
    
    /**
     * Kiểm tra số điện thoại đã được xác thực chưa (cho family member)
     */
    public boolean isPhoneVerifiedForFamilyMember(Long familyMemberId) {
        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
            .orElseThrow(() -> new RuntimeException("Family member not found"));
        
        return familyMember.getPhoneVerified() != null && familyMember.getPhoneVerified()
            && familyMember.getPhoneNumber() != null && !familyMember.getPhoneNumber().trim().isEmpty();
    }
    
    /**
     * Kiểm tra số điện thoại cụ thể đã được xác thực chưa (cho family member)
     * @param familyMemberId Family member ID
     * @param phoneNumber Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại đã được xác thực và khớp với số trong form
     */
    public boolean isPhoneVerifiedForFamilyMember(Long familyMemberId, String phoneNumber) {
        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
            .orElseThrow(() -> new RuntimeException("Family member not found"));
        
        if (familyMember.getPhoneNumber() == null || familyMember.getPhoneNumber().trim().isEmpty()) {
            return false;
        }
        
        // Normalize phone numbers để so sánh
        String normalizedMemberPhone = normalizePhoneNumber(familyMember.getPhoneNumber());
        String normalizedInputPhone = normalizePhoneNumber(phoneNumber);
        
        return familyMember.getPhoneVerified() != null && familyMember.getPhoneVerified()
            && normalizedMemberPhone.equals(normalizedInputPhone);
    }
    
    /**
     * Normalize số điện thoại (loại bỏ khoảng trắng, dấu gạch ngang, dấu ngoặc đơn)
     * Public để có thể dùng ở nơi khác nếu cần
     */
    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        return phoneNumber.replaceAll("[\\s\\-\\(\\)]", "").trim();
    }
    
    /**
     * Generate mã OTP 6 số
     */
    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}

