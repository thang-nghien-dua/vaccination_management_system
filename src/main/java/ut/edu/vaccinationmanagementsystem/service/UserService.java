package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.UserRegisterDTO;
import ut.edu.vaccinationmanagementsystem.dto.UserUpdateDTO;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.AuthProvider;
import ut.edu.vaccinationmanagementsystem.entity.enums.Gender;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;
import ut.edu.vaccinationmanagementsystem.exception.EmailAlreadyExistsException;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.EmailVerificationService;

import java.time.LocalDate;

/**
 * Service xử lý business logic cho User
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.EmailVerificationTokenRepository emailVerificationTokenRepository;
    
    // Đăng ký tài khoản mới
    public User register(UserRegisterDTO dto) {
        // Validate các field bắt buộc
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        
        // Validate email format (đơn giản)
        if (!dto.getEmail().contains("@")) {
            throw new RuntimeException("Invalid email format");
        }
        
        // Validate password length
        if (dto.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        
        // Kiểm tra email đã tồn tại chưa
        String emailLower = dto.getEmail().trim().toLowerCase();
        User existingUser = userRepository.findByEmail(emailLower).orElse(null);
        
        if (existingUser != null) {
            // Email đã tồn tại - throw exception với thông tin status
            throw new EmailAlreadyExistsException(emailLower, existingUser.getStatus());
        }
        
        // Kiểm tra citizenId nếu có
        if (dto.getCitizenId() != null && !dto.getCitizenId().trim().isEmpty()) {
            // Có thể thêm validation cho citizenId nếu cần
        }
        
        // Tạo User mới
        User user = new User();
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Mã hóa password
        user.setProviderUserId(null); // Email/password users không có provider ID
        user.setAuthProvider(AuthProvider.EMAIL);
        user.setFullName(dto.getFullName().trim());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setDayOfBirth(dto.getDayOfBirth());
        user.setGender(dto.getGender());
        user.setAddress(dto.getAddress());
        user.setCitizenId(dto.getCitizenId());
        user.setRole(Role.CUSTOMER); // Mặc định là CUSTOMER
        user.setStatus(UserStatus.INACTIVE); // Chưa kích hoạt cho đến khi xác thực email
        user.setCreateAt(LocalDate.now());
        
        try {
            user = userRepository.save(user);
            
            // Tạo token xác thực email và gửi email
            // Nếu gửi email thất bại, vẫn giữ user status = INACTIVE
            try {
                emailVerificationService.createVerificationToken(user);
            } catch (Exception emailException) {
                // Log lỗi nhưng không throw exception để user vẫn được tạo
                // User sẽ ở trạng thái INACTIVE và cần yêu cầu gửi lại email
                System.err.println("Failed to send verification email: " + emailException.getMessage());
                emailException.printStackTrace();
            }
            
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Đăng ký lại - Xóa user INACTIVE cũ và tạo user mới
     */
    public User reregister(UserRegisterDTO dto) {
        String emailLower = dto.getEmail().trim().toLowerCase();
        
        // Tìm user cũ
        User existingUser = userRepository.findByEmail(emailLower).orElse(null);
        
        if (existingUser != null) {
            // Chỉ cho phép đăng ký lại nếu user cũ chưa xác thực
            if (existingUser.getStatus() != UserStatus.INACTIVE) {
                throw new RuntimeException("Cannot reregister: Email is already active");
            }
            
            // Xóa token xác thực email cũ nếu có
            emailVerificationTokenRepository.findByUser(existingUser).ifPresent(emailVerificationTokenRepository::delete);
            
            // Xóa user cũ
            userRepository.delete(existingUser);
        }
        
        // Đăng ký user mới
        return register(dto);
    }
    
    // Lấy thông tin user theo ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    // Lấy thông tin user theo email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    // Cập nhật profile
    public User updateProfile(Long userId, UserUpdateDTO dto) {
        User user = getUserById(userId);
        
        // Validate fullName nếu có
        if (dto.getFullName() != null && dto.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name cannot be empty");
        }
        
        // Cập nhật các thông tin từ DTO
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName().trim());
        }
        if (dto.getPhoneNumber() != null) {
            String newPhoneNumber = dto.getPhoneNumber().trim();
            String oldPhoneNumber = user.getPhoneNumber() != null ? user.getPhoneNumber().trim() : null;
            
            // Normalize phone numbers để so sánh
            String normalizedNewPhone = normalizePhoneNumber(newPhoneNumber);
            String normalizedOldPhone = oldPhoneNumber != null ? normalizePhoneNumber(oldPhoneNumber) : null;
            
            // Nếu số điện thoại thay đổi hoặc số cũ chưa được xác thực, reset verification status
            if (!normalizedNewPhone.equals(normalizedOldPhone) || 
                user.getPhoneVerified() == null || !user.getPhoneVerified()) {
                user.setPhoneVerified(false);
                user.setPhoneVerificationCode(null);
                user.setPhoneVerificationExpiresAt(null);
            }
            
            user.setPhoneNumber(newPhoneNumber);
        }
        if (dto.getDayOfBirth() != null) {
            user.setDayOfBirth(dto.getDayOfBirth());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }
        if (dto.getCitizenId() != null) {
            // Kiểm tra citizenId trùng nếu có
            if (!dto.getCitizenId().trim().isEmpty()) {
                user.setCitizenId(dto.getCitizenId().trim());
            } else {
                user.setCitizenId(null);
            }
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Normalize số điện thoại (loại bỏ khoảng trắng, dấu gạch ngang, dấu ngoặc đơn)
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        return phoneNumber.replaceAll("[\\s\\-\\(\\)]", "").trim();
    }
    
    // Tạo hoặc cập nhật user từ OAuth2 (Google/Facebook)
    public User processOAuth2User(String email, String name, String providerId, AuthProvider authProvider, Gender gender) {
        // Tìm user theo email và authProvider
        User user = userRepository.findByEmailAndAuthProvider(email, authProvider)
                .orElse(null);
        
        if (user == null) {
            // Kiểm tra xem email đã tồn tại với provider khác chưa
            user = userRepository.findByEmail(email).orElse(null);
            
            if (user != null) {
                // Email đã tồn tại với provider khác, không cho phép
                throw new RuntimeException("Email already registered with different provider");
            }
            
            // Tạo user mới
            user = new User();
            user.setEmail(email);
            user.setPassword(null); // OAuth2 không có password
            user.setProviderUserId(providerId); // Lưu OAuth2 provider ID (Google/Facebook)
            user.setAuthProvider(authProvider);
            user.setFullName(name);
            user.setGender(gender); // Lưu gender từ OAuth2 provider
            user.setRole(Role.CUSTOMER); // Mặc định là CUSTOMER
            user.setStatus(UserStatus.ACTIVE);
            user.setCreateAt(LocalDate.now());
            
            return userRepository.save(user);
        } else {
            // Cập nhật thông tin nếu cần
            if (name != null && !name.equals(user.getFullName())) {
                user.setFullName(name);
            }
            // Cập nhật gender nếu có và chưa có
            if (gender != null && user.getGender() == null) {
                user.setGender(gender);
            }
            return userRepository.save(user);
        }
    }
    
    /**
     * Admin tạo user mới
     */
    public User createUserByAdmin(ut.edu.vaccinationmanagementsystem.dto.AdminUserDTO dto) {
        // Validate
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (dto.getRole() == null) {
            throw new RuntimeException("Role is required");
        }
        if (dto.getStatus() == null) {
            throw new RuntimeException("Status is required");
        }
        
        // Kiểm tra email đã tồn tại chưa
        String emailLower = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(emailLower)) {
            throw new RuntimeException("Email already exists: " + emailLower);
        }
        
        // Tạo user mới
        User user = new User();
        user.setEmail(emailLower);
        
        // Password: nếu có thì encode, nếu không thì null (cho OAuth users)
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setAuthProvider(AuthProvider.EMAIL);
        } else {
            user.setPassword(null);
            user.setAuthProvider(AuthProvider.EMAIL); // Mặc định EMAIL
        }
        
        user.setFullName(dto.getFullName().trim());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setDayOfBirth(dto.getDayOfBirth());
        user.setGender(dto.getGender());
        user.setAddress(dto.getAddress());
        user.setCitizenId(dto.getCitizenId());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        user.setCreateAt(LocalDate.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Admin cập nhật user
     */
    public User updateUserByAdmin(Long userId, ut.edu.vaccinationmanagementsystem.dto.AdminUserDTO dto) {
        User user = getUserById(userId);
        
        // Validate
        if (dto.getFullName() != null && dto.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name cannot be empty");
        }
        
        // Cập nhật email (nếu thay đổi)
        if (dto.getEmail() != null && !dto.getEmail().trim().equalsIgnoreCase(user.getEmail())) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email already exists: " + newEmail);
            }
            user.setEmail(newEmail);
        }
        
        // Cập nhật password (nếu có)
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        // Cập nhật các thông tin khác
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName().trim());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber().trim());
        }
        if (dto.getDayOfBirth() != null) {
            user.setDayOfBirth(dto.getDayOfBirth());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }
        if (dto.getCitizenId() != null) {
            user.setCitizenId(dto.getCitizenId().trim().isEmpty() ? null : dto.getCitizenId().trim());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Admin xóa user
     */
    public void deleteUserByAdmin(Long userId) {
        User user = getUserById(userId);
        
        // Không cho phép xóa chính mình
        // (Có thể thêm check này nếu cần)
        
        // Xóa user
        userRepository.delete(user);
    }
}

