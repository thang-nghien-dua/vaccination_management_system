package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.VaccinationRecordDTO;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.VaccinationRecordService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vaccination-records")
public class VaccinationRecordController {
    
    @Autowired
    private VaccinationRecordService vaccinationRecordService;
    
    @Autowired
    private UserRepository userRepository;
    
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
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        
        throw new RuntimeException("User not found");
    }
    
    /**
     * POST /api/vaccination-records
     * Nhập thông tin tiêm vaccine
     * - Tạo VaccinationRecord
     * - Cập nhật VaccineLot (giảm remainingQuantity)
     * - Cập nhật Appointment status: COMPLETED
     * - Tạo AppointmentHistory
     * - Tạo certificateNumber
     * - Tính nextDoseDate (nếu có)
     * - Gửi email chứng nhận
     */
    @PostMapping
    public ResponseEntity<?> createVaccinationRecord(@RequestBody VaccinationRecordDTO dto) {
        try {
            User currentUser = getCurrentUser();
            
            // Validate required fields
            if (dto.getAppointmentId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Appointment ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (dto.getVaccineLotId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Vaccine lot ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (dto.getNurseId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Nurse ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Tạo VaccinationRecord
            VaccinationRecord record = vaccinationRecordService.createVaccinationRecord(
                    dto.getAppointmentId(),
                    dto.getVaccineLotId(),
                    dto.getNurseId(),
                    dto.getInjectionDate() != null ? dto.getInjectionDate() : LocalDate.now(),
                    dto.getInjectionTime() != null ? dto.getInjectionTime() : LocalTime.now(),
                    dto.getInjectionSite(),
                    dto.getDoseAmount(),
                    null // certificateNumber sẽ được tự động generate
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", record.getId());
            result.put("appointmentId", record.getAppointment().getId());
            result.put("vaccineName", record.getVaccine().getName());
            result.put("injectionDate", record.getInjectionDate());
            result.put("injectionTime", record.getInjectionTime());
            result.put("doseNumber", record.getDoseNumber());
            result.put("certificateNumber", record.getCertificateNumber());
            result.put("batchNumber", record.getBatchNumber());
            result.put("nextDoseDate", record.getNextDoseDate());
            result.put("message", "Vaccination record created successfully");
            
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
     * GET /api/vaccination-records/{id}
     * Xem hồ sơ tiêm vaccine
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVaccinationRecord(@PathVariable Long id) {
        try {
            VaccinationRecord record = vaccinationRecordService.getVaccinationRecordById(id);
            User currentUser = getCurrentUser();
            
            // Kiểm tra quyền: chỉ user sở hữu hoặc admin mới xem được
            if (!record.getUser().getId().equals(currentUser.getId()) && 
                currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN &&
                currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE &&
                currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.DOCTOR) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You don't have permission to view this vaccination record");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", record.getId());
            result.put("appointmentId", record.getAppointment().getId());
            result.put("bookingCode", record.getAppointment().getBookingCode());
            result.put("vaccineId", record.getVaccine().getId());
            result.put("vaccineName", record.getVaccine().getName());
            result.put("vaccineLotId", record.getVaccineLot().getId());
            result.put("lotNumber", record.getVaccineLot().getLotNumber());
            result.put("nurseId", record.getNurse().getId());
            result.put("nurseName", record.getNurse().getFullName());
            result.put("injectionDate", record.getInjectionDate());
            result.put("injectionTime", record.getInjectionTime());
            result.put("doseNumber", record.getDoseNumber());
            result.put("injectionSite", record.getInjectionSite());
            result.put("batchNumber", record.getBatchNumber());
            result.put("doseAmount", record.getDoseAmount());
            result.put("certificateNumber", record.getCertificateNumber());
            result.put("nextDoseDate", record.getNextDoseDate());
            result.put("createdAt", record.getCreatedAt());
            
            // Thông tin người được tiêm
            if (record.getAppointment().getFamilyMember() != null) {
                result.put("patientName", record.getAppointment().getFamilyMember().getFullName());
                result.put("patientType", "FAMILY_MEMBER");
            } else {
                result.put("patientName", record.getUser().getFullName());
                result.put("patientType", "USER");
            }
            
            // Thông tin trung tâm
            if (record.getAppointment().getCenter() != null) {
                result.put("centerId", record.getAppointment().getCenter().getId());
                result.put("centerName", record.getAppointment().getCenter().getName());
            }
            
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
     * GET /api/vaccination-records/{id}/certificate
     * Download chứng nhận PDF (tạm thời trả về text, có thể tích hợp thư viện PDF sau)
     */
    @GetMapping("/{id}/certificate")
    public ResponseEntity<?> downloadCertificate(@PathVariable Long id) {
        try {
            VaccinationRecord record = vaccinationRecordService.getVaccinationRecordById(id);
            User currentUser = getCurrentUser();
            
            // Kiểm tra quyền
            if (!record.getUser().getId().equals(currentUser.getId()) && 
                currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "You don't have permission to download this certificate");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Tạo nội dung chứng nhận (text format, có thể chuyển sang PDF sau)
            StringBuilder certificateContent = new StringBuilder();
            certificateContent.append("═══════════════════════════════════════════════════════\n");
            certificateContent.append("        CHỨNG NHẬN TIÊM CHỦNG VACCINE\n");
            certificateContent.append("═══════════════════════════════════════════════════════\n\n");
            certificateContent.append("Số chứng nhận: ").append(record.getCertificateNumber()).append("\n\n");
            
            if (record.getAppointment().getFamilyMember() != null) {
                certificateContent.append("Họ và tên: ").append(record.getAppointment().getFamilyMember().getFullName()).append("\n");
            } else {
                certificateContent.append("Họ và tên: ").append(record.getUser().getFullName()).append("\n");
            }
            
            certificateContent.append("Vaccine: ").append(record.getVaccine().getName()).append("\n");
            certificateContent.append("Mũi tiêm: ").append(record.getDoseNumber()).append("\n");
            certificateContent.append("Ngày tiêm: ").append(record.getInjectionDate()).append("\n");
            certificateContent.append("Giờ tiêm: ").append(record.getInjectionTime()).append("\n");
            certificateContent.append("Số lô: ").append(record.getBatchNumber()).append("\n");
            
            if (record.getAppointment().getCenter() != null) {
                certificateContent.append("Trung tâm tiêm chủng: ").append(record.getAppointment().getCenter().getName()).append("\n");
            }
            
            certificateContent.append("Người thực hiện: ").append(record.getNurse().getFullName()).append("\n");
            
            if (record.getNextDoseDate() != null) {
                certificateContent.append("Ngày tiêm mũi tiếp theo: ").append(record.getNextDoseDate()).append("\n");
            }
            
            certificateContent.append("\n═══════════════════════════════════════════════════════\n");
            certificateContent.append("Chứng nhận này có giá trị pháp lý và được lưu trữ trong hệ thống.\n");
            certificateContent.append("═══════════════════════════════════════════════════════\n");
            
            // Trả về text (có thể chuyển sang PDF sau)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", 
                    "certificate_" + record.getCertificateNumber() + ".txt");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(certificateContent.toString());
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
}

