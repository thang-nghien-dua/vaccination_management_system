package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ut.edu.vaccinationmanagementsystem.entity.Appointment;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.repository.AppointmentRepository;
import ut.edu.vaccinationmanagementsystem.repository.FamilyMemberRepository;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationRecordRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    /**
     * GET /api/dashboard/statistics
     * Lấy thống kê tổng quan cho dashboard
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

            // Lấy appointments sắp tới (chưa hoàn thành) - bao gồm cả appointments user đặt cho bản thân
            List<Appointment> allUserAppointments = new ArrayList<>();
            List<Appointment> bookedByUser = appointmentRepository.findByBookedByUserId(currentUser.getId());
            bookedByUser.stream()
                    .filter(apt -> apt.getBookedForUser() == null)
                    .forEach(allUserAppointments::add);
            allUserAppointments.addAll(appointmentRepository.findByBookedForUserId(currentUser.getId()));
            
            List<Appointment> upcomingAppointments = allUserAppointments.stream()
                    .filter(apt -> {
                        AppointmentStatus status = apt.getStatus();
                        return status == AppointmentStatus.PENDING ||
                               status == AppointmentStatus.CONFIRMED ||
                               status == AppointmentStatus.CHECKED_IN ||
                               status == AppointmentStatus.SCREENING ||
                               status == AppointmentStatus.APPROVED ||
                               status == AppointmentStatus.INJECTING ||
                               status == AppointmentStatus.MONITORING;
                    })
                    .filter(apt -> apt.getAppointmentDate() != null && 
                            (apt.getAppointmentDate().isAfter(LocalDate.now()) || 
                             apt.getAppointmentDate().equals(LocalDate.now())))
                    .sorted(Comparator.comparing(Appointment::getAppointmentDate)
                            .thenComparing(Appointment::getAppointmentTime))
                    .collect(Collectors.toList());

            // Tính toán thống kê
            long totalInjections = records.size();
            long familyMembersCount = familyMemberRepository.countByUser(currentUser);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalInjections", totalInjections);
            statistics.put("familyMembersCount", familyMembersCount);
            statistics.put("upcomingAppointmentsCount", upcomingAppointments.size());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/dashboard/upcoming
     * Lấy lịch hẹn sắp tới (1 appointment gần nhất)
     * Bao gồm: PENDING, CONFIRMED, CHECKED_IN, SCREENING, APPROVED (chưa hoàn thành)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingAppointment() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            // Lấy appointments sắp tới: bao gồm cả appointments user đặt cho bản thân và cho người thân
            List<Appointment> allUserAppointments = new ArrayList<>();
            // Appointments user đặt cho bản thân (bookedForUser = null và bookedByUser = currentUser)
            List<Appointment> bookedByUser = appointmentRepository.findByBookedByUserId(currentUser.getId());
            bookedByUser.stream()
                    .filter(apt -> apt.getBookedForUser() == null)
                    .forEach(allUserAppointments::add);
            // Appointments user đặt cho người thân (bookedByUser = currentUser và bookedForUser != null)
            bookedByUser.stream()
                    .filter(apt -> apt.getBookedForUser() != null)
                    .forEach(allUserAppointments::add);
            // Appointments đặt cho user (bookedForUser = currentUser)
            allUserAppointments.addAll(appointmentRepository.findByBookedForUserId(currentUser.getId()));
            
            List<Appointment> upcomingAppointments = allUserAppointments.stream()
                    .filter(apt -> {
                        AppointmentStatus status = apt.getStatus();
                        // Bao gồm các status chưa hoàn thành
                        return status == AppointmentStatus.PENDING ||
                               status == AppointmentStatus.CONFIRMED ||
                               status == AppointmentStatus.CHECKED_IN ||
                               status == AppointmentStatus.SCREENING ||
                               status == AppointmentStatus.APPROVED ||
                               status == AppointmentStatus.INJECTING ||
                               status == AppointmentStatus.MONITORING;
                    })
                    .filter(apt -> {
                        // Chấp nhận appointments có ngày >= hôm nay (tương lai hoặc hôm nay)
                        return apt.getAppointmentDate() != null && 
                            (apt.getAppointmentDate().isAfter(LocalDate.now()) || 
                             apt.getAppointmentDate().equals(LocalDate.now()));
                    })
                    .sorted(Comparator.comparing(Appointment::getAppointmentDate)
                            .thenComparing(Appointment::getAppointmentTime))
                    .collect(Collectors.toList());

            if (upcomingAppointments.isEmpty()) {
                return ResponseEntity.ok(Map.of("hasAppointment", false, "appointments", List.of()));
            }

            // Trả về danh sách appointments
            List<Map<String, Object>> appointmentsList = upcomingAppointments.stream().map(apt -> {
                Map<String, Object> appointmentData = new HashMap<>();
                appointmentData.put("id", apt.getId());
                appointmentData.put("bookingCode", apt.getBookingCode());
                appointmentData.put("qrCodeUrl", "/api/dashboard/qr-code/" + apt.getBookingCode());
                appointmentData.put("vaccineName", apt.getVaccine() != null ? apt.getVaccine().getName() : "N/A");
                appointmentData.put("appointmentDate", apt.getAppointmentDate());
                appointmentData.put("appointmentTime", apt.getAppointmentTime());
                appointmentData.put("centerName", apt.getCenter() != null ? apt.getCenter().getName() : "N/A");
                appointmentData.put("roomNumber", apt.getRoom() != null ? apt.getRoom().getRoomNumber() : null);
                appointmentData.put("status", apt.getStatus().toString());
                appointmentData.put("statusText", getStatusText(apt.getStatus()));
                appointmentData.put("doseNumber", apt.getDoseNumber());
                
                // Thông tin người được đặt cho (nếu đặt cho người thân)
                if (apt.getBookedForUser() != null && !apt.getBookedForUser().getId().equals(currentUser.getId())) {
                    appointmentData.put("forUser", apt.getBookedForUser().getFullName());
                } else if (apt.getFamilyMember() != null) {
                    appointmentData.put("forUser", apt.getFamilyMember().getFullName());
                }

                // Format date và time
                if (apt.getAppointmentDate() != null) {
                    appointmentData.put("formattedDate", apt.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
                if (apt.getAppointmentTime() != null) {
                    appointmentData.put("formattedTime", String.format("%02d:%02d", apt.getAppointmentTime().getHour(), apt.getAppointmentTime().getMinute()));
                }
                
                return appointmentData;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("hasAppointment", true, "appointments", appointmentsList));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/dashboard/recent-history
     * Lấy lịch sử tiêm chủng gần đây (3 records gần nhất)
     */
    @GetMapping("/recent-history")
    public ResponseEntity<?> getRecentHistory() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            List<VaccinationRecord> records = vaccinationRecordRepository.findByUserIdOrderByInjectionDateDesc(currentUser.getId())
                    .stream()
                    .limit(3)
                    .collect(Collectors.toList());

            List<Map<String, Object>> recordsList = records.stream().map(record -> {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("vaccineName", record.getVaccine() != null ? record.getVaccine().getName() : "N/A");
                recordMap.put("injectionDate", record.getInjectionDate());
                if (record.getInjectionDate() != null) {
                    recordMap.put("formattedDate", record.getInjectionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
                recordMap.put("doseNumber", record.getDoseNumber());
                recordMap.put("centerName", record.getAppointment() != null && record.getAppointment().getCenter() != null 
                    ? record.getAppointment().getCenter().getName() : "N/A");
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
     * GET /api/dashboard/health-reminder
     * Lấy lời nhắc sức khỏe (vaccine cần tiêm nhắc lại)
     */
    @GetMapping("/health-reminder")
    public ResponseEntity<?> getHealthReminder() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
            }

            // Tìm vaccine có nextDoseDate trong tháng tới
            List<VaccinationRecord> records = vaccinationRecordRepository.findByUserIdOrderByInjectionDateDesc(currentUser.getId());
            LocalDate nextMonth = LocalDate.now().plusMonths(1);

            Optional<VaccinationRecord> reminderRecord = records.stream()
                    .filter(record -> record.getNextDoseDate() != null)
                    .filter(record -> record.getNextDoseDate().isAfter(LocalDate.now()))
                    .filter(record -> record.getNextDoseDate().isBefore(nextMonth) || record.getNextDoseDate().isBefore(nextMonth.plusDays(1)))
                    .findFirst();

            if (reminderRecord.isEmpty()) {
                return ResponseEntity.ok(Map.of("hasReminder", false));
            }

            VaccinationRecord record = reminderRecord.get();
            Map<String, Object> reminder = new HashMap<>();
            reminder.put("hasReminder", true);
            reminder.put("vaccineName", record.getVaccine() != null ? record.getVaccine().getName() : "N/A");
            reminder.put("nextDoseDate", record.getNextDoseDate());
            if (record.getNextDoseDate() != null) {
                reminder.put("formattedDate", record.getNextDoseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            return ResponseEntity.ok(reminder);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/dashboard/qr-code/{bookingCode}
     * Tạo và trả về QR code image từ booking code
     */
    @GetMapping(value = "/qr-code/{bookingCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCode(@PathVariable String bookingCode) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Kiểm tra appointment có thuộc về user không
            Optional<Appointment> appointmentOpt = appointmentRepository.findByBookingCode(bookingCode);
            if (appointmentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            Appointment appointment = appointmentOpt.get();
            // Kiểm tra quyền truy cập
            boolean hasAccess = (appointment.getBookedByUser() != null && appointment.getBookedByUser().getId().equals(currentUser.getId())) ||
                               (appointment.getBookedForUser() != null && appointment.getBookedForUser().getId().equals(currentUser.getId()));
            
            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Tạo QR code
            int width = 200;
            int height = 200;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(bookingCode, BarcodeFormat.QR_CODE, width, height, hints);
            
            // Convert to image
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            
            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
                    
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Chuyển đổi status enum sang text tiếng Việt
     */
    private String getStatusText(AppointmentStatus status) {
        switch (status) {
            case PENDING:
                return "Chờ xác nhận";
            case CONFIRMED:
                return "Đã xác nhận";
            case CHECKED_IN:
                return "Đã check-in";
            case SCREENING:
                return "Đang khám sàng lọc";
            case APPROVED:
                return "Đủ điều kiện tiêm";
            case REJECTED:
                return "Không đủ điều kiện";
            case INJECTING:
                return "Đang tiêm";
            case MONITORING:
                return "Đang theo dõi";
            case COMPLETED:
                return "Hoàn thành";
            case CANCELLED:
                return "Đã hủy";
            case RESCHEDULED:
                return "Đã đổi lịch";
            default:
                return status.toString();
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

