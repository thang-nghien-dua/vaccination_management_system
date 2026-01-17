package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.repository.*;
import ut.edu.vaccinationmanagementsystem.service.AdverseReactionService;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trace")
public class TraceController {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdverseReactionService adverseReactionService;
    
    @Autowired
    private AppointmentHistoryRepository appointmentHistoryRepository;
    
    @Autowired
    private ScreeningRepository screeningRepository;
    
    /**
     * GET /api/trace
     * Truy vết thông tin theo booking code, certificate number, phone, hoặc email
     * Query params: bookingCode, certificateNumber, phone, email (ít nhất một trong các tham số)
     */
    @GetMapping
    public ResponseEntity<?> trace(
            @RequestParam(required = false) String bookingCode,
            @RequestParam(required = false) String certificateNumber,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Kiểm tra ít nhất một tham số được cung cấp
            if ((bookingCode == null || bookingCode.trim().isEmpty()) &&
                (certificateNumber == null || certificateNumber.trim().isEmpty()) &&
                (phone == null || phone.trim().isEmpty()) &&
                (email == null || email.trim().isEmpty())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Phải cung cấp ít nhất một trong các tham số: bookingCode, certificateNumber, phone, email");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> traceResults = new ArrayList<>();
            
            // Tìm theo certificate number (ưu tiên cao nhất)
            if (certificateNumber != null && !certificateNumber.trim().isEmpty()) {
                Optional<VaccinationRecord> recordOpt = vaccinationRecordRepository.findByCertificateNumber(certificateNumber.trim());
                if (recordOpt.isPresent()) {
                    traceResults.add(buildTraceResult(recordOpt.get()));
                }
            }
            
            // Tìm theo booking code
            if (bookingCode != null && !bookingCode.trim().isEmpty()) {
                Optional<Appointment> appointmentOpt = appointmentRepository.findByBookingCode(bookingCode.trim());
                if (appointmentOpt.isPresent()) {
                    Appointment appointment = appointmentOpt.get();
                    
                    // Nếu có vaccination record
                    if (appointment.getVaccinationRecord() != null) {
                        VaccinationRecord record = appointment.getVaccinationRecord();
                        Map<String, Object> traceResult = buildTraceResult(record);
                        traceResults.add(traceResult);
                    } else {
                        // Chỉ có appointment, chưa tiêm
                        Map<String, Object> traceResult = buildAppointmentTraceResult(appointment);
                        traceResults.add(traceResult);
                    }
                }
            }
            
            // Tìm theo phone hoặc email
            if (phone != null && !phone.trim().isEmpty() || email != null && !email.trim().isEmpty()) {
                List<User> users = new ArrayList<>();
                
                if (phone != null && !phone.trim().isEmpty()) {
                    users.addAll(userRepository.findAll().stream()
                        .filter(u -> u.getPhoneNumber() != null && 
                                u.getPhoneNumber().contains(phone.trim()))
                        .collect(Collectors.toList()));
                }
                
                if (email != null && !email.trim().isEmpty()) {
                    Optional<User> userOpt = userRepository.findByEmail(email.trim());
                    if (userOpt.isPresent() && !users.contains(userOpt.get())) {
                        users.add(userOpt.get());
                    }
                }
                
                // Tìm tất cả vaccination records của các users này
                for (User user : users) {
                    List<VaccinationRecord> records = vaccinationRecordRepository.findByUserIdOrderByInjectionDateDesc(user.getId());
                    for (VaccinationRecord record : records) {
                        // Kiểm tra không trùng với kết quả đã có
                        boolean exists = traceResults.stream()
                            .anyMatch(tr -> tr.get("recordId") != null && 
                                    tr.get("recordId").equals(record.getId()));
                        if (!exists) {
                            traceResults.add(buildTraceResult(record));
                        }
                    }
                    
                    // Tìm appointments chưa tiêm
                    List<Appointment> appointments = appointmentRepository.findByBookedForUserId(user.getId());
                    appointments.addAll(appointmentRepository.findByBookedByUserId(user.getId()));
                    for (Appointment appointment : appointments) {
                        if (appointment.getVaccinationRecord() == null) {
                            boolean exists = traceResults.stream()
                                .anyMatch(tr -> tr.get("appointmentId") != null && 
                                        tr.get("appointmentId").equals(appointment.getId()));
                            if (!exists) {
                                traceResults.add(buildAppointmentTraceResult(appointment));
                            }
                        }
                    }
                }
            }
            
            result.put("results", traceResults);
            result.put("count", traceResults.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Xây dựng kết quả truy vết từ VaccinationRecord
     */
    private Map<String, Object> buildTraceResult(VaccinationRecord record) {
        Map<String, Object> result = new HashMap<>();
        
        // Thông tin cơ bản
        result.put("type", "VACCINATION_RECORD");
        result.put("recordId", record.getId());
        result.put("certificateNumber", record.getCertificateNumber());
        result.put("injectionDate", record.getInjectionDate());
        result.put("injectionTime", record.getInjectionTime());
        result.put("injectionSite", record.getInjectionSite());
        result.put("doseNumber", record.getDoseNumber());
        result.put("doseAmount", record.getDoseAmount());
        result.put("batchNumber", record.getBatchNumber());
        result.put("nextDoseDate", record.getNextDoseDate());
        
        // Thông tin bệnh nhân
        if (record.getUser() != null) {
            Map<String, Object> patient = new HashMap<>();
            patient.put("id", record.getUser().getId());
            patient.put("fullName", record.getUser().getFullName());
            patient.put("email", record.getUser().getEmail());
            patient.put("phoneNumber", record.getUser().getPhoneNumber());
            patient.put("dayOfBirth", record.getUser().getDayOfBirth());
            patient.put("gender", record.getUser().getGender());
            patient.put("address", record.getUser().getAddress());
            patient.put("citizenId", record.getUser().getCitizenId());
            result.put("patient", patient);
        }
        
        // Thông tin y tá thực hiện
        if (record.getNurse() != null) {
            Map<String, Object> nurse = new HashMap<>();
            nurse.put("id", record.getNurse().getId());
            nurse.put("fullName", record.getNurse().getFullName());
            nurse.put("email", record.getNurse().getEmail());
            nurse.put("phoneNumber", record.getNurse().getPhoneNumber());
            result.put("nurse", nurse);
        }
        
        // Thông tin vaccine
        if (record.getVaccine() != null) {
            Map<String, Object> vaccine = new HashMap<>();
            vaccine.put("id", record.getVaccine().getId());
            vaccine.put("name", record.getVaccine().getName());
            vaccine.put("manufacturer", record.getVaccine().getManufacturer());
            vaccine.put("code", record.getVaccine().getCode());
            result.put("vaccine", vaccine);
        }
        
        // Thông tin lô vaccine
        if (record.getVaccineLot() != null) {
            Map<String, Object> lot = new HashMap<>();
            lot.put("id", record.getVaccineLot().getId());
            lot.put("lotNumber", record.getVaccineLot().getLotNumber());
            lot.put("manufacturingDate", record.getVaccineLot().getManufacturingDate());
            lot.put("expiryDate", record.getVaccineLot().getExpiryDate());
            result.put("vaccineLot", lot);
        }
        
        // Thông tin appointment
        if (record.getAppointment() != null) {
            Map<String, Object> appointment = new HashMap<>();
            appointment.put("id", record.getAppointment().getId());
            appointment.put("bookingCode", record.getAppointment().getBookingCode());
            appointment.put("appointmentDate", record.getAppointment().getAppointmentDate());
            appointment.put("appointmentTime", record.getAppointment().getAppointmentTime());
            appointment.put("status", record.getAppointment().getStatus() != null ? 
                    record.getAppointment().getStatus().name() : null);
            appointment.put("queueNumber", record.getAppointment().getQueueNumber());
            
            if (record.getAppointment().getCenter() != null) {
                Map<String, Object> center = new HashMap<>();
                center.put("id", record.getAppointment().getCenter().getId());
                center.put("name", record.getAppointment().getCenter().getName());
                center.put("address", record.getAppointment().getCenter().getAddress());
                appointment.put("center", center);
            }
            
            result.put("appointment", appointment);
            
            // Tìm thông tin doctor từ screening hoặc appointment history
            Map<String, Object> doctorInfo = findDoctorInfo(record.getAppointment().getId());
            if (doctorInfo != null && !doctorInfo.isEmpty()) {
                result.put("doctor", doctorInfo);
            }
        }
        
        // Thông tin phản ứng phụ
        try {
            List<AdverseReaction> reactions = adverseReactionService.getAdverseReactionsByVaccinationRecordId(record.getId());
            List<Map<String, Object>> reactionDTOs = reactions.stream().map(reaction -> {
                Map<String, Object> reactionDTO = new HashMap<>();
                reactionDTO.put("id", reaction.getId());
                reactionDTO.put("reactionType", reaction.getReactionType() != null ? reaction.getReactionType().name() : null);
                reactionDTO.put("symptoms", reaction.getSymptoms());
                reactionDTO.put("occurredAt", reaction.getOccurredAt());
                reactionDTO.put("resolved", reaction.getResolved());
                reactionDTO.put("notes", reaction.getNotes());
                reactionDTO.put("treatment", reaction.getTreatment());
                
                if (reaction.getHandledBy() != null) {
                    Map<String, Object> handler = new HashMap<>();
                    handler.put("id", reaction.getHandledBy().getId());
                    handler.put("fullName", reaction.getHandledBy().getFullName());
                    reactionDTO.put("handledBy", handler);
                }
                
                return reactionDTO;
            }).collect(Collectors.toList());
            result.put("adverseReactions", reactionDTOs);
        } catch (Exception e) {
            result.put("adverseReactions", new ArrayList<>());
        }
        
        return result;
    }
    
    /**
     * Xây dựng kết quả truy vết từ Appointment (chưa có vaccination record)
     */
    private Map<String, Object> buildAppointmentTraceResult(Appointment appointment) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("type", "APPOINTMENT");
        result.put("appointmentId", appointment.getId());
        result.put("bookingCode", appointment.getBookingCode());
        result.put("status", appointment.getStatus() != null ? appointment.getStatus().name() : null);
        result.put("appointmentDate", appointment.getAppointmentDate());
        result.put("appointmentTime", appointment.getAppointmentTime());
        result.put("queueNumber", appointment.getQueueNumber());
        result.put("doseNumber", appointment.getDoseNumber());
        result.put("notes", appointment.getNotes());
        result.put("vaccinated", false);
        
        // Thông tin bệnh nhân
        User patient = appointment.getBookedForUser();
        if (patient == null && appointment.getBookedByUser() != null) {
            patient = appointment.getBookedByUser();
        }
        
        if (patient != null) {
            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("id", patient.getId());
            patientInfo.put("fullName", patient.getFullName());
            patientInfo.put("email", patient.getEmail());
            patientInfo.put("phoneNumber", patient.getPhoneNumber());
            patientInfo.put("dayOfBirth", patient.getDayOfBirth());
            patientInfo.put("gender", patient.getGender());
            result.put("patient", patientInfo);
        } else {
            // Guest appointment
            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("fullName", appointment.getGuestFullName());
            patientInfo.put("email", appointment.getGuestEmail());
            patientInfo.put("phoneNumber", appointment.getConsultationPhone());
            patientInfo.put("dayOfBirth", appointment.getGuestDayOfBirth());
            patientInfo.put("gender", appointment.getGuestGender());
            result.put("patient", patientInfo);
            result.put("isGuest", true);
        }
        
        // Thông tin vaccine
        if (appointment.getVaccine() != null) {
            Map<String, Object> vaccine = new HashMap<>();
            vaccine.put("id", appointment.getVaccine().getId());
            vaccine.put("name", appointment.getVaccine().getName());
            vaccine.put("manufacturer", appointment.getVaccine().getManufacturer());
            result.put("vaccine", vaccine);
        }
        
        // Thông tin trung tâm
        if (appointment.getCenter() != null) {
            Map<String, Object> center = new HashMap<>();
            center.put("id", appointment.getCenter().getId());
            center.put("name", appointment.getCenter().getName());
            center.put("address", appointment.getCenter().getAddress());
            result.put("center", center);
        }
        
        // Tìm thông tin doctor từ screening hoặc appointment history
        Map<String, Object> doctorInfo = findDoctorInfo(appointment.getId());
        if (doctorInfo != null && !doctorInfo.isEmpty()) {
            result.put("doctor", doctorInfo);
        }
        
        return result;
    }
    
    /**
     * Tìm thông tin doctor từ screening hoặc appointment history
     * Nếu không có thì trả về null (không báo lỗi)
     */
    private Map<String, Object> findDoctorInfo(Long appointmentId) {
        Map<String, Object> doctorInfo = new HashMap<>();
        boolean found = false;
        
        try {
            // Tìm từ Screening (khám sàng lọc)
            Optional<Screening> screeningOpt = screeningRepository.findByAppointmentId(appointmentId);
            if (screeningOpt.isPresent()) {
                Screening screening = screeningOpt.get();
                if (screening.getDoctor() != null) {
                    doctorInfo.put("id", screening.getDoctor().getId());
                    doctorInfo.put("fullName", screening.getDoctor().getFullName());
                    doctorInfo.put("email", screening.getDoctor().getEmail());
                    doctorInfo.put("phoneNumber", screening.getDoctor().getPhoneNumber());
                    doctorInfo.put("role", "Khám sàng lọc");
                    doctorInfo.put("screenedAt", screening.getScreenedAt());
                    found = true;
                }
            }
        } catch (Exception e) {
            // Nếu chưa có ScreeningRepository hoặc table chưa tồn tại, bỏ qua
        }
        
        try {
            // Tìm từ AppointmentHistory (doctor phê duyệt)
            if (!found) {
                List<AppointmentHistory> histories = appointmentHistoryRepository.findAll().stream()
                    .filter(h -> h.getAppointment() != null && 
                            h.getAppointment().getId().equals(appointmentId) &&
                            h.getNewStatus() != null &&
                            h.getNewStatus().name().equals("APPROVED") &&
                            h.getChangedBy() != null &&
                            h.getChangedBy().getRole() != null &&
                            h.getChangedBy().getRole().name().equals("DOCTOR"))
                    .sorted((h1, h2) -> h2.getChangedAt().compareTo(h1.getChangedAt()))
                    .collect(Collectors.toList());
                
                if (!histories.isEmpty()) {
                    AppointmentHistory history = histories.get(0);
                    User doctor = history.getChangedBy();
                    doctorInfo.put("id", doctor.getId());
                    doctorInfo.put("fullName", doctor.getFullName());
                    doctorInfo.put("email", doctor.getEmail());
                    doctorInfo.put("phoneNumber", doctor.getPhoneNumber());
                    doctorInfo.put("role", "Phê duyệt");
                    doctorInfo.put("approvedAt", history.getChangedAt());
                    found = true;
                }
            }
        } catch (Exception e) {
            // Nếu có lỗi, bỏ qua
        }
        
        return found ? doctorInfo : null;
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
