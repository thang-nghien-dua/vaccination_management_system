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
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.StaffInfoRepository staffInfoRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.VaccinationCenterRepository vaccinationCenterRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.NotificationRepository notificationRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository appointmentRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.FamilyMemberRepository familyMemberRepository;
    
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.ScreeningRepository screeningRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.AdverseReactionRepository adverseReactionRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.AppointmentHistoryRepository appointmentHistoryRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.WorkScheduleRepository workScheduleRepository;
    
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
        
        User savedUser = userRepository.save(user);
        
        // Handle StaffInfo if role is a staff member
        if (dto.getRole() == Role.DOCTOR || dto.getRole() == Role.NURSE || dto.getRole() == Role.RECEPTIONIST) {
            ut.edu.vaccinationmanagementsystem.entity.StaffInfo staffInfo = new ut.edu.vaccinationmanagementsystem.entity.StaffInfo();
            staffInfo.setUser(savedUser);
            staffInfo.setEmployeeId(dto.getEmployeeId() != null ? dto.getEmployeeId() : "EMP" + savedUser.getId());
            staffInfo.setSpecialization(dto.getSpecialization());
            staffInfo.setLicenseNumber(dto.getLicenseNumber());
            staffInfo.setHireDate(dto.getHireDate() != null ? dto.getHireDate() : LocalDate.now());
            staffInfo.setDepartment(dto.getDepartment());
            
            if (dto.getCenterId() != null) {
                vaccinationCenterRepository.findById(dto.getCenterId()).ifPresent(staffInfo::setCenter);
            }
            
            staffInfoRepository.save(staffInfo);
        }
        
        return savedUser;
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
        
        User savedUser = userRepository.save(user);
        
        // Update StaffInfo if role is a staff member
        if (savedUser.getRole() == Role.DOCTOR || savedUser.getRole() == Role.NURSE || savedUser.getRole() == Role.RECEPTIONIST) {
            ut.edu.vaccinationmanagementsystem.entity.StaffInfo staffInfo = staffInfoRepository.findByUser(savedUser)
                    .orElse(new ut.edu.vaccinationmanagementsystem.entity.StaffInfo());
            
            if (staffInfo.getUser() == null) {
                staffInfo.setUser(savedUser);
            }
            
            // Tự động generate employee_id nếu chưa có
            if (staffInfo.getEmployeeId() == null || staffInfo.getEmployeeId().trim().isEmpty()) {
                if (dto.getEmployeeId() != null && !dto.getEmployeeId().trim().isEmpty()) {
                    staffInfo.setEmployeeId(dto.getEmployeeId().trim());
                } else {
                    // Generate employee_id tự động: EMP + user_id
                    staffInfo.setEmployeeId("EMP" + savedUser.getId());
                }
            } else if (dto.getEmployeeId() != null && !dto.getEmployeeId().trim().isEmpty()) {
                staffInfo.setEmployeeId(dto.getEmployeeId().trim());
            }
            
            if (dto.getSpecialization() != null) staffInfo.setSpecialization(dto.getSpecialization());
            if (dto.getLicenseNumber() != null) staffInfo.setLicenseNumber(dto.getLicenseNumber());
            if (dto.getHireDate() != null) staffInfo.setHireDate(dto.getHireDate());
            if (dto.getDepartment() != null) staffInfo.setDepartment(dto.getDepartment());
            
            if (dto.getCenterId() != null) {
                vaccinationCenterRepository.findById(dto.getCenterId()).ifPresent(staffInfo::setCenter);
            }
            
            staffInfoRepository.save(staffInfo);
        } else {
            // Nếu role không phải staff nữa, xóa StaffInfo nếu có
            staffInfoRepository.findByUser(savedUser).ifPresent(staffInfoRepository::delete);
        }
        
        return savedUser;
    }
    
    /**
     * Admin xóa user
     * Xóa tất cả các bản ghi liên quan trước khi xóa user để tránh foreign key constraint
     */
    @Transactional
    public void deleteUserByAdmin(Long userId) {
        User user = getUserById(userId);
        
        // Không cho phép xóa chính mình
        // (Có thể thêm check này nếu cần)
        
        // Xóa các bản ghi liên quan trước khi xóa user
        
        // 1. Xóa notifications
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).forEach(notificationRepository::delete);
        
        // 2. Xóa family members
        familyMemberRepository.findByUserOrderByCreatedAtDesc(user).forEach(familyMemberRepository::delete);
        
        // 3. Xóa work schedules
        workScheduleRepository.findByUserId(userId).forEach(workScheduleRepository::delete);
        
        // 4. Xóa appointment histories (nơi user là người thay đổi)
        // Sử dụng findAll và filter vì không có findByChangedById
        appointmentHistoryRepository.findAll().stream()
            .filter(history -> history.getChangedBy() != null && history.getChangedBy().getId().equals(userId))
            .forEach(appointmentHistoryRepository::delete);
        
        // 5. Xóa screenings (nơi user là bác sĩ)
        screeningRepository.findByDoctorIdOrderByScreenedAtDesc(userId).forEach(screeningRepository::delete);
        
        // 6. Xóa adverse reactions (nơi user là người xử lý)
        adverseReactionRepository.findByHandledById(userId).forEach(adverseReactionRepository::delete);
        
        // 7. Xử lý vaccination records
        // 7a. Xóa tất cả records có user là người được tiêm (vì user_id là NOT NULL)
        vaccinationRecordRepository.findByUserIdOrderByInjectionDateDesc(userId).forEach(vaccinationRecordRepository::delete);
        
        // 7b. Cập nhật records có nurse là user này (set nurse = null)
        vaccinationRecordRepository.findAll().stream()
            .filter(record -> record.getNurse() != null && record.getNurse().getId().equals(userId))
            .forEach(record -> {
                record.setNurse(null);
                vaccinationRecordRepository.save(record);
            });
        
        // 8. Cập nhật appointments (set booked_by_user_id và booked_for_user_id = null)
        // Lưu ý: Có thể cần xóa appointments hoặc set null tùy business logic
        appointmentRepository.findByBookedByUserId(userId).forEach(appointment -> {
            appointment.setBookedByUser(null);
            appointmentRepository.save(appointment);
        });
        appointmentRepository.findByBookedForUserId(userId).forEach(appointment -> {
            appointment.setBookedForUser(null);
            appointmentRepository.save(appointment);
        });
        
        // 9. Xóa StaffInfo nếu có
        staffInfoRepository.findByUser(user).ifPresent(staffInfoRepository::delete);
        
        // 10. Cuối cùng mới xóa user
        userRepository.delete(user);
    }
}

