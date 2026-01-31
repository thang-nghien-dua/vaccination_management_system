package ut.edu.vaccinationmanagementsystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.AuthProvider;
import ut.edu.vaccinationmanagementsystem.entity.enums.Gender;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;

import java.time.LocalDate;

/**
 * DataInitializer - Tự động tạo các tài khoản mặc định khi ứng dụng khởi động
 * Password mặc định cho tất cả user: 123456
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final String DEFAULT_PASSWORD = "123456";
    
    @Override
    public void run(String... args) throws Exception {
        createDefaultUsers();
    }
    
    private void createDefaultUsers() {
        // ADMIN
        createUserIfNotExists(
            "admin@vaccicare.com",
            "Nguyễn Văn Admin",
            "0901234567",
            LocalDate.of(1980, 1, 15),
            Gender.MALE,
            "123 Đường ABC, Quận 1, TP.HCM",
            "001234567890",
            Role.ADMIN,
            UserStatus.ACTIVE
        );
        
        // DOCTOR
        createUserIfNotExists(
            "doctor1@vaccicare.com",
            "Bác sĩ Nguyễn Thị Lan",
            "0901234568",
            LocalDate.of(1985, 5, 20),
            Gender.FEMALE,
            "456 Đường XYZ, Quận 2, TP.HCM",
            "001234567891",
            Role.DOCTOR,
            UserStatus.ACTIVE
        );
        
        createUserIfNotExists(
            "doctor2@vaccicare.com",
            "Bác sĩ Trần Văn Minh",
            "0901234569",
            LocalDate.of(1988, 8, 10),
            Gender.MALE,
            "789 Đường DEF, Quận 3, TP.HCM",
            "001234567892",
            Role.DOCTOR,
            UserStatus.ACTIVE
        );
        
        // NURSE
        createUserIfNotExists(
            "nurse1@vaccicare.com",
            "Y tá Lê Thị Hoa",
            "0901234570",
            LocalDate.of(1990, 3, 25),
            Gender.FEMALE,
            "321 Đường GHI, Quận 4, TP.HCM",
            "001234567893",
            Role.NURSE,
            UserStatus.ACTIVE
        );
        
        createUserIfNotExists(
            "nurse2@vaccicare.com",
            "Y tá Phạm Văn Tuấn",
            "0901234571",
            LocalDate.of(1992, 7, 15),
            Gender.MALE,
            "654 Đường JKL, Quận 5, TP.HCM",
            "001234567894",
            Role.NURSE,
            UserStatus.ACTIVE
        );
        
        createUserIfNotExists(
            "nurse3@vaccicare.com",
            "Y tá Hoàng Thị Mai",
            "0901234572",
            LocalDate.of(1991, 11, 30),
            Gender.FEMALE,
            "987 Đường MNO, Quận 6, TP.HCM",
            "001234567895",
            Role.NURSE,
            UserStatus.ACTIVE
        );
        
        // RECEPTIONIST
        createUserIfNotExists(
            "receptionist1@vaccicare.com",
            "Lễ tân Võ Thị Linh",
            "0901234573",
            LocalDate.of(1993, 4, 12),
            Gender.FEMALE,
            "147 Đường PQR, Quận 7, TP.HCM",
            "001234567896",
            Role.RECEPTIONIST,
            UserStatus.ACTIVE
        );
        
        createUserIfNotExists(
            "receptionist2@vaccicare.com",
            "Lễ tân Đặng Văn Nam",
            "0901234574",
            LocalDate.of(1994, 9, 22),
            Gender.MALE,
            "258 Đường STU, Quận 8, TP.HCM",
            "001234567897",
            Role.RECEPTIONIST,
            UserStatus.ACTIVE
        );
        
        // CUSTOMER
        createUserIfNotExists(
            "user1@test.com",
            "Nguyễn Văn An",
            "0901234575",
            LocalDate.of(1995, 1, 10),
            Gender.MALE,
            "369 Đường VWX, Quận 9, TP.HCM",
            "001234567898",
            Role.CUSTOMER,
            UserStatus.ACTIVE
        );
        
        createUserIfNotExists(
            "user2@test.com",
            "Trần Thị Bình",
            "0901234576",
            LocalDate.of(1996, 6, 20),
            Gender.FEMALE,
            "741 Đường YZA, Quận 10, TP.HCM",
            "001234567899",
            Role.CUSTOMER,
            UserStatus.ACTIVE
        );
        
        createUserIfNotExists(
            "user3@test.com",
            "Lê Văn Cường",
            "0901234577",
            LocalDate.of(1997, 12, 5),
            Gender.MALE,
            "852 Đường BCD, Quận 11, TP.HCM",
            "001234567900",
            Role.CUSTOMER,
            UserStatus.ACTIVE
        );
        
        createUserIfNotExists(
            "user4@test.com",
            "Phạm Thị Dung",
            "0901234578",
            LocalDate.of(1998, 3, 15),
            Gender.FEMALE,
            "963 Đường EFG, Quận 12, TP.HCM",
            "001234567901",
            Role.CUSTOMER,
            UserStatus.ACTIVE
        );
        
        createUserIfNotExists(
            "user5@test.com",
            "Hoàng Văn Em",
            "0901234579",
            LocalDate.of(1999, 8, 25),
            Gender.MALE,
            "159 Đường HIJ, Quận Bình Thạnh, TP.HCM",
            "001234567902",
            Role.CUSTOMER,
            UserStatus.ACTIVE
        );
        
        System.out.println("DataInitializer: Đã kiểm tra và tạo các tài khoản mặc định (nếu chưa tồn tại)");
    }
    
    private void createUserIfNotExists(
            String email,
            String fullName,
            String phoneNumber,
            LocalDate dayOfBirth,
            Gender gender,
            String address,
            String citizenId,
            Role role,
            UserStatus status
    ) {
        // Kiểm tra xem user đã tồn tại chưa
        if (userRepository.existsByEmail(email.toLowerCase())) {
            System.out.println("DataInitializer: User " + email + " đã tồn tại, bỏ qua");
            return;
        }
        
        // Tạo user mới
        User user = new User();
        user.setEmail(email.toLowerCase());
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD)); // Hash password bằng BCrypt
        user.setProviderUserId(null);
        user.setAuthProvider(AuthProvider.EMAIL);
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        user.setPhoneVerified(true);
        user.setDayOfBirth(dayOfBirth);
        user.setGender(gender);
        user.setAddress(address);
        user.setCitizenId(citizenId);
        user.setRole(role);
        user.setStatus(status);
        user.setCreateAt(LocalDate.now());
        
        userRepository.save(user);
        System.out.println("DataInitializer: Đã tạo user " + email + " với password: " + DEFAULT_PASSWORD);
    }
}



