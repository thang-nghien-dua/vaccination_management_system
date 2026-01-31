package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.PaymentStatus;
import ut.edu.vaccinationmanagementsystem.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý logic báo cáo và thống kê
 */
@Service
@Transactional
public class ReportService {
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private VaccineRepository vaccineRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;
    
    /**
     * Báo cáo tiêm chủng
     */
    public Map<String, Object> getVaccinationReport(LocalDate startDate, LocalDate endDate) {
        List<VaccinationRecord> records = vaccinationRecordRepository.findAll();
        
        // Filter theo ngày nếu có
        if (startDate != null || endDate != null) {
            records = records.stream()
                    .filter(record -> {
                        if (record.getInjectionDate() == null) return false;
                        if (startDate != null && record.getInjectionDate().isBefore(startDate)) return false;
                        if (endDate != null && record.getInjectionDate().isAfter(endDate)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
        }
        
        // Thống kê theo vaccine
        Map<Long, Map<String, Object>> vaccineStats = new HashMap<>();
        for (VaccinationRecord record : records) {
            Long vaccineId = record.getVaccine().getId();
            vaccineStats.putIfAbsent(vaccineId, new HashMap<>());
            Map<String, Object> stats = vaccineStats.get(vaccineId);
            
            stats.put("vaccineId", vaccineId);
            stats.put("vaccineName", record.getVaccine().getName());
            stats.put("count", ((Integer) stats.getOrDefault("count", 0)) + 1);
            
            // Thống kê theo mũi tiêm
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> doseStats = (Map<Integer, Integer>) stats.getOrDefault("doseStats", new HashMap<>());
            Integer doseNumber = record.getDoseNumber();
            doseStats.put(doseNumber, doseStats.getOrDefault(doseNumber, 0) + 1);
            stats.put("doseStats", doseStats);
        }
        
        // Thống kê theo trung tâm
        Map<Long, Map<String, Object>> centerStats = new HashMap<>();
        for (VaccinationRecord record : records) {
            if (record.getAppointment() != null && record.getAppointment().getCenter() != null) {
                Long centerId = record.getAppointment().getCenter().getId();
                centerStats.putIfAbsent(centerId, new HashMap<>());
                Map<String, Object> stats = centerStats.get(centerId);
                
                stats.put("centerId", centerId);
                stats.put("centerName", record.getAppointment().getCenter().getName());
                stats.put("count", ((Integer) stats.getOrDefault("count", 0)) + 1);
            }
        }
        
        // Thống kê theo ngày
        Map<LocalDate, Integer> dailyStats = records.stream()
                .filter(record -> record.getInjectionDate() != null)
                .collect(Collectors.groupingBy(
                        VaccinationRecord::getInjectionDate,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalInjections", records.size());
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("vaccineStatistics", new ArrayList<>(vaccineStats.values()));
        report.put("centerStatistics", new ArrayList<>(centerStats.values()));
        report.put("dailyStatistics", dailyStats);
        
        return report;
    }
    
    /**
     * Báo cáo vaccine
     */
    public Map<String, Object> getVaccineReport() {
        List<Vaccine> vaccines = vaccineRepository.findAll();
        List<VaccinationRecord> records = vaccinationRecordRepository.findAll();
        
        List<Map<String, Object>> vaccineReports = vaccines.stream().map(vaccine -> {
            Map<String, Object> report = new HashMap<>();
            report.put("vaccineId", vaccine.getId());
            report.put("vaccineName", vaccine.getName());
            report.put("price", vaccine.getPrice());
            report.put("dosesRequired", vaccine.getDosesRequired());
            report.put("daysBetweenDoses", vaccine.getDaysBetweenDoses());
            
            // Đếm số lần tiêm vaccine này
            long injectionCount = records.stream()
                    .filter(record -> record.getVaccine().getId().equals(vaccine.getId()))
                    .count();
            report.put("totalInjections", injectionCount);
            
            // Đếm số user đã tiêm vaccine này
            long userCount = records.stream()
                    .filter(record -> record.getVaccine().getId().equals(vaccine.getId()))
                    .map(record -> record.getUser().getId())
                    .distinct()
                    .count();
            report.put("totalUsers", userCount);
            
            // Thống kê theo mũi tiêm
            Map<Integer, Long> doseCounts = records.stream()
                    .filter(record -> record.getVaccine().getId().equals(vaccine.getId()))
                    .collect(Collectors.groupingBy(
                            VaccinationRecord::getDoseNumber,
                            Collectors.counting()
                    ));
            report.put("doseCounts", doseCounts);
            
            // Tính doanh thu từ vaccine này
            BigDecimal revenue = appointmentRepository.findAll().stream()
                    .filter(apt -> apt.getVaccine() != null && apt.getVaccine().getId().equals(vaccine.getId()))
                    .filter(apt -> apt.getStatus() == AppointmentStatus.COMPLETED)
                    .map(apt -> {
                        Optional<Payment> paymentOpt = paymentRepository.findByAppointment(apt);
                        if (paymentOpt.isPresent() && paymentOpt.get().getPaymentStatus() == PaymentStatus.PAID) {
                            return paymentOpt.get().getAmount();
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            report.put("revenue", revenue);
            
            return report;
        }).collect(Collectors.toList());
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalVaccines", vaccines.size());
        report.put("vaccines", vaccineReports);
        
        return report;
    }
    
    /**
     * Báo cáo khách hàng
     */
    public Map<String, Object> getCustomerReport(LocalDate startDate, LocalDate endDate) {
        List<User> customers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == ut.edu.vaccinationmanagementsystem.entity.enums.Role.CUSTOMER)
                .collect(Collectors.toList());
        
        List<Map<String, Object>> customerReports = customers.stream().map(customer -> {
            Map<String, Object> report = new HashMap<>();
            report.put("userId", customer.getId());
            report.put("fullName", customer.getFullName());
            report.put("email", customer.getEmail());
            report.put("phoneNumber", customer.getPhoneNumber());
            report.put("status", customer.getStatus().name());
            report.put("createdAt", customer.getCreateAt());
            
            // Đếm số lần tiêm
            List<VaccinationRecord> customerRecords = vaccinationRecordRepository.findAll().stream()
                    .filter(record -> record.getUser().getId().equals(customer.getId()))
                    .filter(record -> {
                        if (startDate != null && record.getInjectionDate() != null && 
                            record.getInjectionDate().isBefore(startDate)) return false;
                        if (endDate != null && record.getInjectionDate() != null && 
                            record.getInjectionDate().isAfter(endDate)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
            report.put("totalInjections", customerRecords.size());
            
            // Đếm số vaccine khác nhau đã tiêm
            long uniqueVaccines = customerRecords.stream()
                    .map(record -> record.getVaccine().getId())
                    .distinct()
                    .count();
            report.put("uniqueVaccines", uniqueVaccines);
            
            // Đếm số appointments
            long appointmentCount = appointmentRepository.findAll().stream()
                    .filter(apt -> (apt.getBookedByUser() != null && apt.getBookedByUser().getId().equals(customer.getId())) ||
                                  (apt.getBookedForUser() != null && apt.getBookedForUser().getId().equals(customer.getId())))
                    .filter(apt -> {
                        if (startDate != null && apt.getAppointmentDate() != null && 
                            apt.getAppointmentDate().isBefore(startDate)) return false;
                        if (endDate != null && apt.getAppointmentDate() != null && 
                            apt.getAppointmentDate().isAfter(endDate)) return false;
                        return true;
                    })
                    .count();
            report.put("totalAppointments", appointmentCount);
            
            // Tính tổng chi tiêu
            BigDecimal totalSpent = appointmentRepository.findAll().stream()
                    .filter(apt -> (apt.getBookedByUser() != null && apt.getBookedByUser().getId().equals(customer.getId())) ||
                                  (apt.getBookedForUser() != null && apt.getBookedForUser().getId().equals(customer.getId())))
                    .filter(apt -> {
                        if (startDate != null && apt.getAppointmentDate() != null && 
                            apt.getAppointmentDate().isBefore(startDate)) return false;
                        if (endDate != null && apt.getAppointmentDate() != null && 
                            apt.getAppointmentDate().isAfter(endDate)) return false;
                        return true;
                    })
                    .map(apt -> {
                        Optional<Payment> paymentOpt = paymentRepository.findByAppointment(apt);
                        if (paymentOpt.isPresent() && paymentOpt.get().getPaymentStatus() == PaymentStatus.PAID) {
                            return paymentOpt.get().getAmount();
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            report.put("totalSpent", totalSpent);
            
            return report;
        }).collect(Collectors.toList());
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalCustomers", customers.size());
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("customers", customerReports);
        
        return report;
    }
}

