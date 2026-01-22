package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.repository.*;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

import java.time.LocalDate;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nurse")
public class NurseController {
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private AdverseReactionRepository adverseReactionRepository;
    
    @Autowired
    private CenterVaccineRepository centerVaccineRepository;
    
    @Autowired
    private VaccineRepository vaccineRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ScreeningRepository screeningRepository;
    
    @Autowired
    private VaccineLotRepository vaccineLotRepository;
    
    @Autowired
    private ut.edu.vaccinationmanagementsystem.repository.StaffInfoRepository staffInfoRepository;
    
    /**
     * GET /api/nurse/statistics
     * Lấy thống kê tổng quan cho nurse dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            LocalDate today = LocalDate.now();
            
            // Lấy thông tin trung tâm của y tá
            ut.edu.vaccinationmanagementsystem.entity.StaffInfo staffInfo = staffInfoRepository.findByUser(currentUser).orElse(null);
            Long userCenterId = (staffInfo != null && staffInfo.getCenter() != null) ? staffInfo.getCenter().getId() : null;
            
            // 1. Số lượng mũi tiêm hôm nay (của nurse này)
            long injectionsToday = vaccinationRecordRepository.countByNurseIdAndInjectionDate(currentUser.getId(), today);
            
            // 2. Số lượng đang chờ (appointments APPROVED)
            long pendingCount;
            if (userCenterId != null) {
                pendingCount = appointmentRepository.countByStatusAndCenterId(AppointmentStatus.APPROVED, userCenterId);
            } else {
                pendingCount = appointmentRepository.countByStatus(AppointmentStatus.APPROVED);
            }
            
            // 3. Vaccine đã sử dụng hôm nay (danh sách vaccine và số lượng)
            List<VaccinationRecord> todayRecords;
            if (userCenterId != null) {
                todayRecords = vaccinationRecordRepository.findByInjectionDateAndAppointmentCenterId(today, userCenterId);
            } else {
                todayRecords = vaccinationRecordRepository.findByInjectionDate(today);
            }
            Map<Long, Map<String, Object>> vaccinesUsed = new HashMap<>();
            for (VaccinationRecord record : todayRecords) {
                if (record.getVaccine() != null) {
                    Long vaccineId = record.getVaccine().getId();
                    if (!vaccinesUsed.containsKey(vaccineId)) {
                        Map<String, Object> vaccineInfo = new HashMap<>();
                        vaccineInfo.put("id", vaccineId);
                        vaccineInfo.put("name", record.getVaccine().getName());
                        vaccineInfo.put("count", 0L);
                        vaccinesUsed.put(vaccineId, vaccineInfo);
                    }
                    Long currentCount = (Long) vaccinesUsed.get(vaccineId).get("count");
                    vaccinesUsed.get(vaccineId).put("count", currentCount + 1);
                }
            }
            
            // 4. Danh sách bệnh nhân chờ tiêm (appointments APPROVED)
            List<Appointment> pendingAppointments;
            if (userCenterId != null) {
                pendingAppointments = appointmentRepository.findByStatusAndCenterId(AppointmentStatus.APPROVED, userCenterId);
            } else {
                pendingAppointments = appointmentRepository.findByStatus(AppointmentStatus.APPROVED);
            }
            List<Map<String, Object>> waitingPatients = pendingAppointments.stream()
                    .limit(10) // Giới hạn 10 bệnh nhân đầu tiên
                    .map(apt -> {
                        Map<String, Object> patient = new HashMap<>();
                        if (apt.getBookedForUser() != null) {
                            patient.put("id", apt.getId());
                            patient.put("fullName", apt.getBookedForUser().getFullName());
                            patient.put("phoneNumber", apt.getBookedForUser().getPhoneNumber());
                            patient.put("appointmentDate", apt.getAppointmentDate());
                            patient.put("appointmentTime", apt.getAppointmentTime());
                            patient.put("vaccineName", apt.getVaccine() != null ? apt.getVaccine().getName() : "N/A");
                        } else if (apt.getFamilyMember() != null) {
                            patient.put("id", apt.getId());
                            patient.put("fullName", apt.getFamilyMember().getFullName());
                            patient.put("phoneNumber", apt.getFamilyMember().getPhoneNumber());
                            patient.put("appointmentDate", apt.getAppointmentDate());
                            patient.put("appointmentTime", apt.getAppointmentTime());
                            patient.put("vaccineName", apt.getVaccine() != null ? apt.getVaccine().getName() : "N/A");
                        } else {
                            patient.put("id", apt.getId());
                            patient.put("fullName", apt.getGuestFullName() != null ? apt.getGuestFullName() : "N/A");
                            patient.put("phoneNumber", apt.getConsultationPhone() != null ? apt.getConsultationPhone() : "N/A");
                            patient.put("appointmentDate", apt.getAppointmentDate());
                            patient.put("appointmentTime", apt.getAppointmentTime());
                            patient.put("vaccineName", apt.getVaccine() != null ? apt.getVaccine().getName() : "N/A");
                        }
                        return patient;
                    })
                    .collect(Collectors.toList());
            
            // 5. Số lượng phản ứng phụ chưa xử lý
            long unresolvedReactions = adverseReactionRepository.findUnresolvedReactions().size();
            
            // 6. Tổng số vaccine đã tiêm (tất cả thời gian)
            long totalInjections = vaccinationRecordRepository.count();
            
            // 7. Danh sách vaccine và số lượng tồn kho tại các trung tâm
            List<Vaccine> allVaccines = vaccineRepository.findAll();
            List<Map<String, Object>> vaccineInventory = allVaccines.stream()
                    .map(vaccine -> {
                        Map<String, Object> inv = new HashMap<>();
                        inv.put("id", vaccine.getId());
                        inv.put("name", vaccine.getName());
                        inv.put("manufacturer", vaccine.getManufacturer());
                        
                        // Lấy tổng số lượng tồn kho từ CenterVaccine
                        List<CenterVaccine> centerVaccines = centerVaccineRepository.findByVaccineId(vaccine.getId());
                        int totalStock = centerVaccines.stream()
                                .mapToInt(cv -> cv.getStockQuantity() != null ? cv.getStockQuantity() : 0)
                                .sum();
                        inv.put("totalStock", totalStock);
                        inv.put("centersCount", centerVaccines.size());
                        
                        return inv;
                    })
                    .sorted((a, b) -> Integer.compare((Integer) b.get("totalStock"), (Integer) a.get("totalStock")))
                    .collect(Collectors.toList());
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("injectionsToday", injectionsToday);
            statistics.put("pendingCount", pendingCount);
            statistics.put("vaccinesUsedToday", new ArrayList<>(vaccinesUsed.values()));
            statistics.put("waitingPatients", waitingPatients);
            statistics.put("unresolvedReactions", unresolvedReactions);
            statistics.put("totalInjections", totalInjections);
            statistics.put("vaccineInventory", vaccineInventory);
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/nurse/adverse-reactions
     * Lấy danh sách phản ứng phụ để theo dõi
     */
    @GetMapping("/adverse-reactions")
    public ResponseEntity<?> getAdverseReactions(@RequestParam(required = false) Boolean resolved) {
        try {
            List<AdverseReaction> reactions;
            if (resolved != null) {
                if (resolved) {
                    reactions = adverseReactionRepository.findAll().stream()
                            .filter(ar -> ar.getResolved())
                            .collect(Collectors.toList());
                } else {
                    reactions = adverseReactionRepository.findUnresolvedReactions();
                }
            } else {
                reactions = adverseReactionRepository.findAll();
            }
            
            List<Map<String, Object>> reactionDTOs = reactions.stream()
                    .map(reaction -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("id", reaction.getId());
                        dto.put("reactionType", reaction.getReactionType());
                        dto.put("symptoms", reaction.getSymptoms());
                        dto.put("occurredAt", reaction.getOccurredAt());
                        dto.put("resolved", reaction.getResolved());
                        dto.put("notes", reaction.getNotes());
                        
                        if (reaction.getVaccinationRecord() != null) {
                            Map<String, Object> record = new HashMap<>();
                            record.put("id", reaction.getVaccinationRecord().getId());
                            record.put("certificateNumber", reaction.getVaccinationRecord().getCertificateNumber());
                            if (reaction.getVaccinationRecord().getVaccine() != null) {
                                record.put("vaccineName", reaction.getVaccinationRecord().getVaccine().getName());
                            }
                            if (reaction.getVaccinationRecord().getUser() != null) {
                                record.put("patientName", reaction.getVaccinationRecord().getUser().getFullName());
                            }
                            dto.put("vaccinationRecord", record);
                        }
                        
                        if (reaction.getHandledBy() != null) {
                            Map<String, Object> handler = new HashMap<>();
                            handler.put("id", reaction.getHandledBy().getId());
                            handler.put("fullName", reaction.getHandledBy().getFullName());
                            dto.put("handledBy", handler);
                        }
                        
                        return dto;
                    })
                    .sorted((a, b) -> {
                        // Sắp xếp: unresolved trước, sau đó theo thời gian giảm dần
                        Boolean aResolved = (Boolean) a.get("resolved");
                        Boolean bResolved = (Boolean) b.get("resolved");
                        if (!aResolved && bResolved) return -1;
                        if (aResolved && !bResolved) return 1;
                        return 0;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(reactionDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/nurse/vaccination-records
     * Lấy danh sách lịch sử tiêm (cho Nurse - xem tất cả bệnh nhân)
     */
    @GetMapping("/vaccination-records")
    public ResponseEntity<?> getVaccinationRecords(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long vaccineId,
            @RequestParam(required = false) Long centerId,
            @RequestParam(required = false) Long nurseId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Lấy tất cả vaccination records (sắp xếp theo ngày giờ tiêm giảm dần)
            List<VaccinationRecord> allRecords = vaccinationRecordRepository.findAll(
                Sort.by(Sort.Direction.DESC, "injectionDate", "injectionTime")
            );
            
            // Apply filters
            List<VaccinationRecord> filteredRecords = allRecords.stream()
                    .filter(vr -> {
                        if (startDate == null && endDate == null) return true;
                        LocalDate recordDate = vr.getInjectionDate();
                        if (startDate != null && recordDate.isBefore(LocalDate.parse(startDate))) return false;
                        if (endDate != null && recordDate.isAfter(LocalDate.parse(endDate))) return false;
                        return true;
                    })
                    .filter(vr -> vaccineId == null || (vr.getVaccine() != null && vr.getVaccine().getId().equals(vaccineId)))
                    .filter(vr -> centerId == null || (vr.getAppointment() != null && vr.getAppointment().getCenter() != null && 
                            vr.getAppointment().getCenter().getId().equals(centerId)))
                    .filter(vr -> nurseId == null || (vr.getNurse() != null && vr.getNurse().getId().equals(nurseId)))
                    .sorted((a, b) -> {
                        // Sắp xếp theo ngày giờ tiêm giảm dần
                        int dateCompare = b.getInjectionDate().compareTo(a.getInjectionDate());
                        if (dateCompare != 0) return dateCompare;
                        return b.getInjectionTime().compareTo(a.getInjectionTime());
                    })
                    .collect(Collectors.toList());
            
            // Convert to DTO để tránh circular reference
            List<Map<String, Object>> recordDTOs = filteredRecords.stream().map(record -> {
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
                
                // Patient info
                if (record.getUser() != null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("id", record.getUser().getId());
                    user.put("fullName", record.getUser().getFullName());
                    user.put("email", record.getUser().getEmail());
                    user.put("phoneNumber", record.getUser().getPhoneNumber());
                    user.put("dayOfBirth", record.getUser().getDayOfBirth());
                    user.put("gender", record.getUser().getGender());
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
                    dto.put("vaccineLot", lot);
                }
                
                // Nurse info
                if (record.getNurse() != null) {
                    Map<String, Object> nurse = new HashMap<>();
                    nurse.put("id", record.getNurse().getId());
                    nurse.put("fullName", record.getNurse().getFullName());
                    dto.put("nurse", nurse);
                }
                
                // Appointment info
                if (record.getAppointment() != null) {
                    Map<String, Object> appointment = new HashMap<>();
                    appointment.put("id", record.getAppointment().getId());
                    appointment.put("bookingCode", record.getAppointment().getBookingCode());
                    appointment.put("appointmentDate", record.getAppointment().getAppointmentDate());
                    appointment.put("appointmentTime", record.getAppointment().getAppointmentTime());
                    if (record.getAppointment().getCenter() != null) {
                        Map<String, Object> center = new HashMap<>();
                        center.put("id", record.getAppointment().getCenter().getId());
                        center.put("name", record.getAppointment().getCenter().getName());
                        appointment.put("center", center);
                    }
                    dto.put("appointment", appointment);
                }
                
                return dto;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", recordDTOs);
            response.put("count", recordDTOs.size());
            
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
    
    /**
     * GET /api/nurse/appointments/{id}/injection
     * Lấy thông tin appointment chi tiết cho trang tiêm vaccine
     */
    @GetMapping("/appointments/{id}/injection")
    public ResponseEntity<?> getAppointmentForInjection(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            // Kiểm tra center của y tá phải trùng với center của appointment
            ut.edu.vaccinationmanagementsystem.entity.StaffInfo staffInfo = staffInfoRepository.findByUser(currentUser).orElse(null);
            if (staffInfo != null && staffInfo.getCenter() != null) {
                Long userCenterId = staffInfo.getCenter().getId();
                if (appointment.getCenter() == null || !appointment.getCenter().getId().equals(userCenterId)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Bạn chỉ có thể tiêm cho bệnh nhân của trung tâm mình");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
                }
            }
            
            // Kiểm tra status phải là APPROVED hoặc INJECTING
            if (appointment.getStatus() != AppointmentStatus.APPROVED && 
                appointment.getStatus() != AppointmentStatus.INJECTING) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Appointment status must be APPROVED or INJECTING");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Map<String, Object> result = new HashMap<>();
            
            // Thông tin appointment
            result.put("id", appointment.getId());
            result.put("bookingCode", appointment.getBookingCode());
            result.put("appointmentDate", appointment.getAppointmentDate());
            result.put("appointmentTime", appointment.getAppointmentTime());
            result.put("status", appointment.getStatus().name());
            result.put("doseNumber", appointment.getDoseNumber());
            
            // Thông tin bệnh nhân
            Map<String, Object> patientInfo = new HashMap<>();
            if (appointment.getBookedForUser() != null) {
                User user = appointment.getBookedForUser();
                patientInfo.put("id", user.getId());
                patientInfo.put("fullName", user.getFullName());
                patientInfo.put("email", user.getEmail());
                patientInfo.put("phoneNumber", user.getPhoneNumber());
                patientInfo.put("dateOfBirth", user.getDayOfBirth());
                patientInfo.put("gender", user.getGender() != null ? user.getGender().name() : null);
                patientInfo.put("citizenId", user.getCitizenId());
                patientInfo.put("address", user.getAddress());
            } else if (appointment.getFamilyMember() != null) {
                FamilyMember member = appointment.getFamilyMember();
                patientInfo.put("id", member.getId());
                patientInfo.put("fullName", member.getFullName());
                patientInfo.put("phoneNumber", member.getPhoneNumber());
                patientInfo.put("dateOfBirth", member.getDateOfBirth());
                patientInfo.put("gender", member.getGender() != null ? member.getGender().name() : null);
                patientInfo.put("relationship", member.getRelationship() != null ? member.getRelationship().name() : null);
                patientInfo.put("isFamilyMember", true);
                if (appointment.getBookedByUser() != null) {
                    patientInfo.put("bookedByUserId", appointment.getBookedByUser().getId());
                    patientInfo.put("bookedByUserName", appointment.getBookedByUser().getFullName());
                }
            } else {
                patientInfo.put("fullName", appointment.getGuestFullName());
                patientInfo.put("phoneNumber", appointment.getConsultationPhone());
            }
            result.put("patientInfo", patientInfo);
            
            // Thông tin vaccine
            if (appointment.getVaccine() != null) {
                Map<String, Object> vaccineInfo = new HashMap<>();
                vaccineInfo.put("id", appointment.getVaccine().getId());
                vaccineInfo.put("name", appointment.getVaccine().getName());
                vaccineInfo.put("manufacturer", appointment.getVaccine().getManufacturer());
                vaccineInfo.put("dosesRequired", appointment.getVaccine().getDosesRequired());
                vaccineInfo.put("daysBetweenDoses", appointment.getVaccine().getDaysBetweenDoses());
                result.put("vaccineInfo", vaccineInfo);
            }
            
            // Thông tin trung tâm
            if (appointment.getCenter() != null) {
                Map<String, Object> centerInfo = new HashMap<>();
                centerInfo.put("id", appointment.getCenter().getId());
                centerInfo.put("name", appointment.getCenter().getName());
                centerInfo.put("address", appointment.getCenter().getAddress());
                result.put("centerInfo", centerInfo);
            }
            
            // Thông tin screening (nếu có)
            Optional<Screening> screeningOpt = screeningRepository.findByAppointmentId(id);
            if (screeningOpt.isPresent()) {
                Screening screening = screeningOpt.get();
                Map<String, Object> screeningInfo = new HashMap<>();
                screeningInfo.put("id", screening.getId());
                screeningInfo.put("temperature", screening   .getBodyTemperature());
                screeningInfo.put("bloodPressure", screening.getBloodPressure());
                screeningInfo.put("heartRate", screening.getHeartRate());
                screeningInfo.put("screeningResult", screening.getScreeningResult() != null ? screening.getScreeningResult().name() : null);
                screeningInfo.put("rejectionReason", screening.getRejectionReason());
                screeningInfo.put("notes", screening.getNotes());
                screeningInfo.put("screenedAt", screening.getScreenedAt());
                if (screening.getDoctor() != null) {
                    screeningInfo.put("doctorName", screening.getDoctor().getFullName());
                }
                result.put("screening", screeningInfo);
            }
            
            // Thông tin vaccine lots có sẵn
            if (appointment.getVaccine() != null) {
                // Lấy tất cả vaccine lots của vaccine này (không filter expiry date ở đây để nurse vẫn chọn được trong khi dev/test)
                List<VaccineLot> allLots = vaccineLotRepository.findByVaccineId(appointment.getVaccine().getId());
                LocalDate today = LocalDate.now();
                
                List<Map<String, Object>> lotsInfo = allLots.stream()
                    .filter(lot -> lot.getStatus() == ut.edu.vaccinationmanagementsystem.entity.enums.VaccineLotStatus.AVAILABLE)
                    .map(lot -> {
                        Map<String, Object> lotInfo = new HashMap<>();
                        lotInfo.put("id", lot.getId());
                        lotInfo.put("lotNumber", lot.getLotNumber());
                        lotInfo.put("remainingQuantity", lot.getRemainingQuantity() != null ? lot.getRemainingQuantity() : 0);
                        lotInfo.put("expiryDate", lot.getExpiryDate());
                        lotInfo.put("isExpired", lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(today));
                        return lotInfo;
                    })
                    .collect(Collectors.toList());
                result.put("availableVaccineLots", lotsInfo);
            } else {
                result.put("availableVaccineLots", new ArrayList<>());
            }
            
            return ResponseEntity.ok(result);
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

