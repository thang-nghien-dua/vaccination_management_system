package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.CreateVaccinationRecordDTO;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.VaccinationRecordService;
import ut.edu.vaccinationmanagementsystem.service.AdverseReactionService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vaccination-records")
public class VaccinationRecordController {
    
    @Autowired
    private VaccinationRecordService vaccinationRecordService;
    
    @Autowired
    private AdverseReactionService adverseReactionService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * POST /api/vaccination-records
     * Nhập thông tin tiêm (cho Nurse)
     */
    @PostMapping
    public ResponseEntity<?> createVaccinationRecord(@RequestBody CreateVaccinationRecordDTO dto) {
        try {
            // Lấy current user (nurse)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            User currentUser = null;
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                currentUser = customOAuth2User.getUser();
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                currentUser = customUserDetails.getUser();
            } else {
                String email = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    currentUser = userOpt.get();
                }
            }
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Validate DTO
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
            if (dto.getInjectionSite() == null || dto.getInjectionSite().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Injection site is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Parse injectionTime nếu là String
            LocalTime injectionTime = dto.getInjectionTime();
            if (injectionTime == null) {
                injectionTime = LocalTime.now();
            }
            
            // Parse injectionDate nếu là String
            LocalDate injectionDate = dto.getInjectionDate();
            if (injectionDate == null) {
                injectionDate = LocalDate.now();
            }
            
            VaccinationRecord record = vaccinationRecordService.createVaccinationRecordWithCertificate(
                dto.getAppointmentId(),
                dto.getVaccineLotId(),
                currentUser.getId(),
                injectionDate,
                injectionTime,
                dto.getInjectionSite(),
                dto.getDoseAmount()
            );
            
            // Convert to Map để tránh circular reference khi serialize JSON
            Map<String, Object> recordDTO = new HashMap<>();
            recordDTO.put("id", record.getId());
            recordDTO.put("certificateNumber", record.getCertificateNumber());
            recordDTO.put("injectionDate", record.getInjectionDate());
            recordDTO.put("injectionTime", record.getInjectionTime());
            recordDTO.put("injectionSite", record.getInjectionSite());
            recordDTO.put("doseNumber", record.getDoseNumber());
            recordDTO.put("doseAmount", record.getDoseAmount());
            recordDTO.put("batchNumber", record.getBatchNumber());
            recordDTO.put("nextDoseDate", record.getNextDoseDate());
            recordDTO.put("createdAt", record.getCreatedAt());
            
            // Appointment info (chỉ basic info, không include histories để tránh circular reference)
            if (record.getAppointment() != null) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("id", record.getAppointment().getId());
                appointment.put("bookingCode", record.getAppointment().getBookingCode());
                appointment.put("appointmentDate", record.getAppointment().getAppointmentDate());
                appointment.put("appointmentTime", record.getAppointment().getAppointmentTime());
                appointment.put("status", record.getAppointment().getStatus() != null ? record.getAppointment().getStatus().name() : null);
                recordDTO.put("appointment", appointment);
            }
            
