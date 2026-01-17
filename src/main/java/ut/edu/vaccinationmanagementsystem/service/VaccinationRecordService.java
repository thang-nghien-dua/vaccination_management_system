package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.repository.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Service xử lý logic tạo VaccinationRecord và quản lý stock
 */
@Service
@Transactional
public class VaccinationRecordService {
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private CenterVaccineRepository centerVaccineRepository;
    
    @Autowired
    private VaccineLotRepository vaccineLotRepository;
    
    /**
     * Tạo VaccinationRecord khi tiêm thành công
     * Đồng thời trừ stock quantity của vaccine tại trung tâm
     * 
     * @param appointmentId ID của appointment đã tiêm thành công
     * @param vaccineLotId ID của vaccine lot đã sử dụng
     * @param nurseId ID của y tá thực hiện tiêm
     * @param injectionDate Ngày tiêm
     * @param injectionTime Giờ tiêm
     * @param injectionSite Vị trí tiêm (ví dụ: "LEFT_ARM", "RIGHT_ARM")
     * @param doseAmount Liều lượng (ví dụ: 0.5ml)
     * @param certificateNumber Số chứng nhận tiêm chủng
     * @return VaccinationRecord đã được tạo
     */
    public VaccinationRecord createVaccinationRecord(
            Long appointmentId,
            Long vaccineLotId,
            Long nurseId,
            LocalDate injectionDate,
            LocalTime injectionTime,
            String injectionSite,
            Double doseAmount,
            String certificateNumber) {
        
        // Lấy appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Kiểm tra appointment đã có vaccination record chưa
        if (appointment.getVaccinationRecord() != null) {
            throw new RuntimeException("Appointment already has a vaccination record");
        }
        
        // Kiểm tra appointment có vaccine không
        if (appointment.getVaccine() == null) {
            throw new RuntimeException("Appointment does not have a vaccine");
        }
        
        // Kiểm tra appointment có center không
        if (appointment.getCenter() == null) {
            throw new RuntimeException("Appointment does not have a center");
        }
        
        // Lấy vaccine lot
        VaccineLot vaccineLot = vaccineLotRepository.findById(vaccineLotId)
                .orElseThrow(() -> new RuntimeException("Vaccine lot not found"));
        
        // Kiểm tra vaccine lot có thuộc vaccine của appointment không
        if (!vaccineLot.getVaccine().getId().equals(appointment.getVaccine().getId())) {
            throw new RuntimeException("Vaccine lot does not match appointment vaccine");
        }
        
        // Kiểm tra vaccine lot còn vaccine không
        if (vaccineLot.getRemainingQuantity() == null || vaccineLot.getRemainingQuantity() <= 0) {
            throw new RuntimeException("Vaccine lot is out of stock");
        }
        
        // Lấy nurse
        User nurse = userRepository.findById(nurseId)
                .orElseThrow(() -> new RuntimeException("Nurse not found"));
        
        // TODO: Kiểm tra nurse có role NURSE không (nếu có enum Role)
        
        // Xác định user được tiêm
        // Lưu ý: Khi đặt cho family member, user field trong VaccinationRecord sẽ lưu bookedByUser
        // Thông tin chi tiết người được tiêm sẽ lấy từ appointment.familyMember
        User userToInject;
        if (appointment.getFamilyMember() != null) {
            // Đặt cho người thân - lưu bookedByUser vào user field
            // Thông tin người được tiêm sẽ lấy từ appointment.familyMember
            if (appointment.getBookedByUser() == null) {
                throw new RuntimeException("Appointment for family member must have bookedByUser");
            }
            userToInject = appointment.getBookedByUser();
        } else if (appointment.getBookedForUser() != null) {
            // Đặt cho user khác (nếu có logic này)
            userToInject = appointment.getBookedForUser();
        } else if (appointment.getBookedByUser() != null) {
            // Đặt cho bản thân (user đã đăng nhập)
            userToInject = appointment.getBookedByUser();
        } else {
            // Guest appointment (walk-in) - không có bookedByUser
            // Tìm hoặc tạo User từ thông tin guest
            String guestEmail = appointment.getGuestEmail();
            if (guestEmail == null || guestEmail.trim().isEmpty()) {
                // Nếu không có email, tạo email tạm thời từ phone number hoặc booking code
                String phone = appointment.getConsultationPhone();
                String bookingCode = appointment.getBookingCode();
                if (phone != null && !phone.trim().isEmpty()) {
                    // Tạo email từ phone: guest_{phone}@walkin.local
                    guestEmail = "guest_" + phone.replaceAll("[^0-9]", "") + "@walkin.local";
                } else if (bookingCode != null && !bookingCode.trim().isEmpty()) {
                    // Tạo email từ booking code: guest_{bookingCode}@walkin.local
                    guestEmail = "guest_" + bookingCode + "@walkin.local";
                } else {
                    // Fallback: sử dụng appointment ID
                    guestEmail = "guest_appointment_" + appointment.getId() + "@walkin.local";
                }
            }
            
            // Tìm User theo guestEmail
            Optional<User> existingUserOpt = userRepository.findByEmail(guestEmail.trim().toLowerCase());
            if (existingUserOpt.isPresent()) {
                userToInject = existingUserOpt.get();
            } else {
                // Tạo User mới từ thông tin guest
                User guestUser = new User();
                guestUser.setEmail(guestEmail.trim().toLowerCase());
                guestUser.setFullName(appointment.getGuestFullName() != null ? appointment.getGuestFullName() : "Guest User");
                guestUser.setPhoneNumber(appointment.getConsultationPhone());
                guestUser.setDayOfBirth(appointment.getGuestDayOfBirth());
                guestUser.setGender(appointment.getGuestGender());
                guestUser.setRole(ut.edu.vaccinationmanagementsystem.entity.enums.Role.CUSTOMER);
                guestUser.setStatus(ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus.ACTIVE);
                guestUser.setAuthProvider(ut.edu.vaccinationmanagementsystem.entity.enums.AuthProvider.EMAIL);
                guestUser.setCreateAt(LocalDate.now());
                // Set password tạm thời (guest user không cần login, nhưng cần password để không null)
                guestUser.setPassword("GUEST_USER_NO_PASSWORD");
                
                userToInject = userRepository.save(guestUser);
                System.out.println("Created guest User from appointment: " + userToInject.getEmail());
            }
        }
        
        // Lấy CenterVaccine để trừ stock
        Optional<CenterVaccine> centerVaccineOpt = centerVaccineRepository.findByCenterAndVaccine(
                appointment.getCenter(),
                appointment.getVaccine()
        );
        
        if (centerVaccineOpt.isEmpty()) {
            throw new RuntimeException("Vaccine not found in center stock");
        }
        
        CenterVaccine centerVaccine = centerVaccineOpt.get();
        
        // Kiểm tra stock còn đủ không
        if (centerVaccine.getStockQuantity() == null || centerVaccine.getStockQuantity() <= 0) {
            throw new RuntimeException("Vaccine is out of stock at this center");
        }
        
        // Tạo VaccinationRecord
        VaccinationRecord record = new VaccinationRecord();
        record.setAppointment(appointment);
        record.setUser(userToInject);
        record.setVaccine(appointment.getVaccine());
        record.setVaccineLot(vaccineLot);
        record.setNurse(nurse);
        record.setInjectionDate(injectionDate != null ? injectionDate : LocalDate.now());
        record.setInjectionTime(injectionTime != null ? injectionTime : LocalTime.now());
        record.setDoseNumber(appointment.getDoseNumber() != null ? appointment.getDoseNumber() : 1);
        record.setInjectionSite(injectionSite);
        record.setBatchNumber(vaccineLot.getLotNumber());
        record.setDoseAmount(doseAmount);
        record.setCertificateNumber(certificateNumber);
        
        // Tính nextDoseDate nếu có daysBetweenDoses
        if (appointment.getVaccine().getDaysBetweenDoses() != null && 
            appointment.getVaccine().getDaysBetweenDoses() > 0 &&
            appointment.getVaccine().getDosesRequired() != null &&
            record.getDoseNumber() < appointment.getVaccine().getDosesRequired()) {
            record.setNextDoseDate(record.getInjectionDate().plusDays(appointment.getVaccine().getDaysBetweenDoses()));
        }
        
        record.setCreatedAt(LocalDateTime.now());
        
        // Trừ stock quantity tại trung tâm
        int currentStock = centerVaccine.getStockQuantity();
        centerVaccine.setStockQuantity(currentStock - 1);
        centerVaccineRepository.save(centerVaccine);
        
        // Trừ remaining quantity của vaccine lot
        int currentLotRemaining = vaccineLot.getRemainingQuantity();
        vaccineLot.setRemainingQuantity(currentLotRemaining - 1);
        vaccineLotRepository.save(vaccineLot);
        
        // Cập nhật appointment status thành COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
        
        // Lưu VaccinationRecord
        return vaccinationRecordRepository.save(record);
    }
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentHistoryRepository appointmentHistoryRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@tiemchung.gov.vn}")
    private String fromEmail;
    
    /**
     * Generate certificate number cho vaccination record
     * Format: VC-YYYYMMDD-HHMMSS-XXXXX (VC-20240116-143025-00001)
     */
    public String generateCertificateNumber() {
        LocalDateTime now = LocalDateTime.now();
        String dateTime = String.format("%04d%02d%02d-%02d%02d%02d",
            now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
            now.getHour(), now.getMinute(), now.getSecond());
        
        // Tìm số thứ tự tiếp theo trong ngày
        long countToday = vaccinationRecordRepository.count();
        String sequence = String.format("%05d", (countToday % 100000) + 1);
        
        return "VC-" + dateTime + "-" + sequence;
    }
    
    /**
     * Tạo VaccinationRecord với certificate number tự động và gửi email
     */
    public VaccinationRecord createVaccinationRecordWithCertificate(
            Long appointmentId,
            Long vaccineLotId,
            Long nurseId,
            LocalDate injectionDate,
            LocalTime injectionTime,
            String injectionSite,
            Double doseAmount) {
        
        // Generate certificate number
        String certificateNumber = generateCertificateNumber();
        
        // Tạo vaccination record
        VaccinationRecord record = createVaccinationRecord(
            appointmentId, vaccineLotId, nurseId,
            injectionDate, injectionTime, injectionSite, doseAmount, certificateNumber
        );
        
        // Tạo AppointmentHistory cho việc hoàn thành tiêm
        Appointment appointment = record.getAppointment();
        AppointmentStatus oldStatus = AppointmentStatus.APPROVED; // Giả sử từ APPROVED
        createAppointmentHistory(appointment, oldStatus, AppointmentStatus.COMPLETED, 
            record.getNurse(), "Hoàn thành tiêm vaccine");
        
        // Gửi email chứng nhận
        try {
            sendVaccinationCertificateEmail(record);
        } catch (Exception e) {
            // Log error nhưng không throw để không làm gián đoạn quá trình tiêm
            System.err.println("Failed to send certificate email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return record;
    }
    
    /**
     * Tạo AppointmentHistory
     */
    private void createAppointmentHistory(Appointment appointment, AppointmentStatus oldStatus, 
                                         AppointmentStatus newStatus, User changedBy, String reason) {
        AppointmentHistory history = new AppointmentHistory();
        history.setAppointment(appointment);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        history.setReason(reason);
        
        appointmentHistoryRepository.save(history);
    }
    
    /**
     * Gửi email chứng nhận tiêm chủng
     */
    private void sendVaccinationCertificateEmail(VaccinationRecord record) {
        Appointment appointment = record.getAppointment();
        User user = getNotificationUser(appointment);
        
        if (user == null || user.getEmail() == null) {
            return; // Không có email để gửi
        }
        
        String title = "Chứng nhận tiêm chủng - " + record.getVaccine().getName();
        String content = buildCertificateEmailContent(record);
        
        // Gửi email chứng nhận trực tiếp
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject(title);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send certificate email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Lấy user để gửi thông báo (bookedForUser hoặc bookedByUser)
     */
    private User getNotificationUser(Appointment appointment) {
        if (appointment.getBookedForUser() != null) {
            return appointment.getBookedForUser();
        } else if (appointment.getBookedByUser() != null) {
            return appointment.getBookedByUser();
        }
        return null;
    }
    
    /**
     * Xây dựng nội dung email chứng nhận
     */
    private String buildCertificateEmailContent(VaccinationRecord record) {
        Appointment appointment = record.getAppointment();
        String patientName = getPatientName(appointment);
        
        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(patientName).append(",\n\n");
        content.append("Bạn đã hoàn thành tiêm vaccine thành công.\n\n");
        content.append("Thông tin tiêm chủng:\n");
        content.append("- Vaccine: ").append(record.getVaccine().getName()).append("\n");
        content.append("- Mũi tiêm: ").append(record.getDoseNumber()).append("\n");
        content.append("- Ngày tiêm: ").append(record.getInjectionDate()).append(" ").append(record.getInjectionTime()).append("\n");
        content.append("- Số chứng nhận: ").append(record.getCertificateNumber()).append("\n");
        content.append("- Trung tâm: ").append(appointment.getCenter().getName()).append("\n");
        if (record.getNextDoseDate() != null) {
            content.append("- Mũi tiếp theo: ").append(record.getNextDoseDate()).append("\n");
        }
        content.append("\n");
        content.append("Bạn có thể tải chứng nhận tại: ").append("/api/vaccination-records/").append(record.getId()).append("/certificate\n\n");
        content.append("Trân trọng,\n");
        content.append("Hệ thống Tiêm chủng Quốc gia");
        
        return content.toString();
    }
    
    /**
     * Lấy tên bệnh nhân từ appointment
     */
    private String getPatientName(Appointment appointment) {
        if (appointment.getBookedForUser() != null) {
            return appointment.getBookedForUser().getFullName();
        } else if (appointment.getFamilyMember() != null) {
            return appointment.getFamilyMember().getFullName();
        } else if (appointment.getGuestFullName() != null) {
            return appointment.getGuestFullName();
        } else if (appointment.getBookedByUser() != null) {
            return appointment.getBookedByUser().getFullName();
        }
        return "Quý khách";
    }
    
    /**
     * Lấy VaccinationRecord theo ID
     */
    public VaccinationRecord getVaccinationRecordById(Long id) {
        return vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccination record not found"));
    }
}

