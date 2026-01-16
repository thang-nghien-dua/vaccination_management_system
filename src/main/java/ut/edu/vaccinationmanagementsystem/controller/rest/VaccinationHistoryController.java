package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationRecordRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vaccination-history")
public class VaccinationHistoryController {

    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/vaccination-history/statistics
     * Lấy thống kê tổng quan về hồ sơ tiêm chủng
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            // Lấy tất cả vaccination records của user
            List<VaccinationRecord> records = vaccinationRecordRepository.findByUserIdOrderByInjectionDateDesc(currentUser.getId());

            // Lấy appointments sắp tới (PENDING, CONFIRMED)
            // Lấy tất cả appointments của user (cả bookedForUser và bookedByUser)
            List<Appointment> allUserAppointments = new ArrayList<>();
            allUserAppointments.addAll(appointmentRepository.findByBookedForUserId(currentUser.getId()));
            List<Appointment> bookedByUser = appointmentRepository.findByBookedByUserId(currentUser.getId());
            bookedByUser.stream()
                    .filter(apt -> apt.getBookedForUser() == null)
                    .forEach(allUserAppointments::add);

            List<Appointment> upcomingAppointments = allUserAppointments.stream()
                    .filter(apt -> apt.getStatus() == AppointmentStatus.PENDING || apt.getStatus() == AppointmentStatus.CONFIRMED)
                    .filter(apt -> apt.getAppointmentDate() != null && apt.getAppointmentDate().isAfter(LocalDate.now().minusDays(1)))
                    .sorted(Comparator.comparing(Appointment::getAppointmentDate)
                            .thenComparing(Appointment::getAppointmentTime))
                    .collect(Collectors.toList());

            // Tính toán thống kê
            long totalInjections = records.size();
            long uniqueVaccines = records.stream()
                    .map(vr -> vr.getVaccine().getId())
                    .distinct()
                    .count();
            long upcomingCount = upcomingAppointments.size();

