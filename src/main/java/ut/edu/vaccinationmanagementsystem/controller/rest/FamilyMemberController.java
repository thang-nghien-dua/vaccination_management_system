package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.FamilyMemberDTO;
import ut.edu.vaccinationmanagementsystem.entity.FamilyMember;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.FamilyMemberService;
import ut.edu.vaccinationmanagementsystem.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationRecordRepository;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;

@RestController
@RequestMapping("/api/family-members")
public class FamilyMemberController {
    
    @Autowired
    private FamilyMemberService familyMemberService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
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
        
        // Nếu là OAuth2 user
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return customOAuth2User.getUser();
        }
        
        // Nếu là email/password login
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
    
    /**
     * GET /api/family-members
     * Lấy danh sách người thân của user hiện tại
     */
    @GetMapping
    public ResponseEntity<?> getFamilyMembers() {
        try {
            User currentUser = getCurrentUser();
            List<FamilyMember> familyMembers = familyMemberService.getFamilyMembersByUser(currentUser);
            
            List<Map<String, Object>> result = familyMembers.stream().map(fm -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", fm.getId());
                map.put("fullName", fm.getFullName());
                map.put("dateOfBirth", fm.getDateOfBirth());
                map.put("gender", fm.getGender() != null ? fm.getGender().name() : null);
                map.put("citizenId", fm.getCitizenId());
                map.put("phoneNumber", fm.getPhoneNumber());
                map.put("relationship", fm.getRelationship().name());
                map.put("createdAt", fm.getCreatedAt());
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/family-members/{id}
     * Lấy thông tin một người thân theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getFamilyMember(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            FamilyMember familyMember = familyMemberService.getFamilyMemberById(id, currentUser);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", familyMember.getId());
            result.put("fullName", familyMember.getFullName());
            result.put("dateOfBirth", familyMember.getDateOfBirth());
            result.put("gender", familyMember.getGender() != null ? familyMember.getGender().name() : null);
            result.put("citizenId", familyMember.getCitizenId());
            result.put("phoneNumber", familyMember.getPhoneNumber());
            result.put("relationship", familyMember.getRelationship().name());
            result.put("createdAt", familyMember.getCreatedAt());
            
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
     * POST /api/family-members
     * Thêm người thân mới
     */
    @PostMapping
    public ResponseEntity<?> createFamilyMember(@RequestBody FamilyMemberDTO dto) {
        try {
            User currentUser = getCurrentUser();
            FamilyMember familyMember = familyMemberService.createFamilyMember(dto, currentUser);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", familyMember.getId());
            result.put("fullName", familyMember.getFullName());
            result.put("dateOfBirth", familyMember.getDateOfBirth());
            result.put("gender", familyMember.getGender() != null ? familyMember.getGender().name() : null);
            result.put("citizenId", familyMember.getCitizenId());
            result.put("phoneNumber", familyMember.getPhoneNumber());
            result.put("relationship", familyMember.getRelationship().name());
            result.put("createdAt", familyMember.getCreatedAt());
            result.put("message", "Family member added successfully");
            
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
     * PUT /api/family-members/{id}
     * Cập nhật thông tin người thân
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFamilyMember(@PathVariable Long id, @RequestBody FamilyMemberDTO dto) {
        try {
            User currentUser = getCurrentUser();
            FamilyMember familyMember = familyMemberService.updateFamilyMember(id, dto, currentUser);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", familyMember.getId());
            result.put("fullName", familyMember.getFullName());
            result.put("dateOfBirth", familyMember.getDateOfBirth());
            result.put("gender", familyMember.getGender() != null ? familyMember.getGender().name() : null);
            result.put("citizenId", familyMember.getCitizenId());
            result.put("phoneNumber", familyMember.getPhoneNumber());
            result.put("relationship", familyMember.getRelationship().name());
            result.put("createdAt", familyMember.getCreatedAt());
            result.put("message", "Family member updated successfully");
            
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
     * DELETE /api/family-members/{id}
     * Xóa người thân
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFamilyMember(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            familyMemberService.deleteFamilyMember(id, currentUser);
            
            Map<String, String> result = new HashMap<>();
            result.put("message", "Family member deleted successfully");
            
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
     * GET /api/family-members/{id}/details
     * Lấy thông tin chi tiết của người thân (bao gồm appointments, vaccination records)
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getFamilyMemberDetails(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            FamilyMember familyMember = familyMemberService.getFamilyMemberById(id, currentUser);
            
            // Lấy appointments của family member
            List<Appointment> appointments = 
                appointmentRepository.findByFamilyMemberIdOrderByAppointmentDateDesc(id);
            
            // Lấy vaccination records thông qua appointments
            List<VaccinationRecord> vaccinationRecords = 
                vaccinationRecordRepository.findByAppointmentFamilyMemberIdOrderByInjectionDateDesc(id);
            
            // Build response
            Map<String, Object> result = new HashMap<>();
            
            // Basic info
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("id", familyMember.getId());
            basicInfo.put("fullName", familyMember.getFullName());
            basicInfo.put("dateOfBirth", familyMember.getDateOfBirth());
            basicInfo.put("gender", familyMember.getGender() != null ? familyMember.getGender().name() : null);
            basicInfo.put("citizenId", familyMember.getCitizenId());
            basicInfo.put("phoneNumber", familyMember.getPhoneNumber());
            basicInfo.put("phoneVerified", familyMember.getPhoneVerified());
            basicInfo.put("relationship", familyMember.getRelationship().name());
            basicInfo.put("createdAt", familyMember.getCreatedAt());
            result.put("basicInfo", basicInfo);
            
            // Appointments
            List<Map<String, Object>> appointmentsList = appointments.stream().map(apt -> {
                Map<String, Object> aptMap = new HashMap<>();
                aptMap.put("id", apt.getId());
                aptMap.put("bookingCode", apt.getBookingCode());
                aptMap.put("vaccineName", apt.getVaccine() != null ? apt.getVaccine().getName() : null);
                aptMap.put("centerName", apt.getCenter() != null ? apt.getCenter().getName() : null);
                aptMap.put("roomNumber", apt.getRoom() != null ? apt.getRoom().getRoomNumber() : null);
                aptMap.put("appointmentDate", apt.getAppointmentDate());
                aptMap.put("appointmentTime", apt.getAppointmentTime());
                aptMap.put("status", apt.getStatus() != null ? apt.getStatus().name() : null);
                aptMap.put("doseNumber", apt.getDoseNumber());
                aptMap.put("notes", apt.getNotes());
                return aptMap;
            }).collect(Collectors.toList());
            result.put("appointments", appointmentsList);
            
            // Vaccination records
            List<Map<String, Object>> recordsList = vaccinationRecords.stream().map(record -> {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("vaccineName", record.getVaccine() != null ? record.getVaccine().getName() : null);
                recordMap.put("injectionDate", record.getInjectionDate());
                recordMap.put("injectionTime", record.getInjectionTime());
                recordMap.put("doseNumber", record.getDoseNumber());
                recordMap.put("batchNumber", record.getBatchNumber());
                recordMap.put("certificateNumber", record.getCertificateNumber());
                recordMap.put("nextDoseDate", record.getNextDoseDate());
                recordMap.put("centerName", record.getAppointment() != null && record.getAppointment().getCenter() != null 
                    ? record.getAppointment().getCenter().getName() : null);
                return recordMap;
            }).collect(Collectors.toList());
            result.put("vaccinationRecords", recordsList);
            
            // Statistics
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalAppointments", appointments.size());
            statistics.put("totalVaccinations", recordsList.size());
            statistics.put("upcomingAppointments", appointments.stream()
                .filter(apt -> apt.getAppointmentDate() != null && 
                       apt.getAppointmentDate().isAfter(java.time.LocalDate.now()) &&
                       (apt.getStatus() == null || 
                        apt.getStatus().name().equals("PENDING") || 
                        apt.getStatus().name().equals("CONFIRMED")))
                .count());
            result.put("statistics", statistics);
            
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
}
