package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.repository.*;

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
}

