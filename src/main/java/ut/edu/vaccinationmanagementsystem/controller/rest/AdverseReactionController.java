package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.entity.AdverseReaction;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;
import ut.edu.vaccinationmanagementsystem.entity.enums.ReactionType;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.repository.AdverseReactionRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationRecordRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/adverse-reactions")
public class AdverseReactionController {
    
    @Autowired
    private AdverseReactionRepository adverseReactionRepository;
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.StaffInfoRepository staffInfoRepository;
    
    /**
     * GET /api/adverse-reactions
     * Lấy danh sách tất cả phản ứng phụ (cho Admin, Doctor, Nurse)
     */
    @GetMapping
    public ResponseEntity<?> getAllAdverseReactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String reactionType,
            @RequestParam(required = false) Boolean resolved) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra quyền: ADMIN, DOCTOR, hoặc NURSE
            if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DOCTOR && currentUser.getRole() != Role.NURSE) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Sử dụng query với JOIN FETCH để load tất cả relationships
            List<AdverseReaction> reactions = adverseReactionRepository.findAllWithRelationships();
            
            // Lọc theo trung tâm (ngoại trừ ADMIN)
            if (currentUser.getRole() != Role.ADMIN) {
                ut.edu.vaccinationmanagementsystem.entity.StaffInfo staffInfo = staffInfoRepository.findByUser(currentUser).orElse(null);
                if (staffInfo != null && staffInfo.getCenter() != null) {
                    Long centerId = staffInfo.getCenter().getId();
                    reactions = reactions.stream()
                        .filter(ar -> ar.getVaccinationRecord() != null && 
                                     ar.getVaccinationRecord().getAppointment() != null && 
                                     ar.getVaccinationRecord().getAppointment().getCenter() != null && 
                                     ar.getVaccinationRecord().getAppointment().getCenter().getId().equals(centerId))
                        .collect(Collectors.toList());
                }
            }
            
            // Filter by resolved status
            if (resolved != null) {
                reactions = reactions.stream()
                    .filter(ar -> ar.getResolved() == resolved)
                    .collect(Collectors.toList());
            }
            
            // Filter by reaction type
            if (reactionType != null && !reactionType.trim().isEmpty()) {
                try {
                    ReactionType type = ReactionType.valueOf(reactionType.toUpperCase());
                    reactions = reactions.stream()
                        .filter(ar -> ar.getReactionType() == type)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Invalid reaction type, ignore filter
                }
            }
            
            // Filter by search (search in user name, vaccine name, symptoms)
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase().trim();
                reactions = reactions.stream()
                    .filter(ar -> {
                        if (ar.getVaccinationRecord() != null && ar.getVaccinationRecord().getUser() != null) {
                            String userName = ar.getVaccinationRecord().getUser().getFullName();
                            if (userName != null && userName.toLowerCase().contains(searchLower)) {
                                return true;
                            }
                        }
                        if (ar.getVaccinationRecord() != null && ar.getVaccinationRecord().getVaccine() != null) {
                            String vaccineName = ar.getVaccinationRecord().getVaccine().getName();
                            if (vaccineName != null && vaccineName.toLowerCase().contains(searchLower)) {
                                return true;
                            }
                        }
                        if (ar.getSymptoms() != null && ar.getSymptoms().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            }
            
            // Convert to response format
            List<Map<String, Object>> result = reactions.stream().map(reaction -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", reaction.getId());
                map.put("reactionType", reaction.getReactionType() != null ? reaction.getReactionType().name() : null);
                map.put("symptoms", reaction.getSymptoms());
                map.put("notes", reaction.getNotes());
                map.put("treatment", reaction.getTreatment());
                map.put("occurredAt", reaction.getOccurredAt());
                map.put("resolved", reaction.getResolved() != null ? reaction.getResolved() : false);
                
                // User info - luôn thêm user info nếu có vaccinationRecord
                if (reaction.getVaccinationRecord() != null) {
                    map.put("vaccinationRecordId", reaction.getVaccinationRecord().getId());
                    
                    // User info
                    if (reaction.getVaccinationRecord().getUser() != null) {
                        User user = reaction.getVaccinationRecord().getUser();
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", user.getId());
                        userInfo.put("fullName", user.getFullName());
                        userInfo.put("email", user.getEmail());
                        map.put("user", userInfo);
                    } else {
                        // Nếu không có user, vẫn thêm null để frontend không bị lỗi
                        map.put("user", null);
                    }
                    
                    // Vaccine info
                    if (reaction.getVaccinationRecord().getVaccine() != null) {
                        Map<String, Object> vaccineInfo = new HashMap<>();
                        vaccineInfo.put("id", reaction.getVaccinationRecord().getVaccine().getId());
                        vaccineInfo.put("name", reaction.getVaccinationRecord().getVaccine().getName());
                        map.put("vaccine", vaccineInfo);
                    } else {
                        // Nếu không có vaccine, vẫn thêm null
                        map.put("vaccine", null);
                    }
                } else {
                    // Nếu không có vaccinationRecord, vẫn thêm null để frontend không bị lỗi
                    map.put("vaccinationRecordId", null);
                    map.put("user", null);
                    map.put("vaccine", null);
                }
                
                // Handler info
                if (reaction.getHandledBy() != null) {
                    Map<String, Object> handlerInfo = new HashMap<>();
                    handlerInfo.put("id", reaction.getHandledBy().getId());
                    handlerInfo.put("fullName", reaction.getHandledBy().getFullName());
                    map.put("handledBy", handlerInfo);
                }
                
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/adverse-reactions/{id}
     * Lấy chi tiết phản ứng phụ (cho Admin, Doctor, Nurse)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdverseReactionById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra quyền: ADMIN, DOCTOR, hoặc NURSE
            if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.DOCTOR && currentUser.getRole() != Role.NURSE) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Optional<AdverseReaction> reactionOpt = adverseReactionRepository.findById(id);
            if (reactionOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Adverse reaction not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            AdverseReaction reaction = reactionOpt.get();
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("id", reaction.getId());
            response.put("reactionType", reaction.getReactionType() != null ? reaction.getReactionType().name() : null);
            response.put("symptoms", reaction.getSymptoms());
            response.put("notes", reaction.getNotes());
            response.put("treatment", reaction.getTreatment());
            response.put("occurredAt", reaction.getOccurredAt());
            response.put("resolved", reaction.getResolved());
            
            // User info
            if (reaction.getVaccinationRecord() != null && reaction.getVaccinationRecord().getUser() != null) {
                User user = reaction.getVaccinationRecord().getUser();
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("email", user.getEmail());
                userInfo.put("phoneNumber", user.getPhoneNumber());
                userInfo.put("dateOfBirth", user.getDayOfBirth());
                userInfo.put("gender", user.getGender() != null ? user.getGender().name() : null);
                response.put("user", userInfo);
            }
            
            // Vaccine info
            if (reaction.getVaccinationRecord() != null && reaction.getVaccinationRecord().getVaccine() != null) {
                Map<String, Object> vaccineInfo = new HashMap<>();
                vaccineInfo.put("id", reaction.getVaccinationRecord().getVaccine().getId());
                vaccineInfo.put("name", reaction.getVaccinationRecord().getVaccine().getName());
                vaccineInfo.put("code", reaction.getVaccinationRecord().getVaccine().getCode());
                response.put("vaccine", vaccineInfo);
            }
            
            // Vaccination record info
            if (reaction.getVaccinationRecord() != null) {
                VaccinationRecord record = reaction.getVaccinationRecord();
                Map<String, Object> recordInfo = new HashMap<>();
                recordInfo.put("id", record.getId());
                recordInfo.put("doseNumber", record.getDoseNumber());
                // Combine injectionDate and injectionTime
                if (record.getInjectionDate() != null && record.getInjectionTime() != null) {
                    recordInfo.put("vaccinationDate", record.getInjectionDate().atTime(record.getInjectionTime()));
                } else if (record.getInjectionDate() != null) {
                    recordInfo.put("vaccinationDate", record.getInjectionDate().atStartOfDay());
                } else {
                    recordInfo.put("vaccinationDate", null);
                }
                // Get center from appointment (appointment has both center and slot)
                if (record.getAppointment() != null) {
                    if (record.getAppointment().getCenter() != null) {
                        recordInfo.put("centerName", record.getAppointment().getCenter().getName());
                    } else if (record.getAppointment().getSlot() != null && 
                               record.getAppointment().getSlot().getCenter() != null) {
                        recordInfo.put("centerName", record.getAppointment().getSlot().getCenter().getName());
                    }
                }
                response.put("vaccinationRecord", recordInfo);
            }
            
            // Handler info
            if (reaction.getHandledBy() != null) {
                Map<String, Object> handlerInfo = new HashMap<>();
                handlerInfo.put("id", reaction.getHandledBy().getId());
                handlerInfo.put("fullName", reaction.getHandledBy().getFullName());
                response.put("handledBy", handlerInfo);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/adverse-reactions
     * Tạo mới phản ứng phụ (cho Nurse)
     */
    @PostMapping
    public ResponseEntity<?> createAdverseReaction(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra quyền: chỉ NURSE mới có thể tạo adverse reaction
            if (currentUser.getRole() != Role.NURSE) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only nurses can create adverse reactions");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Validate request body
            Long vaccinationRecordId = null;
            if (request.get("vaccinationRecordId") != null) {
                if (request.get("vaccinationRecordId") instanceof Number) {
                    vaccinationRecordId = ((Number) request.get("vaccinationRecordId")).longValue();
                } else {
                    vaccinationRecordId = Long.parseLong(request.get("vaccinationRecordId").toString());
                }
            }
            
            if (vaccinationRecordId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "vaccinationRecordId is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Tìm vaccination record
            Optional<VaccinationRecord> recordOpt = vaccinationRecordRepository.findById(vaccinationRecordId);
            if (recordOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Vaccination record not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            VaccinationRecord record = recordOpt.get();
            
            // Validate reaction type
            String reactionTypeStr = request.get("reactionType") != null ? request.get("reactionType").toString() : null;
            if (reactionTypeStr == null || reactionTypeStr.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "reactionType is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            ReactionType reactionType;
            try {
                reactionType = ReactionType.valueOf(reactionTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid reactionType. Must be MILD, MODERATE, or SEVERE");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Validate symptoms
            String symptoms = request.get("symptoms") != null ? request.get("symptoms").toString().trim() : null;
            if (symptoms == null || symptoms.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "symptoms is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Parse occurredAt
            LocalDateTime occurredAt = LocalDateTime.now();
            if (request.get("occurredAt") != null) {
                try {
                    occurredAt = LocalDateTime.parse(request.get("occurredAt").toString());
                } catch (Exception e) {
                    // Nếu parse lỗi, dùng thời gian hiện tại
                    occurredAt = LocalDateTime.now();
                }
            }
            
            // Parse notes (optional)
            String notes = request.get("notes") != null ? request.get("notes").toString().trim() : null;
            if (notes != null && notes.isEmpty()) {
                notes = null;
            }
            
            // Tạo adverse reaction
            AdverseReaction reaction = new AdverseReaction();
            reaction.setVaccinationRecord(record);
            reaction.setReactionType(reactionType);
            reaction.setSymptoms(symptoms);
            reaction.setOccurredAt(occurredAt);
            reaction.setNotes(notes);
            reaction.setResolved(false); // Mặc định chưa giải quyết
            reaction.setHandledBy(null); // Chưa có người xử lý
            
            AdverseReaction savedReaction = adverseReactionRepository.save(reaction);
            
            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedReaction.getId());
            response.put("reactionType", savedReaction.getReactionType().name());
            response.put("symptoms", savedReaction.getSymptoms());
            response.put("occurredAt", savedReaction.getOccurredAt());
            response.put("resolved", savedReaction.getResolved());
            response.put("notes", savedReaction.getNotes());
            response.put("vaccinationRecordId", savedReaction.getVaccinationRecord().getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * PUT /api/adverse-reactions/{id}/resolve
     * Đánh dấu phản ứng phụ đã được giải quyết (cho Nurse hoặc Doctor)
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveAdverseReaction(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra quyền: NURSE hoặc DOCTOR
            if (currentUser.getRole() != Role.NURSE && currentUser.getRole() != Role.DOCTOR) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only nurses or doctors can resolve adverse reactions");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            Optional<AdverseReaction> reactionOpt = adverseReactionRepository.findById(id);
            if (reactionOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Adverse reaction not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            AdverseReaction reaction = reactionOpt.get();
            
            // Parse treatment (optional)
            String treatment = null;
            if (request != null && request.get("treatment") != null) {
                treatment = request.get("treatment").toString().trim();
                if (treatment.isEmpty()) {
                    treatment = null;
                }
            }
            
            // Parse notes (optional)
            String notes = reaction.getNotes();
            if (request != null && request.get("notes") != null) {
                notes = request.get("notes").toString().trim();
                if (notes.isEmpty()) {
                    notes = null;
                }
            }
            
            // Cập nhật
            reaction.setResolved(true);
            reaction.setHandledBy(currentUser);
            if (treatment != null) {
                reaction.setTreatment(treatment);
            }
            if (notes != null) {
                reaction.setNotes(notes);
            }
            
            AdverseReaction savedReaction = adverseReactionRepository.save(reaction);
            
            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedReaction.getId());
            response.put("reactionType", savedReaction.getReactionType().name());
            response.put("symptoms", savedReaction.getSymptoms());
            response.put("occurredAt", savedReaction.getOccurredAt());
            response.put("resolved", savedReaction.getResolved());
            response.put("notes", savedReaction.getNotes());
            response.put("treatment", savedReaction.getTreatment());
            if (savedReaction.getHandledBy() != null) {
                Map<String, Object> handler = new HashMap<>();
                handler.put("id", savedReaction.getHandledBy().getId());
                handler.put("fullName", savedReaction.getHandledBy().getFullName());
                response.put("handledBy", handler);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Lấy current user từ SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return null;
        }
        
        try {
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                return customOAuth2User.getUser();
            } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                return customUserDetails.getUser();
            } else {
                String email = authentication.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                return userOpt.orElse(null);
            }
        } catch (Exception e) {
            return null;
        }
    }
}

