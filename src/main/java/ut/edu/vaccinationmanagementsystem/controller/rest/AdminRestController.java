package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.AdminUserDTO;
import ut.edu.vaccinationmanagementsystem.dto.VaccineDTO;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;
import ut.edu.vaccinationmanagementsystem.service.UserService;
import ut.edu.vaccinationmanagementsystem.service.VaccineService;
import ut.edu.vaccinationmanagementsystem.service.VaccinationRecordService;
import ut.edu.vaccinationmanagementsystem.repository.*;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VaccineRepository vaccineRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private VaccinationCenterRepository vaccinationCenterRepository;
    
    @Autowired
    private CenterVaccineRepository centerVaccineRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VaccineService vaccineService;
    
    @Autowired
    private VaccinationRecordService vaccinationRecordService;
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return customOAuth2User.getUser();
        } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getUser();
        } else {
            String email = authentication.getName();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
    }
    
    /**
     * Kiểm tra quyền ADMIN
     */
    private void checkAdminPermission(User user) {
        if (user == null) {
            throw new RuntimeException("User not authenticated");
        }
        if (user.getRole() != Role.ADMIN) {
            System.out.println("User role: " + user.getRole() + ", Expected: ADMIN");
            throw new RuntimeException("Only ADMIN can access this resource. Current role: " + (user.getRole() != null ? user.getRole().name() : "null"));
        }
    }
    
    /**
     * GET /api/admin/users
     * Lấy danh sách tất cả users (cho admin)
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            List<User> users = userRepository.findAll();
            
            // Filter theo role
            if (role != null && !role.trim().isEmpty()) {
                try {
                    Role roleEnum = Role.valueOf(role.toUpperCase());
                    users = users.stream()
                            .filter(u -> u.getRole() == roleEnum)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid role, ignore filter
                }
            }
            
            // Filter theo status
            if (status != null && !status.trim().isEmpty()) {
                try {
                    UserStatus statusEnum = UserStatus.valueOf(status.toUpperCase());
                    users = users.stream()
                            .filter(u -> u.getStatus() == statusEnum)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore filter
                }
            }
            
            // Filter theo search (tên, email)
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                users = users.stream()
                        .filter(u -> (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchLower)) ||
                                   (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchLower)))
                        .collect(Collectors.toList());
            }
            
            List<Map<String, Object>> result = users.stream().map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("email", user.getEmail());
                map.put("fullName", user.getFullName());
                map.put("phoneNumber", user.getPhoneNumber());
                map.put("role", user.getRole().name());
                map.put("status", user.getStatus().name());
                map.put("createAt", user.getCreateAt());
                map.put("dayOfBirth", user.getDayOfBirth());
                map.put("gender", user.getGender() != null ? user.getGender().name() : null);
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/admin/vaccines
     * Lấy danh sách tất cả vaccines (cho admin)
     */
    @GetMapping("/vaccines")
    public ResponseEntity<?> getAllVaccines(@RequestParam(required = false) String search) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            List<Vaccine> vaccines;
            if (search != null && !search.trim().isEmpty()) {
                vaccines = vaccineRepository.searchByKeyword(search);
            } else {
                vaccines = vaccineRepository.findAll();
            }
            
            // Lấy trung tâm tổng (centerId = 1) - phải là final để dùng trong lambda
            ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter mainCenter = null;
            try {
                ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter tempCenter = vaccinationCenterRepository.findById(1L).orElse(null);
                if (tempCenter == null) {
                    // Nếu không có centerId = 1, lấy trung tâm đầu tiên
                    List<ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter> centers = vaccinationCenterRepository.findAll();
                    if (!centers.isEmpty()) {
                        tempCenter = centers.get(0);
                    }
                }
                mainCenter = tempCenter;
            } catch (Exception e) {
                System.err.println("Warning: Could not find main center: " + e.getMessage());
            }
            
            // Tạo biến final để sử dụng trong lambda
            final ut.edu.vaccinationmanagementsystem.entity.VaccinationCenter finalMainCenter = mainCenter;
            
            List<Map<String, Object>> result = vaccines.stream().map(vaccine -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", vaccine.getId());
                map.put("name", vaccine.getName());
                map.put("code", vaccine.getCode());
                map.put("manufacturer", vaccine.getManufacturer());
                map.put("origin", vaccine.getOrigin());
                map.put("price", vaccine.getPrice());
                map.put("dosesRequired", vaccine.getDosesRequired());
                map.put("status", vaccine.getStatus() != null ? vaccine.getStatus().name() : null);
                map.put("imageUrl", vaccine.getImageUrl());
                map.put("createdAt", vaccine.getCreatedAt());
                
                // Tính số lượng tồn kho từ trung tâm tổng
                Integer stockQuantity = 0;
                if (finalMainCenter != null) {
                    try {
                        Optional<ut.edu.vaccinationmanagementsystem.entity.CenterVaccine> mainCenterVaccine = 
                            centerVaccineRepository.findByCenterAndVaccine(finalMainCenter, vaccine);
                        if (mainCenterVaccine.isPresent()) {
                            stockQuantity = mainCenterVaccine.get().getStockQuantity() != null 
                                ? mainCenterVaccine.get().getStockQuantity() 
                                : 0;
                        }
                    } catch (Exception e) {
                        System.err.println("Warning: Could not get stock for vaccine " + vaccine.getId() + ": " + e.getMessage());
                    }
                }
                map.put("stockQuantity", stockQuantity);
                
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/admin/vaccines
     * Tạo vaccine mới (chỉ admin)
     */
    @PostMapping("/vaccines")
    public ResponseEntity<?> createVaccine(@RequestBody VaccineDTO dto) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            Vaccine createdVaccine = vaccineService.createVaccine(dto);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", createdVaccine.getId());
            result.put("name", createdVaccine.getName());
            result.put("code", createdVaccine.getCode());
            result.put("manufacturer", createdVaccine.getManufacturer());
            result.put("origin", createdVaccine.getOrigin());
            result.put("price", createdVaccine.getPrice());
            result.put("dosesRequired", createdVaccine.getDosesRequired());
            result.put("status", createdVaccine.getStatus() != null ? createdVaccine.getStatus().name() : null);
            result.put("imageUrl", createdVaccine.getImageUrl());
            result.put("createdAt", createdVaccine.getCreatedAt());
            result.put("message", "Vaccine created successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
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
     * PUT /api/admin/vaccines/{id}
     * Cập nhật vaccine (chỉ admin)
     */
    @PutMapping("/vaccines/{id}")
    public ResponseEntity<?> updateVaccine(@PathVariable Long id, @RequestBody VaccineDTO dto) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            // Log for debugging
            System.out.println("Updating vaccine ID: " + id);
            System.out.println("DTO received: " + dto.getName() + ", " + dto.getCode() + ", " + dto.getPrice() + ", " + dto.getStatus());
            
            Vaccine updatedVaccine = vaccineService.updateVaccine(id, dto);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", updatedVaccine.getId());
            result.put("name", updatedVaccine.getName());
            result.put("code", updatedVaccine.getCode());
            result.put("manufacturer", updatedVaccine.getManufacturer());
            result.put("origin", updatedVaccine.getOrigin());
            result.put("description", updatedVaccine.getDescription());
            result.put("price", updatedVaccine.getPrice());
            result.put("minAge", updatedVaccine.getMinAge());
            result.put("maxAge", updatedVaccine.getMaxAge());
            result.put("dosesRequired", updatedVaccine.getDosesRequired());
            result.put("daysBetweenDoses", updatedVaccine.getDaysBetweenDoses());
            result.put("contraindications", updatedVaccine.getContraindications());
            result.put("storageTemperature", updatedVaccine.getStorageTemperature());
            result.put("imageUrl", updatedVaccine.getImageUrl());
            result.put("status", updatedVaccine.getStatus() != null ? updatedVaccine.getStatus().name() : null);
            result.put("message", "Vaccine updated successfully");
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            System.err.println("Error updating vaccine: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("Unexpected error updating vaccine: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * DELETE /api/admin/vaccines/{id}
     * Xóa vaccine (chỉ admin)
     */
    @DeleteMapping("/vaccines/{id}")
    public ResponseEntity<?> deleteVaccine(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            System.out.println("Current user: " + currentUser.getEmail() + ", Role: " + currentUser.getRole());
            checkAdminPermission(currentUser);
            
            vaccineService.deleteVaccine(id);
            
            Map<String, String> result = new HashMap<>();
            result.put("message", "Vaccine deleted successfully");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            System.err.println("Error deleting vaccine: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("Unexpected error deleting vaccine: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/admin/vaccines/{id}
     * Lấy chi tiết vaccine (chỉ admin)
     */
    @GetMapping("/vaccines/{id}")
    public ResponseEntity<?> getVaccineById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            Vaccine vaccine = vaccineService.getVaccineById(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", vaccine.getId());
            result.put("name", vaccine.getName());
            result.put("code", vaccine.getCode());
            result.put("manufacturer", vaccine.getManufacturer());
            result.put("origin", vaccine.getOrigin());
            result.put("description", vaccine.getDescription());
            result.put("price", vaccine.getPrice());
            result.put("minAge", vaccine.getMinAge());
            result.put("maxAge", vaccine.getMaxAge());
            result.put("dosesRequired", vaccine.getDosesRequired());
            result.put("daysBetweenDoses", vaccine.getDaysBetweenDoses());
            result.put("contraindications", vaccine.getContraindications());
            result.put("storageTemperature", vaccine.getStorageTemperature());
            result.put("imageUrl", vaccine.getImageUrl());
            result.put("status", vaccine.getStatus() != null ? vaccine.getStatus().name() : null);
            result.put("createdAt", vaccine.getCreatedAt());
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/admin/appointments
     * Lấy danh sách tất cả appointments (cho admin)
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getAllAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String search) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            List<Appointment> appointments = appointmentRepository.findAll();
            
            // Filter theo status
            if (status != null && !status.trim().isEmpty()) {
                try {
                    AppointmentStatus statusEnum = AppointmentStatus.valueOf(status.toUpperCase());
                    appointments = appointments.stream()
                            .filter(apt -> apt.getStatus() == statusEnum)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore filter
                }
            }
            
            // Filter theo date range
            if (startDate != null && !startDate.trim().isEmpty()) {
                LocalDate start = LocalDate.parse(startDate);
                appointments = appointments.stream()
                        .filter(apt -> apt.getAppointmentDate() != null && 
                                     apt.getAppointmentDate().isAfter(start.minusDays(1)))
                        .collect(Collectors.toList());
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                LocalDate end = LocalDate.parse(endDate);
                appointments = appointments.stream()
                        .filter(apt -> apt.getAppointmentDate() != null && 
                                     apt.getAppointmentDate().isBefore(end.plusDays(1)))
                        .collect(Collectors.toList());
            }
            
            // Filter theo search (booking code, tên bệnh nhân)
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                appointments = appointments.stream()
                        .filter(apt -> (apt.getBookingCode() != null && apt.getBookingCode().toLowerCase().contains(searchLower)) ||
                                     (apt.getBookedForUser() != null && apt.getBookedForUser().getFullName() != null && 
                                      apt.getBookedForUser().getFullName().toLowerCase().contains(searchLower)) ||
                                     (apt.getBookedByUser() != null && apt.getBookedByUser().getFullName() != null && 
                                      apt.getBookedByUser().getFullName().toLowerCase().contains(searchLower)) ||
                                     (apt.getFamilyMember() != null && apt.getFamilyMember().getFullName() != null && 
                                      apt.getFamilyMember().getFullName().toLowerCase().contains(searchLower)))
                        .collect(Collectors.toList());
            }
            
            List<Map<String, Object>> result = appointments.stream().map(apt -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", apt.getId());
                map.put("bookingCode", apt.getBookingCode());
                map.put("appointmentDate", apt.getAppointmentDate());
                map.put("appointmentTime", apt.getAppointmentTime());
                map.put("status", apt.getStatus().name());
                map.put("doseNumber", apt.getDoseNumber());
                
                // Thông tin bệnh nhân
                Map<String, Object> patientInfo = new HashMap<>();
                if (apt.getBookedForUser() != null) {
                    patientInfo.put("fullName", apt.getBookedForUser().getFullName());
                    patientInfo.put("email", apt.getBookedForUser().getEmail());
                    patientInfo.put("phoneNumber", apt.getBookedForUser().getPhoneNumber());
                } else if (apt.getFamilyMember() != null) {
                    patientInfo.put("fullName", apt.getFamilyMember().getFullName());
                    patientInfo.put("phoneNumber", apt.getFamilyMember().getPhoneNumber());
                } else if (apt.getBookedByUser() != null) {
                    patientInfo.put("fullName", apt.getBookedByUser().getFullName());
                    patientInfo.put("email", apt.getBookedByUser().getEmail());
                    patientInfo.put("phoneNumber", apt.getBookedByUser().getPhoneNumber());
                }
                map.put("patientInfo", patientInfo);
                
                // Thông tin vaccine
                if (apt.getVaccine() != null) {
                    Map<String, Object> vaccineInfo = new HashMap<>();
                    vaccineInfo.put("id", apt.getVaccine().getId());
                    vaccineInfo.put("name", apt.getVaccine().getName());
                    map.put("vaccineInfo", vaccineInfo);
                }
                
                // Thông tin trung tâm
                if (apt.getCenter() != null) {
                    map.put("centerName", apt.getCenter().getName());
                }
                
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/admin/staff
     * Lấy danh sách staff (DOCTOR, NURSE, RECEPTIONIST)
     */
    @GetMapping("/staff")
    public ResponseEntity<?> getAllStaff(@RequestParam(required = false) String role) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            List<User> staff = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.DOCTOR || 
                               u.getRole() == Role.NURSE || 
                               u.getRole() == Role.RECEPTIONIST)
                    .collect(Collectors.toList());
            
            // Filter theo role nếu có
            if (role != null && !role.trim().isEmpty()) {
                try {
                    Role roleEnum = Role.valueOf(role.toUpperCase());
                    staff = staff.stream()
                            .filter(u -> u.getRole() == roleEnum)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid role, ignore filter
                }
            }
            
            List<Map<String, Object>> result = staff.stream().map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("email", user.getEmail());
                map.put("fullName", user.getFullName());
                map.put("phoneNumber", user.getPhoneNumber());
                map.put("role", user.getRole().name());
                map.put("status", user.getStatus().name());
                map.put("createAt", user.getCreateAt());
                map.put("dayOfBirth", user.getDayOfBirth());
                map.put("gender", user.getGender() != null ? user.getGender().name() : null);
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/admin/dashboard/stats
     * Lấy thống kê dashboard cho admin (tổng quan)
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            // Sử dụng DashboardController logic hoặc tạo mới
            // Tạm thời tạo stats đơn giản
            long totalUsers = userRepository.findAll().size();
            long totalAppointments = appointmentRepository.findAll().size();
            long totalVaccines = vaccineRepository.findAll().size();
            long totalCenters = vaccinationCenterRepository.findAll().size();
            
            // Đếm users theo role
            long totalCustomers = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.CUSTOMER)
                    .count();
            long totalDoctors = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.DOCTOR)
                    .count();
            
            // Đếm appointments hôm nay
            LocalDate today = LocalDate.now();
            long todayAppointments = appointmentRepository.findAll().stream()
                    .filter(apt -> apt.getAppointmentDate() != null && apt.getAppointmentDate().equals(today))
                    .count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalCustomers", totalCustomers);
            stats.put("totalDoctors", totalDoctors);
            stats.put("totalAppointments", totalAppointments);
            stats.put("todayAppointments", todayAppointments);
            stats.put("totalVaccines", totalVaccines);
            stats.put("totalCenters", totalCenters);
            
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/admin/users/{id}
     * Lấy chi tiết user theo ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            User user = userService.getUserById(id);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("email", user.getEmail());
            result.put("fullName", user.getFullName());
            result.put("phoneNumber", user.getPhoneNumber());
            result.put("dayOfBirth", user.getDayOfBirth());
            result.put("gender", user.getGender() != null ? user.getGender().name() : null);
            result.put("address", user.getAddress());
            result.put("citizenId", user.getCitizenId());
            result.put("role", user.getRole().name());
            result.put("status", user.getStatus().name());
            result.put("createAt", user.getCreateAt());
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/admin/users
     * Tạo user mới
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AdminUserDTO dto) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            User user = userService.createUserByAdmin(dto);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("email", user.getEmail());
            result.put("fullName", user.getFullName());
            result.put("phoneNumber", user.getPhoneNumber());
            result.put("role", user.getRole().name());
            result.put("status", user.getStatus().name());
            result.put("message", "User created successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
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
     * PUT /api/admin/users/{id}
     * Cập nhật user
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody AdminUserDTO dto) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            User user = userService.updateUserByAdmin(id, dto);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("email", user.getEmail());
            result.put("fullName", user.getFullName());
            result.put("phoneNumber", user.getPhoneNumber());
            result.put("role", user.getRole().name());
            result.put("status", user.getStatus().name());
            result.put("message", "User updated successfully");
            
            return ResponseEntity.ok(result);
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
     * DELETE /api/admin/users/{id}
     * Xóa user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            // Không cho phép xóa chính mình
            if (currentUser.getId().equals(id)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Cannot delete your own account");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            userService.deleteUserByAdmin(id);
            
            Map<String, String> result = new HashMap<>();
            result.put("message", "User deleted successfully");
            
            return ResponseEntity.ok(result);
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
     * PUT /api/admin/appointments/{id}/status
     * Cập nhật trạng thái appointment (cho admin)
     * 
     * CÁC TRẠNG THÁI VÀ QUYỀN THAY ĐỔI:
     * - PENDING (Chờ xác nhận): ADMIN, RECEPTIONIST có thể chuyển sang CONFIRMED
     * - CONFIRMED (Đã xác nhận): ADMIN, RECEPTIONIST có thể chuyển sang CHECKED_IN
     * - CHECKED_IN (Đã check-in): ADMIN, RECEPTIONIST có thể chuyển sang SCREENING
     * - SCREENING (Đang khám sàng lọc): ADMIN, DOCTOR có thể chuyển sang APPROVED/REJECTED
     * - APPROVED (Đủ điều kiện): ADMIN, DOCTOR có thể chuyển sang INJECTING
     * - REJECTED (Từ chối): ADMIN, DOCTOR có thể chuyển sang CANCELLED
     * - INJECTING (Đang tiêm): ADMIN, NURSE có thể chuyển sang MONITORING
     * - MONITORING (Đang theo dõi): ADMIN, NURSE có thể chuyển sang COMPLETED
     * - COMPLETED (Hoàn thành): ADMIN có thể xem, không nên thay đổi
     * - CANCELLED (Đã hủy): ADMIN có thể xem, không nên thay đổi
     * - RESCHEDULED (Đã đổi lịch): ADMIN có thể xem, không nên thay đổi
     * 
     * LƯU Ý: ADMIN có quyền thay đổi bất kỳ trạng thái nào, nhưng nên tuân theo workflow
     * Khi chuyển sang COMPLETED, hệ thống sẽ kiểm tra xem đã có VaccinationRecord chưa
     */
    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            String statusStr = request.get("status");
            if (statusStr == null || statusStr.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Status is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            AppointmentStatus newStatus;
            try {
                newStatus = AppointmentStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + statusStr);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Lưu trạng thái cũ
            AppointmentStatus oldStatus = appointment.getStatus();
            
            // Kiểm tra nếu đang chuyển sang COMPLETED mà chưa có VaccinationRecord
            if (newStatus == AppointmentStatus.COMPLETED && oldStatus != AppointmentStatus.COMPLETED) {
                // Kiểm tra xem đã có VaccinationRecord chưa
                boolean hasRecord = vaccinationRecordRepository.existsByAppointment(appointment);
                if (!hasRecord) {
                    // Cảnh báo nhưng vẫn cho phép ADMIN thay đổi
                    // Vì ADMIN có thể muốn đánh dấu COMPLETED trước khi NURSE tạo record
                }
            }
            
            // Cập nhật trạng thái
            appointment.setStatus(newStatus);
            appointment.setUpdatedAt(java.time.LocalDateTime.now());
            appointmentRepository.save(appointment);
            
            // Nếu chuyển sang CANCELLED, rollback slot booking count
            if (newStatus == AppointmentStatus.CANCELLED && oldStatus != AppointmentStatus.CANCELLED) {
                AppointmentSlot slot = appointment.getSlot();
                if (slot != null) {
                    int currentBookings = slot.getCurrentBookings();
                    if (currentBookings > 0) {
                        slot.setCurrentBookings(currentBookings - 1);
                    }
                    if (!slot.getIsAvailable() && slot.getCurrentBookings() < slot.getMaxCapacity()) {
                        slot.setIsAvailable(true);
                    }
                    appointmentSlotRepository.save(slot);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment status updated successfully");
            response.put("appointmentId", appointment.getId());
            response.put("oldStatus", oldStatus.name());
            response.put("newStatus", newStatus.name());
            
            // Thêm thông tin về VaccinationRecord nếu chuyển sang COMPLETED
            if (newStatus == AppointmentStatus.COMPLETED) {
                boolean hasRecord = vaccinationRecordRepository.existsByAppointment(appointment);
                response.put("hasVaccinationRecord", hasRecord);
                if (!hasRecord) {
                    response.put("warning", "Chưa có VaccinationRecord. Vui lòng tạo qua API của NURSE hoặc tạo thủ công.");
                }
            }
            
            return ResponseEntity.ok(response);
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
     * DELETE /api/admin/appointments/{id}
     * Xóa appointment (cho admin)
     */
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Không cho phép xóa nếu đã hoàn thành hoặc đang trong quá trình tiêm
            if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
                appointment.getStatus() == AppointmentStatus.INJECTING ||
                appointment.getStatus() == AppointmentStatus.MONITORING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không thể xóa lịch hẹn đã hoàn thành hoặc đang trong quá trình tiêm");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Rollback slot booking count
            AppointmentSlot slot = appointment.getSlot();
            if (slot != null) {
                int currentBookings = slot.getCurrentBookings();
                if (currentBookings > 0) {
                    slot.setCurrentBookings(currentBookings - 1);
                }
                if (!slot.getIsAvailable() && slot.getCurrentBookings() < slot.getMaxCapacity()) {
                    slot.setIsAvailable(true);
                }
                appointmentSlotRepository.save(slot);
            }
            
            // Xóa payment nếu có
            if (appointment.getPayment() != null) {
                paymentRepository.delete(appointment.getPayment());
            }
            
            // Xóa appointment
            appointmentRepository.delete(appointment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Appointment deleted successfully");
            response.put("appointmentId", id);
            
            return ResponseEntity.ok(response);
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

