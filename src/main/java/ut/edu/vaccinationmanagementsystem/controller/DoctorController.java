package ut.edu.vaccinationmanagementsystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ut.edu.vaccinationmanagementsystem.entity.Screening;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.repository.ScreeningRepository;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.UserService;

import java.util.Optional;

@Controller
public class DoctorController {
    
    private final UserService userService;
    
    @Autowired
    private ScreeningRepository screeningRepository;
    
    public DoctorController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return customOAuth2User.getUser();
        }
        
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getUser();
        }
        
        String email = authentication.getName();
        return userService.getUserByEmail(email);
    }
    
    /**
     * Kiểm tra quyền DOCTOR
     */
    private boolean checkDoctorPermission(User user) {
        return user != null && user.getRole() == Role.DOCTOR;
    }
    
    /**
     * GET /doctor/home
     * Dashboard tổng quan cho bác sĩ
     */
    @GetMapping("/doctor/home")
    public String doctorHome(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkDoctorPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "home_docter";
    }
    
    /**
     * GET /doctor/appointments
     * Danh sách lịch hẹn cần khám
     */
    @GetMapping("/doctor/appointments")
    public String doctorAppointments(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkDoctorPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "liic_hen";
    }
    
    /**
     * GET /doctor/screening/{appointmentId}
     * Chi tiết khám sàng lọc
     */
    @GetMapping("/doctor/screening/{appointmentId}")
    public String doctorScreeningDetail(@PathVariable Long appointmentId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkDoctorPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("appointmentId", appointmentId);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "sang_loc_chi_tiet";
    }
    
    /**
     * GET /doctor/history
     * Lịch sử khám sàng lọc
     */
    @GetMapping("/doctor/history")
    public String doctorHistory(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkDoctorPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "history_docter";
    }
    
    /**
     * GET /doctor/profile
     * Hồ sơ bác sĩ
     */
    @GetMapping("/doctor/profile")
    public String doctorProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkDoctorPermission(currentUser)) {
                return "redirect:/home";
            }
            
            // Refresh user từ database để có dữ liệu mới nhất
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
            return "redirect:/login";
        }
        
        return "profile_docter";
    }
    
    /**
     * GET /doctor/history/detail/{screeningId}
     * Xem chi tiết lịch sử khám sàng lọc (chỉ đọc)
     */
    @GetMapping("/doctor/history/detail/{screeningId}")
    public String doctorHistoryDetail(@PathVariable Long screeningId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkDoctorPermission(currentUser)) {
                return "redirect:/home";
            }
            
            Optional<Screening> screeningOpt = screeningRepository.findById(screeningId);
            if (screeningOpt.isEmpty()) {
                return "redirect:/doctor/history";
            }
            
            Screening screening = screeningOpt.get();
            
            // Kiểm tra quyền: chỉ bác sĩ đã khám mới xem được
            if (!screening.getDoctor().getId().equals(currentUser.getId())) {
                return "redirect:/doctor/history";
            }
            
            // Truyền screeningId và appointmentId để trang có thể load dữ liệu
            model.addAttribute("screeningId", screeningId);
            model.addAttribute("appointmentId", screening.getAppointment().getId());
            model.addAttribute("isReadOnly", true); // Đánh dấu là chỉ đọc
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/doctor/history";
        }
        
        return "sang_loc_chi_tiet";
    }
}

