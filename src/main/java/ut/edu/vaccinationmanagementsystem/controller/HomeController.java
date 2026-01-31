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
    public String login(@RequestParam(required = false) String registered, Model model) {
        if (registered != null) {
            model.addAttribute("registered", true);
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
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        try {
            User currentUser = getCurrentUser(authentication);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("vaccineId", id);
        } catch (Exception e) {
            return "redirect:/login";
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
}

