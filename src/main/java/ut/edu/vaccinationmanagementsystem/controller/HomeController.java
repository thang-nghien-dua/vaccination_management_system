package ut.edu.vaccinationmanagementsystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.UserService;

@Controller
public class HomeController {
    
    private final UserService userService;
    
    public HomeController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * GET /about
     * Trang giới thiệu
     */
    @GetMapping("/about")
    public String about(Model model) {
        // Lấy thông tin user hiện tại nếu đã đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                User currentUser = getCurrentUser(authentication);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("isAuthenticated", true);
            } catch (Exception e) {
                model.addAttribute("isAuthenticated", false);
            }
        } else {
            model.addAttribute("isAuthenticated", false);
        }
        return "about";
    }
    
    /**
     * GET /
     * Redirect đến trang about nếu chưa đăng nhập, hoặc trang home nếu đã đăng nhập
     */
    @GetMapping("/")
    public String root() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            // Đã đăng nhập → redirect đến trang home
            return "redirect:/home";
        } else {
            // Chưa đăng nhập → redirect đến trang about
            return "redirect:/about";
        }
    }
    
    /**
     * GET /home
     * Trang chủ (dashboard) - yêu cầu đăng nhập
     */
    @GetMapping("/home")
    public String home(Model model) {
        // Lấy thông tin user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                User currentUser = getCurrentUser(authentication);
                
                // Nếu là DOCTOR, redirect về trang doctor
                if (currentUser.getRole() == ut.edu.vaccinationmanagementsystem.entity.enums.Role.DOCTOR) {
                    return "redirect:/doctor/home";
                }
                // Nếu là ADMIN, redirect về trang admin
                if (currentUser.getRole() == ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN) {
                    return "redirect:/admin/home";
                }
                // Nếu là NURSE, redirect về trang nurse dashboard
                if (currentUser.getRole() == ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE) {
                    return "redirect:/nurse/dashboard";
                }
                // Nếu là RECEPTIONIST, redirect về trang receptionist dashboard
                if (currentUser.getRole() == ut.edu.vaccinationmanagementsystem.entity.enums.Role.RECEPTIONIST) {
                    return "redirect:/receptionist/dashboard";
                }
                
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("isAuthenticated", true);
            } catch (Exception e) {
                // Nếu không lấy được user, redirect về login
                return "redirect:/login";
            }
        } else {
            // Chưa đăng nhập, redirect về login
            return "redirect:/login";
        }
        return "home";
    }
    
    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    private User getCurrentUser(Authentication authentication) {
        // Nếu là OAuth2 user
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return customOAuth2User.getUser();
        }
        
        // Nếu là email/password login
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getUser();
        }
        
        // Fallback: lấy từ email
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
    
    /**
     * GET /login
     * Trang đăng nhập
     */
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String registered, 
                       @RequestParam(required = false) String locked,
                       Model model) {
        if (registered != null) {
            model.addAttribute("registered", true);
        }
        if (locked != null) {
            model.addAttribute("locked", true);
        }
        return "login";
    }
    
    /**
     * GET /register
     * Trang đăng ký
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    /**
     * GET /verify-email-success
     * Trang xác thực email thành công
     */
    @GetMapping("/verify-email-success")
    public String verifyEmailSuccess(@RequestParam(required = false) String verified, Model model) {
        if (verified != null && verified.equals("true")) {
            model.addAttribute("verified", true);
        }
        return "verify-email-success";
    }
    
    /**
     * GET /forgot-password
     * Trang quên mật khẩu
     */
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }
    
    /**
     * GET /reset-password
     * Trang đặt lại mật khẩu
     */
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.trim().isEmpty()) {
            model.addAttribute("error", "Token is required");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }
    
    /**
     * GET /profile
     * Trang profile - yêu cầu đăng nhập
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        // Lấy thông tin user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                User currentUser = getCurrentUser(authentication);
                
                // Nếu user được lấy từ session, refresh từ database để có dữ liệu mới nhất
                // Nhưng chỉ refresh nếu có ID (tránh lỗi với OAuth2 user chưa có ID)
                if (currentUser != null && currentUser.getId() != null) {
                    try {
                        currentUser = userService.getUserById(currentUser.getId());
                    } catch (Exception e) {
                        // Nếu không refresh được, vẫn dùng user từ session
                    }
                }
                
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("isAuthenticated", true);
            } catch (Exception e) {
                // Nếu không lấy được user, redirect về login
                return "redirect:/login";
            }
        } else {
            // Chưa đăng nhập, redirect về login
            return "redirect:/login";
        }
        return "profile";
    }
    
    /**
     * GET /family-members
     * Trang quản lý người thân - yêu cầu đăng nhập
     */
    @GetMapping("/family-members")
    public String familyMembers(Model model) {
        // Lấy thông tin user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                User currentUser = getCurrentUser(authentication);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("isAuthenticated", true);
            } catch (Exception e) {
                // Nếu không lấy được user, redirect về login
                return "redirect:/login";
            }
        } else {
            // Chưa đăng nhập, redirect về login
            return "redirect:/login";
        }
        return "family-members";
    }

    @GetMapping("/vaccines")
    public String vaccines(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "vaccines";
    }
    
    @GetMapping("/vaccines/{id}")
    public String vaccineDetail(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Cho phép khách vãng lai xem chi tiết vaccine
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser");
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("vaccineId", id);
        
        if (isAuthenticated) {
            try {
                User currentUser = getCurrentUser(authentication);
                model.addAttribute("currentUser", currentUser);
            } catch (Exception e) {
                // Nếu có lỗi khi lấy user, vẫn cho phép xem như khách vãng lai
                model.addAttribute("isAuthenticated", false);
            }
        }
        return "vaccine-detail";
    }
    
    /**
     * GET /appointments
     * Trang đặt lịch - yêu cầu đăng nhập
     */
    @GetMapping("/appointments")
    public String appointments(@RequestParam(required = false) Long vaccineId, 
                              @RequestParam(required = false) Long familyMemberId, 
                              Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            if (vaccineId != null) {
                model.addAttribute("selectedVaccineId", vaccineId);
            }
            if (familyMemberId != null) {
                model.addAttribute("selectedFamilyMemberId", familyMemberId);
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "appointment";
    }
    
    /**
     * GET /vaccination-history
     * Trang hồ sơ tiêm chủng
     */
    @GetMapping("/vaccination-history")
    public String vaccinationHistory(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "vaccination-history";
    }
    
    /**
     * GET /notifications
     * Trang thông báo
     */
    @GetMapping("/notifications")
    public String notifications(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "notifications";
    }
    
    /**
     * GET /notifications/{id}
     * Trang chi tiết thông báo
     */
    @GetMapping("/notifications/{id}")
    public String notificationDetail(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("notificationId", id);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "chi_tiet_thong_bao";
    }
    
    /**
     * GET /payment-result
     * Trang hiển thị kết quả thanh toán
     */
    @GetMapping("/payment-result")
    public String paymentResult(@RequestParam(required = false) Boolean success,
                                @RequestParam(required = false) String bookingCode,
                                @RequestParam(required = false) Long appointmentId,
                                @RequestParam(required = false) String message,
                                Model model) {
        // Lấy thông tin user hiện tại nếu đã đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                User currentUser = getCurrentUser(authentication);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("isAuthenticated", true);
            } catch (Exception e) {
                model.addAttribute("isAuthenticated", false);
            }
        } else {
            model.addAttribute("isAuthenticated", false);
        }
        
        model.addAttribute("success", success != null && success);
        model.addAttribute("bookingCode", bookingCode);
        model.addAttribute("appointmentId", appointmentId);
        model.addAttribute("message", message != null ? message : (success != null && success ? "Thanh toán thành công" : "Thanh toán thất bại"));
        
        return "payment-result";
    }
    
    /**
     * GET /nurse/dashboard
     * Dashboard cho Nurse - quản lý tiêm vaccine và phản ứng phụ
     */
    @GetMapping("/nurse/dashboard")
    public String nurseDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Kiểm tra role
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "nurse-dashboard";
    }
    
    /**
     * GET /nurse/profile
     * Trang profile cho Nurse
     */
    @GetMapping("/nurse/profile")
    public String nurseProfile(@RequestParam(required = false) String returnUrl, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Kiểm tra role
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            // Refresh từ database để có dữ liệu mới nhất
            if (currentUser != null && currentUser.getId() != null) {
                try {
                    currentUser = userService.getUserById(currentUser.getId());
                } catch (Exception e) {
                    // Nếu không refresh được, vẫn dùng user từ session
                }
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            if (returnUrl != null && !returnUrl.isEmpty()) {
                model.addAttribute("returnUrl", returnUrl);
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "nurse-profile";
    }
    
    /**
     * GET /nurse/appointments
     * Trang quản lý lịch hẹn cho Nurse
     */
    @GetMapping("/nurse/appointments")
    public String nurseAppointments(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "nurse-appointments";
    }
    
    /**
     * GET /nurse/inject/{id}
     * Trang thực hiện tiêm vaccine cho Nurse
     */
    @GetMapping("/nurse/inject/{id}")
    public String nurseInject(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("appointmentId", id);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "tiem_vaccin";
    }
    
    /**
     * GET /nurse/vaccines
     * Trang quản lý vaccine cho Nurse
     */
    @GetMapping("/nurse/vaccines")
    public String nurseVaccines(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "nurse-vaccines";
    }
    
    /**
     * GET /nurse/vaccination-history
     * Redirect đến trang lịch sử tiêm mới
     */
    @GetMapping("/nurse/vaccination-history")
    public String nurseVaccinationHistoryRedirect() {
        return "redirect:/nurse/lich-su-tiem-y-ta";
    }
    
    /**
     * GET /nurse/lich-su-tiem-y-ta
     * Trang lịch sử tiêm cho Nurse
     */
    @GetMapping("/nurse/lich-su-tiem-y-ta")
    public String nurseLichSuTiemYTa(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Kiểm tra role
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "lich_su_tiem_y_ta";
    }
    
    /**
     * GET /nurse/lich-su-tiem-y-ta/chi-tiet/{recordId}
     * Trang chi tiết lịch sử tiêm cho Nurse
     */
    @GetMapping("/nurse/lich-su-tiem-y-ta/chi-tiet/{recordId}")
    public String nurseLichSuTiemYTaChiTiet(@PathVariable Long recordId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            model.addAttribute("recordId", recordId);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/nurse/lich-su-tiem-y-ta";
        }
        
        return "chi_tiet-lich_su_tiem_y_ta";
    }
    
    /**
     * GET /nurse/vaccination-history/detail/{recordId}
     * Xem chi tiết lịch sử tiêm chủng (chỉ đọc)
     */
    @GetMapping("/nurse/vaccination-history/detail/{recordId}")
    public String nurseVaccinationHistoryDetail(@PathVariable Long recordId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            // Truyền recordId để trang có thể load dữ liệu
            model.addAttribute("recordId", recordId);
            model.addAttribute("isReadOnly", true); // Đánh dấu là chỉ đọc
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/nurse/history-y-ta";
        }
        
        return "chi_tiet_tiem_chung";
    }
    
    /**
     * GET /nurse/reactions
     * Trang quản lý phản ứng phụ cho Nurse
     */
    @GetMapping("/nurse/reactions")
    public String nurseReactions(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "nurse-reactions";
    }
    
    /**
     * GET /nurse/trace
     * Trang truy vết vaccine cho Nurse
     */
    @GetMapping("/nurse/trace")
    public String nurseTrace(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        // Tạm thời redirect về vaccination-history vì chưa có template trace
        return "redirect:/nurse/vaccination-history";
    }
    
    /**
     * GET /trace
     * Trang truy vết vaccine (alias cho /nurse/trace)
     */
    @GetMapping("/trace")
    public String trace(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.NURSE && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            // Redirect về nurse trace
            return "redirect:/nurse/trace";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    /**
     * GET /receptionist/dashboard
     * Dashboard cho Receptionist - quản lý lịch hẹn
     */
    @GetMapping("/receptionist/dashboard")
    public String receptionistDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Kiểm tra role
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.RECEPTIONIST && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "receptionist-dashboard";
    }
    
    /**
     * GET /receptionist/profile
     * Trang profile cho Receptionist
     */
    @GetMapping("/receptionist/profile")
    public String receptionistProfile(@RequestParam(required = false) String returnUrl, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Kiểm tra role
            if (currentUser.getRole() == null || 
                (currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.RECEPTIONIST && 
                 currentUser.getRole() != ut.edu.vaccinationmanagementsystem.entity.enums.Role.ADMIN)) {
                return "redirect:/home";
            }
            
            // Refresh từ database để có dữ liệu mới nhất
            if (currentUser != null && currentUser.getId() != null) {
                try {
                    currentUser = userService.getUserById(currentUser.getId());
                } catch (Exception e) {
                    // Nếu không refresh được, vẫn dùng user từ session
                }
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            if (returnUrl != null && !returnUrl.isEmpty()) {
                model.addAttribute("returnUrl", returnUrl);
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "receptionist-profile";
    }
}

