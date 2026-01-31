package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.entity.enums.ScreeningResult;
import ut.edu.vaccinationmanagementsystem.repository.*;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
public class DoctorRestController {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private ScreeningRepository screeningRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private VaccineRepository vaccineRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.StaffInfoRepository staffInfoRepository;
    
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
            return userService.getUserByEmail(email);
        }
    }
    
    /**
     * Kiểm tra quyền DOCTOR
     */
    private void checkDoctorPermission(User user) {
        if (user == null || user.getRole() != Role.DOCTOR) {
            throw new RuntimeException("Only DOCTOR can access this resource");
        }
    }
    
    /**
     * GET /api/doctor/appointments/pending
     * Lấy danh sách appointments cần khám sàng lọc
     */
    @GetMapping("/appointments/pending")
    public ResponseEntity<?> getPendingAppointments() {
        try {
            User currentUser = getCurrentUser();
            checkDoctorPermission(currentUser);
            
            // Lấy appointments có status CONFIRMED (đã xác nhận), CHECKED_IN (đã check-in), hoặc SCREENING (đang khám)
            // Không yêu cầu payment status = PAID vì chức năng QR thanh toán chưa hoạt động
            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED || 
                                  apt.getStatus() == AppointmentStatus.CHECKED_IN ||
                                  apt.getStatus() == AppointmentStatus.SCREENING)
                    .filter(apt -> apt.getAppointmentDate() != null && 
                                 (apt.getAppointmentDate().equals(LocalDate.now()) || 
                                  apt.getAppointmentDate().isAfter(LocalDate.now())))
                    .collect(Collectors.toList());
            
            // Bác sĩ có thể thấy tất cả lịch hẹn từ mọi trung tâm
            
            List<Map<String, Object>> result = appointments.stream().map(apt -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", apt.getId());
                map.put("bookingCode", apt.getBookingCode());
                map.put("appointmentDate", apt.getAppointmentDate());
                map.put("appointmentTime", apt.getAppointmentTime());
                map.put("status", apt.getStatus().name());
                
                // Thông tin bệnh nhân - ưu tiên: familyMember > bookedForUser > bookedByUser > guest
                Map<String, Object> patientInfo = new HashMap<>();
                if (apt.getFamilyMember() != null) {
                    // Ưu tiên familyMember (người thân sẽ được tiêm)
                    patientInfo.put("fullName", apt.getFamilyMember().getFullName());
                    patientInfo.put("phoneNumber", apt.getFamilyMember().getPhoneNumber());
                    patientInfo.put("dateOfBirth", apt.getFamilyMember().getDateOfBirth());
                    patientInfo.put("gender", apt.getFamilyMember().getGender() != null ? apt.getFamilyMember().getGender().name() : null);
                } else if (apt.getBookedForUser() != null) {
                    patientInfo.put("fullName", apt.getBookedForUser().getFullName());
                    patientInfo.put("email", apt.getBookedForUser().getEmail());
                    patientInfo.put("phoneNumber", apt.getBookedForUser().getPhoneNumber());
                    patientInfo.put("dateOfBirth", apt.getBookedForUser().getDayOfBirth());
                    patientInfo.put("gender", apt.getBookedForUser().getGender() != null ? apt.getBookedForUser().getGender().name() : null);
                } else if (apt.getBookedByUser() != null) {
                    // Fallback: nếu bookedForUser null, dùng bookedByUser (người đặt lịch cho chính mình)
                    patientInfo.put("fullName", apt.getBookedByUser().getFullName());
                    patientInfo.put("email", apt.getBookedByUser().getEmail());
                    patientInfo.put("phoneNumber", apt.getBookedByUser().getPhoneNumber());
                    patientInfo.put("dateOfBirth", apt.getBookedByUser().getDayOfBirth());
                    patientInfo.put("gender", apt.getBookedByUser().getGender() != null ? apt.getBookedByUser().getGender().name() : null);
                } else {
                    // Guest info (walk-in appointments)
                    patientInfo.put("fullName", apt.getGuestFullName() != null ? apt.getGuestFullName() : "Khách vãng lai");
                    patientInfo.put("phoneNumber", apt.getConsultationPhone() != null ? apt.getConsultationPhone() : "N/A");
                    patientInfo.put("dateOfBirth", apt.getGuestDayOfBirth());
                    patientInfo.put("gender", apt.getGuestGender() != null ? apt.getGuestGender().name() : null);
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
     * GET /api/doctor/appointments/{appointmentId}
     * Lấy chi tiết appointment để khám sàng lọc
     */
    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<?> getAppointmentDetail(@PathVariable Long appointmentId) {
        try {
            User currentUser = getCurrentUser();
            checkDoctorPermission(currentUser);
            
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Appointment apt = appointmentOpt.get();
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", apt.getId());
            result.put("bookingCode", apt.getBookingCode());
            result.put("appointmentDate", apt.getAppointmentDate());
            result.put("appointmentTime", apt.getAppointmentTime());
            result.put("status", apt.getStatus().name());
            result.put("doseNumber", apt.getDoseNumber());
            
            // Thông tin bệnh nhân
            Map<String, Object> patientInfo = new HashMap<>();
            if (apt.getBookedForUser() != null) {
                patientInfo.put("userId", apt.getBookedForUser().getId()); // Thêm userId
                patientInfo.put("fullName", apt.getBookedForUser().getFullName());
                patientInfo.put("email", apt.getBookedForUser().getEmail());
                patientInfo.put("phoneNumber", apt.getBookedForUser().getPhoneNumber());
                patientInfo.put("dateOfBirth", apt.getBookedForUser().getDayOfBirth());
                patientInfo.put("gender", apt.getBookedForUser().getGender() != null ? apt.getBookedForUser().getGender().name() : null);
                patientInfo.put("citizenId", apt.getBookedForUser().getCitizenId());
            } else if (apt.getFamilyMember() != null) {
                // Với family member, userId là của user chủ (bookedByUser)
                patientInfo.put("userId", apt.getBookedByUser() != null ? apt.getBookedByUser().getId() : null);
                patientInfo.put("familyMemberId", apt.getFamilyMember().getId());
                patientInfo.put("fullName", apt.getFamilyMember().getFullName());
                patientInfo.put("phoneNumber", apt.getFamilyMember().getPhoneNumber());
                patientInfo.put("dateOfBirth", apt.getFamilyMember().getDateOfBirth());
                patientInfo.put("gender", apt.getFamilyMember().getGender() != null ? apt.getFamilyMember().getGender().name() : null);
                patientInfo.put("citizenId", apt.getFamilyMember().getCitizenId());
            } else {
                patientInfo.put("userId", apt.getBookedByUser().getId()); // Thêm userId
                patientInfo.put("fullName", apt.getBookedByUser().getFullName());
                patientInfo.put("email", apt.getBookedByUser().getEmail());
                patientInfo.put("phoneNumber", apt.getBookedByUser().getPhoneNumber());
                patientInfo.put("dateOfBirth", apt.getBookedByUser().getDayOfBirth());
                patientInfo.put("gender", apt.getBookedByUser().getGender() != null ? apt.getBookedByUser().getGender().name() : null);
                patientInfo.put("citizenId", apt.getBookedByUser().getCitizenId());
            }
            result.put("patientInfo", patientInfo);
            
            // Thông tin vaccine
            if (apt.getVaccine() != null) {
                Map<String, Object> vaccineInfo = new HashMap<>();
                vaccineInfo.put("id", apt.getVaccine().getId());
                vaccineInfo.put("name", apt.getVaccine().getName());
                result.put("vaccineInfo", vaccineInfo);
            }
            
            // Kiểm tra đã có screening chưa
            Optional<Screening> screeningOpt = screeningRepository.findByAppointmentId(appointmentId);
            if (screeningOpt.isPresent()) {
                Screening screening = screeningOpt.get();
                Map<String, Object> screeningInfo = new HashMap<>();
                screeningInfo.put("id", screening.getId());
                screeningInfo.put("bodyTemperature", screening.getBodyTemperature());
                screeningInfo.put("bloodPressure", screening.getBloodPressure());
                screeningInfo.put("heartRate", screening.getHeartRate());
                screeningInfo.put("screeningResult", screening.getScreeningResult().name());
                screeningInfo.put("rejectionReason", screening.getRejectionReason());
                screeningInfo.put("notes", screening.getNotes());
                screeningInfo.put("screenedAt", screening.getScreenedAt());
                result.put("screening", screeningInfo);
            }
            
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
     * POST /api/doctor/screening
     * Tạo hoặc cập nhật kết quả khám sàng lọc
     */
    @PostMapping("/screening")
    public ResponseEntity<?> createOrUpdateScreening(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = getCurrentUser();
            checkDoctorPermission(currentUser);
            
            Long appointmentId = Long.parseLong(request.get("appointmentId").toString());
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Appointment appointment = appointmentOpt.get();
            
            // Bác sĩ có thể khám sàng lọc cho tất cả bệnh nhân
            
            // Kiểm tra đã có screening chưa
            Optional<Screening> existingScreeningOpt = screeningRepository.findByAppointmentId(appointmentId);
            Screening screening;
            
            if (existingScreeningOpt.isPresent()) {
                screening = existingScreeningOpt.get();
            } else {
                screening = new Screening();
                screening.setAppointment(appointment);
                screening.setDoctor(currentUser);
            }
            
            // Cập nhật thông tin
            if (request.containsKey("bodyTemperature") && request.get("bodyTemperature") != null) {
                try {
                    screening.setBodyTemperature(Double.parseDouble(request.get("bodyTemperature").toString()));
                } catch (NumberFormatException e) {
                    // Ignore invalid number format
                }
            }
            if (request.containsKey("bloodPressure") && request.get("bloodPressure") != null) {
                String bp = request.get("bloodPressure").toString().trim();
                if (!bp.isEmpty()) {
                    screening.setBloodPressure(bp);
                }
            }
            if (request.containsKey("heartRate") && request.get("heartRate") != null) {
                try {
                    screening.setHeartRate(Integer.parseInt(request.get("heartRate").toString()));
                } catch (NumberFormatException e) {
                    // Ignore invalid number format
                }
            }
            if (request.containsKey("screeningResult") && request.get("screeningResult") != null) {
                String resultStr = request.get("screeningResult").toString().toUpperCase();
                screening.setScreeningResult(ScreeningResult.valueOf(resultStr));
            } else {
                // Default to APPROVED if not specified
                screening.setScreeningResult(ScreeningResult.APPROVED);
            }
            if (request.containsKey("rejectionReason") && request.get("rejectionReason") != null) {
                String reason = request.get("rejectionReason").toString().trim();
                screening.setRejectionReason(reason.isEmpty() ? null : reason);
            }
            if (request.containsKey("notes") && request.get("notes") != null) {
                String notes = request.get("notes").toString().trim();
                screening.setNotes(notes.isEmpty() ? null : notes);
            }
            screening.setScreenedAt(LocalDateTime.now());
            
            // Validate required fields
            if (screening.getScreeningResult() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Screening result is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Save screening
            try {
                screening = screeningRepository.save(screening);
                System.out.println("✅ Screening saved successfully. ID: " + screening.getId());
            } catch (Exception e) {
                System.err.println("❌ Error saving screening: " + e.getMessage());
                e.printStackTrace();
                Map<String, String> error = new HashMap<>();
                error.put("error", "Failed to save screening: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
            
            // Cập nhật vaccine nếu có
            if (request.containsKey("vaccineId") && request.get("vaccineId") != null) {
                try {
                    Long vaccineId = Long.parseLong(request.get("vaccineId").toString());
                    Optional<Vaccine> vaccineOpt = vaccineRepository.findById(vaccineId);
                    if (vaccineOpt.isPresent()) {
                        appointment.setVaccine(vaccineOpt.get());
                        System.out.println("✅ Vaccine updated to: " + vaccineOpt.get().getName());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error updating vaccine: " + e.getMessage());
                    // Không throw error, chỉ log
                }
            }
            
            // Cập nhật appointment status
            try {
                if (screening.getScreeningResult() == ScreeningResult.APPROVED) {
                    appointment.setStatus(AppointmentStatus.APPROVED);
                } else if (screening.getScreeningResult() == ScreeningResult.REJECTED) {
                    appointment.setStatus(AppointmentStatus.REJECTED);
                }
                appointment.setUpdatedAt(LocalDateTime.now());
                appointmentRepository.save(appointment);
                System.out.println("✅ Appointment status updated to: " + appointment.getStatus());
            } catch (Exception e) {
                System.err.println("❌ Error updating appointment status: " + e.getMessage());
                e.printStackTrace();
                // Không throw error vì screening đã được lưu
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", screening.getId());
            result.put("message", "Screening result saved successfully");
            result.put("appointmentStatus", appointment.getStatus().name());
            result.put("screeningResult", screening.getScreeningResult().name());
            
            System.out.println("✅ Screening API response: " + result);
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
     * GET /api/doctor/dashboard/stats
     * Lấy thống kê dashboard cho bác sĩ
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            User currentUser = getCurrentUser();
            checkDoctorPermission(currentUser);
            
            // Lấy tất cả appointments của bác sĩ (đã khám)
            List<Screening> screenings = screeningRepository.findByDoctorIdOrderByScreenedAtDesc(currentUser.getId());
            
            // Đếm các trạng thái
            long totalAppointments = screenings.size();
            long approvedCount = screenings.stream()
                    .filter(s -> s.getScreeningResult() == ScreeningResult.APPROVED)
                    .count();
            long rejectedCount = screenings.stream()
                    .filter(s -> s.getScreeningResult() == ScreeningResult.REJECTED)
                    .count();
            
            // Lấy appointments pending (chưa khám) - không yêu cầu payment status
            List<Appointment> pendingAppointments = appointmentRepository.findAll().stream()
                    .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED || 
                                  apt.getStatus() == AppointmentStatus.CHECKED_IN ||
                                  apt.getStatus() == AppointmentStatus.SCREENING)
                    .filter(apt -> apt.getAppointmentDate() != null && 
                                 (apt.getAppointmentDate().equals(LocalDate.now()) || 
                                  apt.getAppointmentDate().isAfter(LocalDate.now())))
                    .collect(Collectors.toList());
            
            // Bác sĩ có thể thấy tất cả lịch hẹn từ mọi trung tâm
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAppointments", pendingAppointments.size());
            stats.put("completedAppointments", totalAppointments);
            stats.put("approvedAppointments", approvedCount);
            stats.put("rejectedAppointments", rejectedCount);
            
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
     * GET /api/doctor/dashboard/weekly-stats
     * Lấy thống kê theo tuần (7 ngày gần nhất) cho dashboard
     */
    @GetMapping("/dashboard/weekly-stats")
    public ResponseEntity<?> getWeeklyStats() {
        try {
            User currentUser = getCurrentUser();
            checkDoctorPermission(currentUser);
            
            // Thống kê cho tất cả trung tâm
            
            // Tính toán 7 ngày gần nhất (từ 6 ngày trước đến hôm nay)
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(6);
            
            // Lấy tất cả screenings trong 7 ngày qua
            List<Screening> screenings = screeningRepository.findByDoctorIdOrderByScreenedAtDesc(currentUser.getId())
                    .stream()
                    .filter(s -> {
                        LocalDate screenedDate = s.getScreenedAt().toLocalDate();
                        return !screenedDate.isBefore(startDate) && !screenedDate.isAfter(today);
                    })
                    .collect(Collectors.toList());
            
            // Đếm theo từng ngày trong tuần
            Map<String, Integer> dailyCounts = new HashMap<>();
            String[] dayLabels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            
            // Khởi tạo tất cả các ngày với 0
            for (int i = 0; i < 7; i++) {
                LocalDate date = startDate.plusDays(i);
                int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
                // Chuyển sang index 0-6 (T2=0, T3=1, ..., CN=6)
                int index = dayOfWeek == 7 ? 6 : dayOfWeek - 1;
                dailyCounts.put(dayLabels[index], 0);
            }
            
            // Đếm số lượng screenings theo ngày
            for (Screening screening : screenings) {
                LocalDate screenedDate = screening.getScreenedAt().toLocalDate();
                int dayOfWeek = screenedDate.getDayOfWeek().getValue();
                int index = dayOfWeek == 7 ? 6 : dayOfWeek - 1;
                String dayLabel = dayLabels[index];
                dailyCounts.put(dayLabel, dailyCounts.get(dayLabel) + 1);
            }
            
            // Tìm ngày có số lượng cao nhất
            String highestDay = dayLabels[0];
            int highestCount = dailyCounts.get(highestDay);
            for (String day : dayLabels) {
                if (dailyCounts.get(day) > highestCount) {
                    highestCount = dailyCounts.get(day);
                    highestDay = day;
                }
            }
            
            // Tính trung bình
            int totalCount = dailyCounts.values().stream().mapToInt(Integer::intValue).sum();
            int averageCount = totalCount / 7;
            
            // Tạo response
            Map<String, Object> result = new HashMap<>();
            result.put("dailyCounts", dailyCounts);
            result.put("highestDay", highestDay);
            result.put("highestCount", highestCount);
            result.put("averageCount", averageCount);
            result.put("totalCount", totalCount);
            
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
     * GET /api/doctor/screening/history
     * Lấy lịch sử khám sàng lọc của bác sĩ
     */
    @GetMapping("/screening/history")
    public ResponseEntity<?> getScreeningHistory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            User currentUser = getCurrentUser();
            checkDoctorPermission(currentUser);
            
            List<Screening> screenings = screeningRepository.findByDoctorIdOrderByScreenedAtDesc(currentUser.getId());
            
            // Filter theo ngày nếu có
            if (startDate != null || endDate != null) {
                LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
                LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
                
                screenings = screenings.stream()
                        .filter(s -> {
                            LocalDate screenedDate = s.getScreenedAt().toLocalDate();
                            if (start != null && screenedDate.isBefore(start)) return false;
                            if (end != null && screenedDate.isAfter(end)) return false;
                            return true;
                        })
                        .collect(Collectors.toList());
            }
            
            List<Map<String, Object>> result = screenings.stream().map(screening -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", screening.getId());
                map.put("screenedAt", screening.getScreenedAt());
                map.put("bodyTemperature", screening.getBodyTemperature());
                map.put("bloodPressure", screening.getBloodPressure());
                map.put("heartRate", screening.getHeartRate());
                map.put("screeningResult", screening.getScreeningResult().name());
                map.put("rejectionReason", screening.getRejectionReason());
                map.put("notes", screening.getNotes());
                
                // Thông tin appointment
                Appointment apt = screening.getAppointment();
                if (apt != null) {
                    Map<String, Object> appointmentInfo = new HashMap<>();
                    appointmentInfo.put("id", apt.getId());
                    appointmentInfo.put("bookingCode", apt.getBookingCode());
                    appointmentInfo.put("appointmentDate", apt.getAppointmentDate());
                    appointmentInfo.put("appointmentTime", apt.getAppointmentTime());
                    appointmentInfo.put("status", apt.getStatus().name()); // Thêm trạng thái appointment
                    appointmentInfo.put("hasVaccinationRecord", apt.getVaccinationRecord() != null); // Flag để biết đã tiêm chưa
                    
                    // Thông tin bệnh nhân
                    Map<String, Object> patientInfo = new HashMap<>();
                    if (apt.getBookedForUser() != null) {
                        patientInfo.put("fullName", apt.getBookedForUser().getFullName());
                    } else if (apt.getFamilyMember() != null) {
                        patientInfo.put("fullName", apt.getFamilyMember().getFullName());
                    } else {
                        patientInfo.put("fullName", apt.getBookedByUser().getFullName());
                    }
                    appointmentInfo.put("patientInfo", patientInfo);
                    
                    // Thông tin vaccine
                    if (apt.getVaccine() != null) {
                        appointmentInfo.put("vaccineName", apt.getVaccine().getName());
                    }
                    
                    map.put("appointment", appointmentInfo);
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
     * GET /api/doctor/profile/stats
     * Lấy thống kê cho trang profile của bác sĩ
     */
    @GetMapping("/profile/stats")
    public ResponseEntity<?> getProfileStats() {
        try {
            User currentUser = getCurrentUser();
            checkDoctorPermission(currentUser);
            
            // Lấy tất cả screenings của bác sĩ
            List<Screening> screenings = screeningRepository.findByDoctorIdOrderByScreenedAtDesc(currentUser.getId());
            
            long totalScreenings = screenings.size();
            long approvedCount = screenings.stream()
                    .filter(s -> s.getScreeningResult() == ScreeningResult.APPROVED)
                    .count();
            long rejectedCount = screenings.stream()
                    .filter(s -> s.getScreeningResult() == ScreeningResult.REJECTED)
                    .count();
            
            // Tính tỷ lệ phê duyệt
            double approvalRate = totalScreenings > 0 ? 
                (double) approvedCount / totalScreenings * 100 : 0.0;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalScreenings", totalScreenings);
            stats.put("approvedCount", approvedCount);
            stats.put("rejectedCount", rejectedCount);
            stats.put("approvalRate", Math.round(approvalRate * 10.0) / 10.0); // Round to 1 decimal
            
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
     * GET /api/doctor/patient/history
     * Lấy lịch sử tiêm chủng của bệnh nhân (theo citizenId hoặc userId)
     */
    @GetMapping("/patient/history")
    public ResponseEntity<?> getPatientVaccinationHistory(
            @RequestParam(required = false) String citizenId,
            @RequestParam(required = false) Long userId) {
        try {
            User currentUser = getCurrentUser();
            checkDoctorPermission(currentUser);
            
            User patient = null;
            
            // Tìm patient theo citizenId hoặc userId
            if (citizenId != null && !citizenId.trim().isEmpty()) {
                patient = userRepository.findByCitizenId(citizenId.trim()).orElse(null);
            } else if (userId != null) {
                patient = userRepository.findById(userId).orElse(null);
            }
            
            if (patient == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Lấy lịch sử tiêm chủng
            List<VaccinationRecord> records = vaccinationRecordRepository.findByUserIdOrderByInjectionDateDesc(patient.getId());
            
            // Convert to DTO
            List<Map<String, Object>> recordsList = records.stream().map(record -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", record.getId());
                map.put("injectionDate", record.getInjectionDate());
                map.put("injectionTime", record.getInjectionTime());
                map.put("doseNumber", record.getDoseNumber());
                map.put("vaccineName", record.getVaccine() != null ? record.getVaccine().getName() : null);
                map.put("vaccineId", record.getVaccine() != null ? record.getVaccine().getId() : null);
                map.put("centerName", record.getAppointment() != null && record.getAppointment().getCenter() != null 
                        ? record.getAppointment().getCenter().getName() : null);
                map.put("batchNumber", record.getBatchNumber());
                map.put("certificateNumber", record.getCertificateNumber());
                map.put("injectionSite", record.getInjectionSite());
                return map;
            }).collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("patientId", patient.getId());
            result.put("patientName", patient.getFullName());
            result.put("citizenId", patient.getCitizenId());
            result.put("records", recordsList);
            result.put("totalRecords", recordsList.size());
            
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
     * GET /api/doctor/screening/history/{screeningId}
     * Lấy chi tiết lịch sử khám sàng lọc với đầy đủ thông tin
     */
    @GetMapping("/screening/history/{screeningId}")
    public ResponseEntity<?> getScreeningHistoryDetail(@PathVariable Long screeningId) {
        try {
            User currentUser = getCurrentUser();
            checkDoctorPermission(currentUser);
            
            Optional<Screening> screeningOpt = screeningRepository.findById(screeningId);
            if (screeningOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Screening not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Screening screening = screeningOpt.get();
            
            // Kiểm tra quyền: chỉ bác sĩ đã khám mới xem được
            if (!screening.getDoctor().getId().equals(currentUser.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Appointment appointment = screening.getAppointment();
            
            Map<String, Object> result = new HashMap<>();
            
            // Thông tin screening
            Map<String, Object> screeningInfo = new HashMap<>();
            screeningInfo.put("id", screening.getId());
            screeningInfo.put("bodyTemperature", screening.getBodyTemperature());
            screeningInfo.put("bloodPressure", screening.getBloodPressure());
            screeningInfo.put("heartRate", screening.getHeartRate());
            screeningInfo.put("screeningResult", screening.getScreeningResult().name());
            screeningInfo.put("rejectionReason", screening.getRejectionReason());
            screeningInfo.put("notes", screening.getNotes());
            screeningInfo.put("screenedAt", screening.getScreenedAt());
            result.put("screening", screeningInfo);
            
            // Thông tin appointment
            Map<String, Object> appointmentInfo = new HashMap<>();
            appointmentInfo.put("id", appointment.getId());
            appointmentInfo.put("bookingCode", appointment.getBookingCode());
            appointmentInfo.put("appointmentDate", appointment.getAppointmentDate());
            appointmentInfo.put("appointmentTime", appointment.getAppointmentTime());
            appointmentInfo.put("status", appointment.getStatus().name());
            appointmentInfo.put("doseNumber", appointment.getDoseNumber());
            
            // Thông tin vaccine
            if (appointment.getVaccine() != null) {
                Map<String, Object> vaccineInfo = new HashMap<>();
                vaccineInfo.put("id", appointment.getVaccine().getId());
                vaccineInfo.put("name", appointment.getVaccine().getName());
                appointmentInfo.put("vaccineInfo", vaccineInfo);
            }
            
            // Thông tin trung tâm
            if (appointment.getCenter() != null) {
                appointmentInfo.put("centerName", appointment.getCenter().getName());
            }
            
            // Thông tin vaccination record (nếu đã tiêm)
            if (appointment.getVaccinationRecord() != null) {
                VaccinationRecord record = appointment.getVaccinationRecord();
                Map<String, Object> vaccinationInfo = new HashMap<>();
                vaccinationInfo.put("id", record.getId());
                vaccinationInfo.put("injectionDate", record.getInjectionDate());
                vaccinationInfo.put("injectionTime", record.getInjectionTime());
                vaccinationInfo.put("batchNumber", record.getBatchNumber());
                vaccinationInfo.put("certificateNumber", record.getCertificateNumber());
                vaccinationInfo.put("injectionSite", record.getInjectionSite());
                vaccinationInfo.put("doseAmount", record.getDoseAmount());
                vaccinationInfo.put("nextDoseDate", record.getNextDoseDate());
                vaccinationInfo.put("notes", record.getNotes()); // Ghi chú phản ứng sau tiêm và ghi chú chi tiết khác
                if (record.getNurse() != null) {
                    vaccinationInfo.put("nurseName", record.getNurse().getFullName());
                    vaccinationInfo.put("nurseId", record.getNurse().getId());
                }
                appointmentInfo.put("vaccinationRecord", vaccinationInfo);
            }
            
            result.put("appointment", appointmentInfo);
            
            // Thông tin bệnh nhân
            Map<String, Object> patientInfo = new HashMap<>();
            if (appointment.getBookedForUser() != null) {
                patientInfo.put("userId", appointment.getBookedForUser().getId()); // Thêm userId
                patientInfo.put("fullName", appointment.getBookedForUser().getFullName());
                patientInfo.put("email", appointment.getBookedForUser().getEmail());
                patientInfo.put("phoneNumber", appointment.getBookedForUser().getPhoneNumber());
                patientInfo.put("dateOfBirth", appointment.getBookedForUser().getDayOfBirth());
                patientInfo.put("gender", appointment.getBookedForUser().getGender() != null ? appointment.getBookedForUser().getGender().name() : null);
                patientInfo.put("citizenId", appointment.getBookedForUser().getCitizenId());
            } else if (appointment.getFamilyMember() != null) {
                // Với family member, userId là của user chủ (bookedByUser)
                patientInfo.put("userId", appointment.getBookedByUser() != null ? appointment.getBookedByUser().getId() : null);
                patientInfo.put("familyMemberId", appointment.getFamilyMember().getId());
                patientInfo.put("fullName", appointment.getFamilyMember().getFullName());
                patientInfo.put("phoneNumber", appointment.getFamilyMember().getPhoneNumber());
                patientInfo.put("dateOfBirth", appointment.getFamilyMember().getDateOfBirth());
                patientInfo.put("gender", appointment.getFamilyMember().getGender() != null ? appointment.getFamilyMember().getGender().name() : null);
                patientInfo.put("citizenId", appointment.getFamilyMember().getCitizenId());
            } else {
                patientInfo.put("userId", appointment.getBookedByUser().getId()); // Thêm userId
                patientInfo.put("fullName", appointment.getBookedByUser().getFullName());
                patientInfo.put("email", appointment.getBookedByUser().getEmail());
                patientInfo.put("phoneNumber", appointment.getBookedByUser().getPhoneNumber());
                patientInfo.put("dateOfBirth", appointment.getBookedByUser().getDayOfBirth());
                patientInfo.put("gender", appointment.getBookedByUser().getGender() != null ? appointment.getBookedByUser().getGender().name() : null);
                patientInfo.put("citizenId", appointment.getBookedByUser().getCitizenId());
            }
            result.put("patientInfo", patientInfo);
            
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
}