            // Tìm vaccine cần tiêm mũi tiếp theo
            Map<String, Object> nextDoseInfo = calculateNextDoseInfo(records);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalInjections", totalInjections);
            statistics.put("uniqueVaccines", uniqueVaccines);
            statistics.put("upcomingAppointments", upcomingCount);
            statistics.put("nextDoseInfo", nextDoseInfo);

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/vaccination-history/records
     * Lấy danh sách lịch sử tiêm chủng với filter
     */
    @GetMapping("/records")
    public ResponseEntity<?> getRecords(
            @RequestParam(required = false) Long vaccineId,
            @RequestParam(required = false) Long centerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            // Lấy tất cả records của user
            List<VaccinationRecord> allRecords = vaccinationRecordRepository.findByUserIdOrderByInjectionDateDesc(currentUser.getId());

            // Apply filters
            List<VaccinationRecord> filteredRecords = allRecords.stream()
                    .filter(vr -> vaccineId == null || vr.getVaccine().getId().equals(vaccineId))
                    .filter(vr -> centerId == null || (vr.getAppointment().getCenter() != null && 
                            vr.getAppointment().getCenter().getId().equals(centerId)))
                    .filter(vr -> {
                        if (startDate == null && endDate == null) return true;
                        LocalDate recordDate = vr.getInjectionDate();
                        if (startDate != null && recordDate.isBefore(LocalDate.parse(startDate))) return false;
                        if (endDate != null && recordDate.isAfter(LocalDate.parse(endDate))) return false;
                        return true;
                    })
                    .collect(Collectors.toList());

            // Convert to DTO
            List<Map<String, Object>> recordsList = filteredRecords.stream().map(record -> {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("vaccineName", record.getVaccine().getName());
                recordMap.put("vaccineId", record.getVaccine().getId());
                recordMap.put("injectionDate", record.getInjectionDate());
                recordMap.put("injectionTime", record.getInjectionTime());
                recordMap.put("doseNumber", record.getDoseNumber());
                recordMap.put("centerName", record.getAppointment().getCenter() != null ? 
                        record.getAppointment().getCenter().getName() : "N/A");
                recordMap.put("centerId", record.getAppointment().getCenter() != null ? 
                        record.getAppointment().getCenter().getId() : null);
                recordMap.put("batchNumber", record.getBatchNumber());
                recordMap.put("nurseName", record.getNurse() != null ? record.getNurse().getFullName() : "N/A");
                recordMap.put("certificateNumber", record.getCertificateNumber());
                recordMap.put("nextDoseDate", record.getNextDoseDate());
                return recordMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(recordsList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/vaccination-history/upcoming
     * Lấy danh sách lịch hẹn sắp tới
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingAppointments() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            // Lấy tất cả appointments của user (cả bookedForUser và bookedByUser)
            List<Appointment> allUserAppointments = new ArrayList<>();
            // Appointments đặt cho user (bookedForUser = currentUser)
            allUserAppointments.addAll(appointmentRepository.findByBookedForUserId(currentUser.getId()));
            // Appointments user đặt cho chính mình (bookedByUser = currentUser và bookedForUser = null)
            List<Appointment> bookedByUser = appointmentRepository.findByBookedByUserId(currentUser.getId());
            bookedByUser.stream()
                    .filter(apt -> apt.getBookedForUser() == null)
                    .forEach(allUserAppointments::add);

            List<Appointment> appointments = allUserAppointments.stream()
                    .filter(apt -> apt.getStatus() == AppointmentStatus.PENDING || apt.getStatus() == AppointmentStatus.CONFIRMED)
                    .filter(apt -> apt.getAppointmentDate() != null && apt.getAppointmentDate().isAfter(LocalDate.now().minusDays(1)))
                    .sorted(Comparator.comparing(Appointment::getAppointmentDate)
                            .thenComparing(Appointment::getAppointmentTime))
                    .collect(Collectors.toList());

            List<Map<String, Object>> appointmentsList = appointments.stream().map(apt -> {
                Map<String, Object> aptMap = new HashMap<>();
                aptMap.put("id", apt.getId());
                aptMap.put("bookingCode", apt.getBookingCode());
                aptMap.put("vaccineName", apt.getVaccine() != null ? apt.getVaccine().getName() : "N/A");
                aptMap.put("vaccineId", apt.getVaccine() != null ? apt.getVaccine().getId() : null);
                aptMap.put("appointmentDate", apt.getAppointmentDate());
                aptMap.put("appointmentTime", apt.getAppointmentTime());
                aptMap.put("centerName", apt.getCenter() != null ? apt.getCenter().getName() : "N/A");
                aptMap.put("centerId", apt.getCenter() != null ? apt.getCenter().getId() : null);
                aptMap.put("roomNumber", apt.getRoom() != null ? apt.getRoom().getRoomNumber() : null);
                aptMap.put("status", apt.getStatus().toString());
                aptMap.put("doseNumber", apt.getDoseNumber());
                return aptMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(appointmentsList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/vaccination-history/appointments
     * Lấy tất cả lịch hẹn (bao gồm cả đã hoàn thành, đã hủy)
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getAllAppointments() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            // Lấy tất cả appointments của user (cả bookedForUser và bookedByUser)
            List<Appointment> allAppointments = new ArrayList<>();
            allAppointments.addAll(appointmentRepository.findByBookedForUserId(currentUser.getId()));
            // Lấy appointments mà user đặt cho người khác (bookedByUser nhưng bookedForUser là null)
            List<Appointment> bookedByUser = appointmentRepository.findByBookedByUserId(currentUser.getId());
            bookedByUser.stream()
                    .filter(apt -> apt.getBookedForUser() == null)
                    .forEach(allAppointments::add);

            // Sort by date descending
            allAppointments.sort(Comparator.comparing(Appointment::getAppointmentDate, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Appointment::getAppointmentTime, Comparator.nullsLast(Comparator.reverseOrder())));

            List<Map<String, Object>> appointmentsList = allAppointments.stream().map(apt -> {
                Map<String, Object> aptMap = new HashMap<>();
                aptMap.put("id", apt.getId());
                aptMap.put("bookingCode", apt.getBookingCode());
                aptMap.put("vaccineName", apt.getVaccine() != null ? apt.getVaccine().getName() : "N/A");
                aptMap.put("vaccineId", apt.getVaccine() != null ? apt.getVaccine().getId() : null);
                aptMap.put("appointmentDate", apt.getAppointmentDate());
                aptMap.put("appointmentTime", apt.getAppointmentTime());
                aptMap.put("centerName", apt.getCenter() != null ? apt.getCenter().getName() : "N/A");
                aptMap.put("centerId", apt.getCenter() != null ? apt.getCenter().getId() : null);
                aptMap.put("status", apt.getStatus().toString());
                aptMap.put("doseNumber", apt.getDoseNumber());
                aptMap.put("notes", apt.getNotes());
                aptMap.put("createdAt", apt.getCreatedAt());
                return aptMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(appointmentsList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/vaccination-history/appointments/{id}
     * Lấy chi tiết một appointment
     */
    @GetMapping("/appointments/{id}")
    public ResponseEntity<?> getAppointmentDetail(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            Appointment appointment = appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            // Check authorization - user phải là người đặt hoặc người được đặt cho
            boolean isAuthorized = (appointment.getBookedForUser() != null && appointment.getBookedForUser().getId().equals(currentUser.getId())) ||
                    (appointment.getBookedForUser() == null && appointment.getBookedByUser() != null && appointment.getBookedByUser().getId().equals(currentUser.getId())) ||
                    (appointment.getBookedByUser() != null && appointment.getBookedByUser().getId().equals(currentUser.getId()));

            if (!isAuthorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
            }

            Map<String, Object> aptMap = new HashMap<>();
            aptMap.put("id", appointment.getId());
            aptMap.put("bookingCode", appointment.getBookingCode());
            aptMap.put("vaccineName", appointment.getVaccine() != null ? appointment.getVaccine().getName() : "N/A");
            aptMap.put("vaccineId", appointment.getVaccine() != null ? appointment.getVaccine().getId() : null);
            aptMap.put("appointmentDate", appointment.getAppointmentDate());
            aptMap.put("appointmentTime", appointment.getAppointmentTime());
            aptMap.put("centerName", appointment.getCenter() != null ? appointment.getCenter().getName() : "N/A");
            aptMap.put("centerAddress", appointment.getCenter() != null ? appointment.getCenter().getAddress() : "N/A");
            aptMap.put("centerPhone", appointment.getCenter() != null ? appointment.getCenter().getPhoneNumber() : "N/A");
            aptMap.put("centerId", appointment.getCenter() != null ? appointment.getCenter().getId() : null);
            aptMap.put("roomNumber", appointment.getRoom() != null ? appointment.getRoom().getRoomNumber() : null);
            aptMap.put("status", appointment.getStatus().toString());
            aptMap.put("doseNumber", appointment.getDoseNumber());
            aptMap.put("notes", appointment.getNotes());
            aptMap.put("createdAt", appointment.getCreatedAt());
            aptMap.put("updatedAt", appointment.getUpdatedAt());
            aptMap.put("requiresConsultation", appointment.getRequiresConsultation());
            
            // Thêm thông tin thanh toán
            if (appointment.getPayment() != null) {
                aptMap.put("paymentMethod", appointment.getPayment().getPaymentMethod() != null ? 
                        appointment.getPayment().getPaymentMethod().toString() : null);
                aptMap.put("paymentStatus", appointment.getPayment().getPaymentStatus() != null ? 
                        appointment.getPayment().getPaymentStatus().toString() : null);
                aptMap.put("amount", appointment.getPayment().getAmount());
            } else {
                aptMap.put("paymentMethod", null);
                aptMap.put("paymentStatus", null);
                aptMap.put("amount", null);
            }

            return ResponseEntity.ok(aptMap);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/vaccination-history/records/{id}
     * Lấy chi tiết một vaccination record
     */
    @GetMapping("/records/{id}")
    public ResponseEntity<?> getRecordDetail(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            VaccinationRecord record = vaccinationRecordRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Vaccination record not found"));

            // Check authorization
            if (!record.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
            }

            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("vaccineName", record.getVaccine().getName());
            recordMap.put("vaccineId", record.getVaccine().getId());
            recordMap.put("injectionDate", record.getInjectionDate());
            recordMap.put("injectionTime", record.getInjectionTime());
            recordMap.put("doseNumber", record.getDoseNumber());
            recordMap.put("centerName", record.getAppointment().getCenter() != null ? 
                    record.getAppointment().getCenter().getName() : "N/A");
            recordMap.put("centerAddress", record.getAppointment().getCenter() != null ? 
                    record.getAppointment().getCenter().getAddress() : "N/A");
            recordMap.put("batchNumber", record.getBatchNumber());
            recordMap.put("nurseName", record.getNurse() != null ? record.getNurse().getFullName() : "N/A");
            recordMap.put("nurseId", record.getNurse() != null ? record.getNurse().getId() : null);
            recordMap.put("injectionSite", record.getInjectionSite());
            recordMap.put("doseAmount", record.getDoseAmount());
            recordMap.put("certificateNumber", record.getCertificateNumber());
            recordMap.put("nextDoseDate", record.getNextDoseDate());
            recordMap.put("bookingCode", record.getAppointment().getBookingCode());

            return ResponseEntity.ok(recordMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Helper method để tính toán thông tin mũi tiêm tiếp theo
     */
    private Map<String, Object> calculateNextDoseInfo(List<VaccinationRecord> records) {
        Map<String, Object> nextDoseInfo = new HashMap<>();
        
        // Tìm vaccine có nextDoseDate sớm nhất trong tương lai
        Optional<VaccinationRecord> nextDose = records.stream()
                .filter(vr -> vr.getNextDoseDate() != null)
                .filter(vr -> vr.getNextDoseDate().isAfter(LocalDate.now()))
                .min(Comparator.comparing(VaccinationRecord::getNextDoseDate));

        if (nextDose.isPresent()) {
            VaccinationRecord record = nextDose.get();
            nextDoseInfo.put("hasNextDose", true);
            nextDoseInfo.put("vaccineName", record.getVaccine().getName());
            nextDoseInfo.put("vaccineId", record.getVaccine().getId());
            nextDoseInfo.put("nextDoseDate", record.getNextDoseDate());
            nextDoseInfo.put("nextDoseNumber", record.getDoseNumber() + 1);
        } else {
            nextDoseInfo.put("hasNextDose", false);
        }

        return nextDoseInfo;
    }

    /**
     * Helper method to get current user
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
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            return null;
        }
    }
}