            // Patient info
            if (record.getUser() != null) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", record.getUser().getId());
                user.put("fullName", record.getUser().getFullName());
                user.put("email", record.getUser().getEmail());
                user.put("phoneNumber", record.getUser().getPhoneNumber());
                recordDTO.put("user", user);
            }
            
            // Vaccine info
            if (record.getVaccine() != null) {
                Map<String, Object> vaccine = new HashMap<>();
                vaccine.put("id", record.getVaccine().getId());
                vaccine.put("name", record.getVaccine().getName());
                vaccine.put("manufacturer", record.getVaccine().getManufacturer());
                recordDTO.put("vaccine", vaccine);
            }
            
            // Vaccine lot info
            if (record.getVaccineLot() != null) {
                Map<String, Object> lot = new HashMap<>();
                lot.put("id", record.getVaccineLot().getId());
                lot.put("lotNumber", record.getVaccineLot().getLotNumber());
                lot.put("expiryDate", record.getVaccineLot().getExpiryDate());
                recordDTO.put("vaccineLot", lot);
            }
            
            // Nurse info
            if (record.getNurse() != null) {
                Map<String, Object> nurse = new HashMap<>();
                nurse.put("id", record.getNurse().getId());
                nurse.put("fullName", record.getNurse().getFullName());
                recordDTO.put("nurse", nurse);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(recordDTO);
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
     * Xem hồ sơ tiêm (cho Nurse)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVaccinationRecord(@PathVariable Long id) {
        try {
            VaccinationRecord record = vaccinationRecordService.getVaccinationRecordById(id);
            
            // Convert to Map để tránh circular reference
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", record.getId());
            dto.put("certificateNumber", record.getCertificateNumber());
            dto.put("injectionDate", record.getInjectionDate());
            dto.put("injectionTime", record.getInjectionTime());
            dto.put("injectionSite", record.getInjectionSite());
            dto.put("doseNumber", record.getDoseNumber());
            dto.put("doseAmount", record.getDoseAmount());
            dto.put("batchNumber", record.getBatchNumber());
            dto.put("nextDoseDate", record.getNextDoseDate());
            dto.put("createdAt", record.getCreatedAt());
            
            // Appointment info
            if (record.getAppointment() != null) {
                Map<String, Object> appointment = new HashMap<>();
                appointment.put("id", record.getAppointment().getId());
                appointment.put("bookingCode", record.getAppointment().getBookingCode());
                appointment.put("appointmentDate", record.getAppointment().getAppointmentDate());
                appointment.put("appointmentTime", record.getAppointment().getAppointmentTime());
                dto.put("appointment", appointment);
            }
            
            // Patient info
            if (record.getUser() != null) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", record.getUser().getId());
                user.put("fullName", record.getUser().getFullName());
                user.put("email", record.getUser().getEmail());
                user.put("phoneNumber", record.getUser().getPhoneNumber());
                dto.put("user", user);
            }
            
            // Vaccine info
            if (record.getVaccine() != null) {
                Map<String, Object> vaccine = new HashMap<>();
                vaccine.put("id", record.getVaccine().getId());
                vaccine.put("name", record.getVaccine().getName());
                vaccine.put("manufacturer", record.getVaccine().getManufacturer());
                dto.put("vaccine", vaccine);
            }
            
            // Vaccine lot info
            if (record.getVaccineLot() != null) {
                Map<String, Object> lot = new HashMap<>();
                lot.put("id", record.getVaccineLot().getId());
                lot.put("lotNumber", record.getVaccineLot().getLotNumber());
                lot.put("expiryDate", record.getVaccineLot().getExpiryDate());
                dto.put("vaccineLot", lot);
            }
            
            // Nurse info
            if (record.getNurse() != null) {
                Map<String, Object> nurse = new HashMap<>();
                nurse.put("id", record.getNurse().getId());
                nurse.put("fullName", record.getNurse().getFullName());
                dto.put("nurse", nurse);
            }
            
            // Adverse reactions (convert to DTO để tránh circular reference)
            List<ut.edu.vaccinationmanagementsystem.entity.AdverseReaction> adverseReactions = adverseReactionService.getAdverseReactionsByVaccinationRecordId(id);
            List<Map<String, Object>> adverseReactionDTOs = adverseReactions.stream().map(reaction -> {
                Map<String, Object> reactionDTO = new HashMap<>();
                reactionDTO.put("id", reaction.getId());
                reactionDTO.put("reactionType", reaction.getReactionType() != null ? reaction.getReactionType().name() : null);
                reactionDTO.put("symptoms", reaction.getSymptoms());
                reactionDTO.put("occurredAt", reaction.getOccurredAt());
                reactionDTO.put("resolved", reaction.getResolved());
                reactionDTO.put("notes", reaction.getNotes());
                
                // Handled by info
                if (reaction.getHandledBy() != null) {
                    Map<String, Object> handledBy = new HashMap<>();
                    handledBy.put("id", reaction.getHandledBy().getId());
                    handledBy.put("fullName", reaction.getHandledBy().getFullName());
                    reactionDTO.put("handledBy", handledBy);
                }
                
                // Treatment
                reactionDTO.put("treatment", reaction.getTreatment());
                
                return reactionDTO;
            }).collect(java.util.stream.Collectors.toList());
            dto.put("adverseReactions", adverseReactionDTOs);
            
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccination-records/{id}/certificate
     * Download chứng nhận PDF (tạm thời trả về thông tin, chưa có PDF generator)
     */
    @GetMapping("/{id}/certificate")
    public ResponseEntity<?> getCertificate(@PathVariable Long id) {
        try {
            VaccinationRecord record = vaccinationRecordService.getVaccinationRecordById(id);
            
            // TODO: Generate PDF certificate
            // Tạm thời trả về JSON với thông tin certificate
            Map<String, Object> certificate = new HashMap<>();
            certificate.put("certificateNumber", record.getCertificateNumber());
            certificate.put("patientName", record.getUser() != null ? record.getUser().getFullName() : "N/A");
            certificate.put("vaccineName", record.getVaccine() != null ? record.getVaccine().getName() : "N/A");
            certificate.put("injectionDate", record.getInjectionDate());
            certificate.put("injectionTime", record.getInjectionTime());
            certificate.put("doseNumber", record.getDoseNumber());
            certificate.put("centerName", record.getAppointment() != null && record.getAppointment().getCenter() != null 
                ? record.getAppointment().getCenter().getName() : "N/A");
            
            return ResponseEntity.ok(certificate);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/vaccination-records/{id}/adverse-reactions
     * Xem phản ứng của lần tiêm
     */
    @GetMapping("/{id}/adverse-reactions")
    public ResponseEntity<?> getAdverseReactions(@PathVariable Long id) {
        try {
            // Verify vaccination record exists
            vaccinationRecordService.getVaccinationRecordById(id);
            
            return ResponseEntity.ok(adverseReactionService.getAdverseReactionsByVaccinationRecordId(id));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
