package ut.edu.vaccinationmanagementsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.UserService;

/**
 * Controller cho các trang admin
 */
@Controller
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return customOAuth2User.getUser();
        } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getUser();
        } else {
            String email = authentication.getName();
            return userService.getUserByEmail(email);
        }
    }
    
    /**
     * Kiểm tra quyền ADMIN
     */
    private boolean checkAdminPermission(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }
    
    /**
     * GET /admin/home
     * Dashboard tổng quan cho admin
     */
    @GetMapping("/admin/home")
    public String adminHome(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "tong_quan";
    }
    
    /**
     * GET /admin/vaccines
     * Quản lý vaccine
     */
    @GetMapping("/admin/vaccines")
    public String adminVaccines(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "manager_vaccin";
    }
    
    /**
     * GET /admin/users
     * Quản lý người dùng
     */
    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "manager_user";
    }
    
    /**
     * GET /admin/appointments
     * Quản lý lịch hẹn
     */
    @GetMapping("/admin/appointments")
    public String adminAppointments(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "manager_lich_hen";
    }
    
    /**
     * GET /admin/staff
     * Quản lý nhân viên
     */
    @GetMapping("/admin/staff")
    public String adminStaff(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "manager_staff";
    }
    
    /**
     * GET /admin/reports
     * Thống kê & Báo cáo
     */
    @GetMapping("/admin/reports")
    public String adminReports(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "baocao";
    }
    
    /**
     * GET /admin/settings
     * Cài đặt hệ thống
     */
    @GetMapping("/admin/settings")
    public String adminSettings(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "setting_manager";
    }
    
    /**
     * GET /admin/notifications
     * Quản lý thông báo
     */
    @GetMapping("/admin/notifications")
    public String adminNotifications(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "quan_ly_thong_bao";
    }
    
    /**
     * GET /admin/notifications/send-individual
     * Gửi thông báo cho cá nhân
     */
    @GetMapping("/admin/notifications/send-individual")
    public String adminNotificationsSendIndividual(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "gui_thong_bao_ca_nhan";
    }
    
    /**
     * GET /admin/notifications/send-all
     * Gửi thông báo cho tất cả
     */
    @GetMapping("/admin/notifications/send-all")
    public String adminNotificationsSendAll(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "gui_thong_bao_tat_ca";
    }
    
    /**
     * GET /admin/notifications/send-center
     * Gửi thông báo cho trung tâm
     */
    @GetMapping("/admin/notifications/send-center")
    public String adminNotificationsSendCenter(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "gui_thong_bao_trung_tam";
    }
    
    /**
     * GET /admin/notifications/send-role
     * Gửi thông báo cho vai trò
     */
    @GetMapping("/admin/notifications/send-role")
    public String adminNotificationsSendRole(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "gui_thong_bao_vai_tro";
    }
    
    /**
     * GET /admin/adverse-reactions
     * Quản lý phản ứng phụ
     */
    @GetMapping("/admin/adverse-reactions")
    public String adminAdverseReactions(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "quan_ly_phan_ung_phu";
    }
    
    /**
     * GET /admin/adverse-reactions/{id}
     * Chi tiết phản ứng phụ
     */
    @GetMapping("/admin/adverse-reactions/{id}")
    public String adminAdverseReactionDetail(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("reactionId", id);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "chi-tiet_phan_ung_phu";
    }
    
    /**
     * GET /admin/adverse-reactions/filter
     * Lọc và tìm kiếm phản ứng phụ
     */
    @GetMapping("/admin/adverse-reactions/filter")
    public String adminAdverseReactionsFilter(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "sang_loc_chi_tiet";
    }
    
    /**
     * GET /admin/centers
     * Quản lý trung tâm tiêm chủng
     */
    @GetMapping("/admin/centers")
    public String adminCenters(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "quan_ly_trung-tam";
    }
    
    /**
     * GET /admin/centers/{id}
     * Chi tiết trung tâm
     */
    @GetMapping("/admin/centers/{id}")
    public String adminCenterDetail(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("centerId", id);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "tong_quan";
    }
    
    /**
     * GET /admin/centers/{id}/vaccines
     * Vaccine tại trung tâm
     */
    @GetMapping("/admin/centers/{id}/vaccines")
    public String adminCenterVaccines(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("centerId", id);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "vaccin_tai_chung_tam";
    }
    
    /**
     * GET /admin/centers/{id}/rooms
     * Phòng khám tại trung tâm
     */
    @GetMapping("/admin/centers/{id}/rooms")
    public String adminCenterRooms(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("centerId", id);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "phong_kham_tai_trung_tam";
    }
    
    /**
     * GET /admin/centers/{id}/working-hours
     * Giờ làm việc tại trung tâm
     */
    @GetMapping("/admin/centers/{id}/working-hours")
    public String adminCenterWorkingHours(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("centerId", id);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "gio_lam_viec_phong_kham";
    }
    
    /**
     * GET /admin/centers/{id}/staff
     * Quản lý nhân viên tại trung tâm
     */
    @GetMapping("/admin/centers/{id}/staff")
    public String adminCenterStaff(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("centerId", id);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "quan_ly_nhan_vien";
    }
    
    /**
     * GET /admin/centers/{id}/staff/new
     * Thêm nhân viên mới vào trung tâm
     */
    @GetMapping("/admin/centers/{id}/staff/new")
    public String adminCenterStaffNew(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("centerId", id);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "them_nhan_vien";
    }
    
    /**
     * GET /admin/notifications/templates
     * Quản lý template thông báo
     */
    @GetMapping("/admin/notifications/templates")
    public String adminNotificationsTemplates(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "quan_ly_thong_bao"; // Sử dụng cùng trang với thêm tab/quản lý template
    }
    
    /**
     * GET /admin/adverse-reactions/export
     * Xuất báo cáo phản ứng phụ
     */
    @GetMapping("/admin/adverse-reactions/export")
    public String adminAdverseReactionsExport(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "quan_ly_phan_ung_phu"; // Có thể tạo trang riêng hoặc dùng trang hiện tại với export modal
    }
    
    /**
     * GET /admin/centers/new
     * Form thêm trung tâm mới
     */
    @GetMapping("/admin/centers/new")
    public String adminCentersNew(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        try {
            User currentUser = getCurrentUser(authentication);
            if (!checkAdminPermission(currentUser)) {
                return "redirect:/home";
            }
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAuthenticated", true);
        } catch (Exception e) {
            return "redirect:/login";
        }
        
        return "quan_ly_trung-tam"; // Có thể tạo trang riêng hoặc dùng modal trong trang hiện tại
    }
}

