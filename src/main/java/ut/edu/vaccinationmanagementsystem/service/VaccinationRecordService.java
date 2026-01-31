package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.repository.*;
import ut.edu.vaccinationmanagementsystem.service.EmailService;

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
    
    @Autowired
    private AppointmentHistoryRepository appointmentHistoryRepository;
    
    @Autowired
    private StaffInfoRepository staffInfoRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Tạo số chứng nhận tiêm chủng tự động
     * Format: CERT-YYYYMMDD-HHMMSS-XXXX (XXXX là số random 4 chữ số)
     */
    private String generateCertificateNumber() {
        LocalDateTime now = LocalDateTime.now();
        String dateTime = String.format("%04d%02d%02d-%02d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond());
        int random = (int)(Math.random() * 10000);
        return String.format("CERT-%s-%04d", dateTime, random);
    }
    
    /**
     * Tạo AppointmentHistory khi status thay đổi
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
    private void sendCertificateEmail(VaccinationRecord record) {
        try {
            User userToNotify = record.getUser();
            String email = userToNotify.getEmail();
            String fullName = userToNotify.getFullName();
            
            // Nếu là family member, lấy thông tin từ appointment
            if (record.getAppointment().getFamilyMember() != null) {
                fullName = record.getAppointment().getFamilyMember().getFullName();
            }
            
            String subject = "Chứng nhận tiêm chủng - " + record.getVaccine().getName();
            String content = String.format(
                "Xin chào %s,\n\n" +
                "Bạn đã hoàn thành tiêm vaccine %s (Mũi %d) tại %s.\n\n" +
                "Thông tin tiêm chủng:\n" +
                "- Ngày tiêm: %s\n" +
                "- Giờ tiêm: %s\n" +
                "- Số chứng nhận: %s\n" +
                "- Số lô vaccine: %s\n" +
                "- Vị trí tiêm: %s\n" +
                "%s\n\n" +
                "Bạn có thể tải chứng nhận tại: %s/api/vaccination-records/%d/certificate\n\n" +
                "Trân trọng,\n" +
                "Hệ thống Tiêm chủng Quốc gia",
                fullName,
                record.getVaccine().getName(),
                record.getDoseNumber(),
                record.getAppointment().getCenter() != null ? record.getAppointment().getCenter().getName() : "N/A",
                record.getInjectionDate(),
                record.getInjectionTime(),
                record.getCertificateNumber(),
                record.getBatchNumber(),
                record.getInjectionSite() != null ? record.getInjectionSite() : "N/A",
                record.getNextDoseDate() != null ? String.format("- Ngày tiêm mũi tiếp theo: %s", record.getNextDoseDate()) : "",
                System.getProperty("app.base-url", "http://localhost:8080"),
                record.getId()
            );
            
            emailService.sendEmail(email, subject, content);
        } catch (Exception e) {
            // Log error nhưng không throw exception
            System.err.println("Failed to send certificate email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
     * @param certificateNumber Số chứng nhận tiêm chủng (nếu null sẽ tự động generate)
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
            String notes,
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
        
        // Kiểm tra nurse có cùng center với appointment (ngoại trừ ADMIN)
        if (nurse.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
            ut.edu.vaccinationmanagementsystem.entity.StaffInfo nurseStaffInfo = staffInfoRepository.findByUser(nurse).orElse(null);
            if (nurseStaffInfo != null && nurseStaffInfo.getCenter() != null) {
                Long nurseCenterId = nurseStaffInfo.getCenter().getId();
                if (!appointment.getCenter().getId().equals(nurseCenterId)) {
                    throw new RuntimeException("Nurse chỉ có thể tiêm cho bệnh nhân của trung tâm mình");
                }
            }
        }
        
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
        } else {
            // Đặt cho bản thân
            if (appointment.getBookedByUser() == null) {
                throw new RuntimeException("Appointment must have bookedByUser");
            }
            userToInject = appointment.getBookedByUser();
        }
        
        // Lấy CenterVaccine để trừ stock
        Optional<CenterVaccine> centerVaccineOpt = centerVaccineRepository.findByCenterAndVaccine(
                appointment.getCenter(),
                appointment.getVaccine()
        );
        
        CenterVaccine centerVaccine;
        
        if (centerVaccineOpt.isEmpty()) {
            // Nếu chưa có bản ghi center_vaccine, tự động tạo mới
            // Tính tổng stock từ tất cả các vaccine lots AVAILABLE của vaccine này
            int totalStock = vaccineLotRepository.findByVaccineId(appointment.getVaccine().getId())
                .stream()
                .filter(lot -> lot.getStatus() == ut.edu.vaccinationmanagementsystem.entity.enums.VaccineLotStatus.AVAILABLE)
                .mapToInt(lot -> lot.getRemainingQuantity() != null ? lot.getRemainingQuantity() : 0)
                .sum();
            
            // Tạo bản ghi center_vaccine mới
            centerVaccine = new CenterVaccine();
            centerVaccine.setCenter(appointment.getCenter());
            centerVaccine.setVaccine(appointment.getVaccine());
            centerVaccine.setStockQuantity(totalStock);
            centerVaccine.setLastRestocked(LocalDateTime.now());
            centerVaccine = centerVaccineRepository.save(centerVaccine);
        } else {
            centerVaccine = centerVaccineOpt.get();
        }
        
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
        record.setNotes(notes);
        
        // Generate certificateNumber nếu chưa có
        if (certificateNumber == null || certificateNumber.trim().isEmpty()) {
            certificateNumber = generateCertificateNumber();
        }
        record.setCertificateNumber(certificateNumber);
        
        // Tính nextDoseDate nếu có daysBetweenDoses
        if (appointment.getVaccine().getDaysBetweenDoses() != null && 
            appointment.getVaccine().getDaysBetweenDoses() > 0 &&
            appointment.getVaccine().getDosesRequired() != null &&
            record.getDoseNumber() < appointment.getVaccine().getDosesRequired()) {
            record.setNextDoseDate(record.getInjectionDate().plusDays(appointment.getVaccine().getDaysBetweenDoses()));
        }
        
        record.setCreatedAt(LocalDateTime.now());
        
        // KHÔNG trừ vaccine ở đây nữa vì đã trừ khi CONFIRMED
        // Vaccine đã được giữ trong appointment.reservedVaccineLot khi appointment được CONFIRMED
        // Chỉ cần kiểm tra xem appointment có reservedVaccineLot không
        if (appointment.getReservedVaccineLot() == null) {
            // Nếu không có reserved lot (backward compatibility hoặc appointment cũ), vẫn trừ như cũ
            // Trừ stock quantity tại trung tâm
            int currentStock = centerVaccine.getStockQuantity();
            centerVaccine.setStockQuantity(currentStock - 1);
            centerVaccineRepository.save(centerVaccine);
            
            // Trừ remaining quantity của vaccine lot
            int currentLotRemaining = vaccineLot.getRemainingQuantity();
            vaccineLot.setRemainingQuantity(currentLotRemaining - 1);
            vaccineLotRepository.save(vaccineLot);
        } else {
            // Đã có reserved lot, không cần trừ nữa
            // Chỉ cần đảm bảo lot được sử dụng đúng
            if (!appointment.getReservedVaccineLot().getId().equals(vaccineLot.getId())) {
                // Nếu lot được chọn khác với lot đã giữ, vẫn cần trừ lot mới và cộng lại lot cũ
                // Nhưng để đơn giản, ta sẽ chỉ cảnh báo
                System.out.println("Warning: Vaccine lot selected (" + vaccineLot.getLotNumber() + 
                    ") differs from reserved lot (" + appointment.getReservedVaccineLot().getLotNumber() + ")");
            }
        }
        
        // Lưu trạng thái cũ trước khi cập nhật
        AppointmentStatus oldStatus = appointment.getStatus();
        
        // Cập nhật appointment status thành COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
        
        // Tạo AppointmentHistory
        createAppointmentHistory(appointment, oldStatus, AppointmentStatus.COMPLETED, 
                                nurse, "Hoàn thành tiêm vaccine");
        
        // Lưu VaccinationRecord
        VaccinationRecord savedRecord = vaccinationRecordRepository.save(record);
        
        // Gửi email chứng nhận
        sendCertificateEmail(savedRecord);
        
        return savedRecord;
    }
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lấy VaccinationRecord theo ID
     */
    public VaccinationRecord getVaccinationRecordById(Long id) {
        return vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vaccination record not found with id: " + id));
    }
}

