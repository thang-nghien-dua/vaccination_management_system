package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.dto.CreateAdverseReactionDTO;
import ut.edu.vaccinationmanagementsystem.dto.HandleAdverseReactionDTO;
import ut.edu.vaccinationmanagementsystem.entity.AdverseReaction;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.AdverseReactionService;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/adverse-reactions")
public class AdverseReactionController {
    
    @Autowired
    private AdverseReactionService adverseReactionService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * POST /api/adverse-reactions
     * Ghi nhận phản ứng phụ (cho Nurse)
     */
    @PostMapping
    public ResponseEntity<?> createAdverseReaction(@RequestBody CreateAdverseReactionDTO dto) {
        try {
            // Validate DTO
            if (dto.getVaccinationRecordId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Vaccination record ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (dto.getReactionType() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Reaction type is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (dto.getSymptoms() == null || dto.getSymptoms().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Symptoms are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            AdverseReaction reaction = adverseReactionService.createAdverseReaction(dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Adverse reaction recorded successfully");
            response.put("adverseReactionId", reaction.getId());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/adverse-reactions/{id}
     * Xem chi tiết phản ứng
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdverseReaction(@PathVariable Long id) {
        try {
            AdverseReaction reaction = adverseReactionService.getAdverseReactionById(id);
            
            // Convert to Map để tránh circular reference
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", reaction.getId());
            dto.put("reactionType", reaction.getReactionType());
            dto.put("symptoms", reaction.getSymptoms());
            dto.put("occurredAt", reaction.getOccurredAt());
            dto.put("notes", reaction.getNotes());
            dto.put("resolved", reaction.getResolved());
            dto.put("treatment", reaction.getTreatment());
            
            // Vaccination record info
            if (reaction.getVaccinationRecord() != null) {
                Map<String, Object> record = new HashMap<>();
                record.put("id", reaction.getVaccinationRecord().getId());
                record.put("certificateNumber", reaction.getVaccinationRecord().getCertificateNumber());
                record.put("injectionDate", reaction.getVaccinationRecord().getInjectionDate());
                if (reaction.getVaccinationRecord().getVaccine() != null) {
                    record.put("vaccineName", reaction.getVaccinationRecord().getVaccine().getName());
                }
                dto.put("vaccinationRecord", record);
            }
            
            // Handled by info
            if (reaction.getHandledBy() != null) {
                Map<String, Object> handledBy = new HashMap<>();
                handledBy.put("id", reaction.getHandledBy().getId());
                handledBy.put("fullName", reaction.getHandledBy().getFullName());
                dto.put("handledBy", handledBy);
            }
            
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
     * PUT /api/adverse-reactions/{id}/handle
     * Xử lý phản ứng phụ (cho Doctor)
     */
    @PutMapping("/{id}/handle")
    public ResponseEntity<?> handleAdverseReaction(@PathVariable Long id, @RequestBody HandleAdverseReactionDTO dto) {
        try {
            // Lấy current user (doctor)
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
            
            AdverseReaction reaction = adverseReactionService.handleAdverseReaction(id, dto, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Adverse reaction handled successfully");
            response.put("adverseReactionId", reaction.getId());
            response.put("resolved", reaction.getResolved());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
