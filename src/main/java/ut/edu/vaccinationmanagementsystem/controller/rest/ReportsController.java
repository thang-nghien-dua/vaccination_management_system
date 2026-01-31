package ut.edu.vaccinationmanagementsystem.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.repository.UserRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.ReportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        // Nếu là OAuth2 user
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return customOAuth2User.getUser();
        }
        
        // Nếu là email/password login
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        
        throw new RuntimeException("User not found");
    }
    
    /**
     * Kiểm tra quyền ADMIN
     */
    private void checkAdminPermission(User user) {
        if (user.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can access reports");
        }
    }
    
    /**
     * GET /api/reports/vaccination
     * Báo cáo tiêm chủng
     */
    @GetMapping("/vaccination")
    public ResponseEntity<?> getVaccinationReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            LocalDate start = null;
            LocalDate end = null;
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDate.parse(startDate);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDate.parse(endDate);
            }
            
            Map<String, Object> report = reportService.getVaccinationReport(start, end);
            
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/reports/vaccine
     * Báo cáo vaccine
     */
    @GetMapping("/vaccine")
    public ResponseEntity<?> getVaccineReport() {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            Map<String, Object> report = reportService.getVaccineReport();
            
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/reports/customer
     * Báo cáo khách hàng
     */
    @GetMapping("/customer")
    public ResponseEntity<?> getCustomerReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            LocalDate start = null;
            LocalDate end = null;
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDate.parse(startDate);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDate.parse(endDate);
            }
            
            Map<String, Object> report = reportService.getCustomerReport(start, end);
            
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/reports/export?type={}&format={}
     * Export báo cáo Excel/PDF
     * type: vaccination, vaccine, customer
     * format: excel, pdf
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportReport(
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "excel") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            User currentUser = getCurrentUser();
            checkAdminPermission(currentUser);
            
            LocalDate start = null;
            LocalDate end = null;
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDate.parse(startDate);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDate.parse(endDate);
            }
            
            Map<String, Object> reportData;
            String fileName;
            
            // Lấy dữ liệu báo cáo
            switch (type.toLowerCase()) {
                case "vaccination":
                    reportData = reportService.getVaccinationReport(start, end);
                    fileName = "vaccination_report";
                    break;
                case "vaccine":
                    reportData = reportService.getVaccineReport();
                    fileName = "vaccine_report";
                    break;
                case "customer":
                    reportData = reportService.getCustomerReport(start, end);
                    fileName = "customer_report";
                    break;
                default:
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid report type. Valid types: vaccination, vaccine, customer");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Tạo file name với timestamp
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fullFileName = fileName + "_" + timestamp;
            
            // Export format
            if (format.equalsIgnoreCase("excel")) {
                // TODO: Implement Excel export using Apache POI
                // Tạm thời trả về JSON
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setContentDispositionFormData("attachment", fullFileName + ".json");
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(reportData);
            } else if (format.equalsIgnoreCase("pdf")) {
                // TODO: Implement PDF export using iText or Apache PDFBox
                // Tạm thời trả về text format
                String pdfContent = generateTextReport(reportData, type);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.setContentDispositionFormData("attachment", fullFileName + ".txt");
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(pdfContent);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid format. Valid formats: excel, pdf");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Generate text report (tạm thời, có thể chuyển sang PDF sau)
     */
    private String generateTextReport(Map<String, Object> reportData, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("                    BÁO CÁO ").append(type.toUpperCase()).append("\n");
        sb.append("═══════════════════════════════════════════════════════\n\n");
        sb.append("Ngày xuất báo cáo: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
        
        // Format report data thành text
        if (type.equals("vaccination")) {
            sb.append("Tổng số lần tiêm: ").append(reportData.get("totalInjections")).append("\n\n");
            // Có thể thêm chi tiết khác
        } else if (type.equals("vaccine")) {
            sb.append("Tổng số vaccine: ").append(reportData.get("totalVaccines")).append("\n\n");
        } else if (type.equals("customer")) {
            sb.append("Tổng số khách hàng: ").append(reportData.get("totalCustomers")).append("\n\n");
        }
        
        sb.append("\n═══════════════════════════════════════════════════════\n");
        sb.append("Chi tiết báo cáo:\n");
        sb.append(reportData.toString());
        sb.append("\n═══════════════════════════════════════════════════════\n");
        
        return sb.toString();
    }
}

