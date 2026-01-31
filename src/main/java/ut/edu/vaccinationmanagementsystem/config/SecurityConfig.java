package ut.edu.vaccinationmanagementsystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletResponse;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2UserService;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetailsService;
import ut.edu.vaccinationmanagementsystem.service.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    
    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tạm thời disable CSRF cho API, có thể enable lại sau
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/about").permitAll()
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers("/payment-result").permitAll() // Payment result page
                // Protected endpoints - cần đăng nhập
                .requestMatchers("/home").authenticated()
                .requestMatchers("/forgot-password", "/reset-password").permitAll()
                .requestMatchers("/verify-email-success").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/api/vaccines/**").permitAll() // Cho phép xem vaccine API
                .requestMatchers("/api/appointment-slots/available/**").permitAll()
                .requestMatchers("/api/appointments/consultation-request").permitAll() // Cho phép guest gửi yêu cầu tư vấn
                .requestMatchers("/public/vaccines/**").permitAll() // Cho phép xem chi tiết vaccine công khai (cho trang about)
                .requestMatchers("/vaccines/*").permitAll() // Cho phép xem chi tiết vaccine (public) - pattern /vaccines/{id}
                // Protected endpoints - cần đăng nhập
                .requestMatchers("/home", "/profile", "/family-members", "/vaccines", "/vaccination-history", "/appointments", "/notifications").authenticated()
                // Doctor endpoints
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                // Nurse endpoints
                .requestMatchers("/nurse/**").hasAnyRole("NURSE", "ADMIN")
                .requestMatchers("/api/nurse/**").hasAnyRole("NURSE", "ADMIN")
                .requestMatchers("/api/adverse-reactions/**").hasAnyRole("NURSE", "DOCTOR", "ADMIN")
                .requestMatchers("/trace").hasAnyRole("NURSE", "ADMIN")
                // Receptionist endpoints - đặt trước rule chung /api/appointments/**
                .requestMatchers("/receptionist/**").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/api/appointments/today").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/api/appointments/*/check-in").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/api/appointments/*/confirm").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/api/appointments/*/cancel-by-receptionist").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/api/appointments/search").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/api/appointments/*/detail").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/api/appointments/walk-in").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/api/payment/*/mark-paid-cash").hasAnyRole("RECEPTIONIST", "ADMIN")
                // Admin endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/family-members/**").authenticated()
                .requestMatchers("/api/vaccination-history/**").authenticated() // Vaccination history endpoints
                .requestMatchers("/api/dashboard/**").authenticated() // Dashboard endpoints
                .requestMatchers("/api/notifications/**").authenticated() // Notification endpoints
                .requestMatchers("/api/centers/**").authenticated() // Tất cả endpoints của centers cần đăng nhập
                .requestMatchers("/api/clinic-rooms/**").hasRole("ADMIN") // Clinic rooms endpoints - chỉ admin
                .requestMatchers("/api/center-working-hours/**").hasRole("ADMIN") // Center working hours endpoints - chỉ admin
                .requestMatchers("/api/center-vaccines/**").hasRole("ADMIN") // Center vaccines endpoints - chỉ admin
                .requestMatchers("/api/phone/**").authenticated() // Phone verification endpoints
                .requestMatchers("/api/payment/**").authenticated() // Payment endpoints
                .requestMatchers("/api/vaccine-lots/**").hasAnyRole("ADMIN", "NURSE")
                .requestMatchers("/api/appointment-slots/**").authenticated()
                .requestMatchers("/api/appointments/approved").hasAnyRole("NURSE", "ADMIN")
                .requestMatchers("/api/vaccination-records/**").hasAnyRole("NURSE", "ADMIN", "DOCTOR")
                .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                // Tất cả các request khác cần đăng nhập
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login") // Custom login page
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oAuth2LoginSuccessHandler)
                .failureUrl("/login?oauth2error=true") // Redirect về login nếu OAuth2 lỗi
            )
            .formLogin(form -> form.disable()) // Disable form login vì dùng REST API qua JavaScript
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // Nếu là AJAX request, trả về JSON
                    if (request.getHeader("X-Requested-With") != null && 
                        request.getHeader("X-Requested-With").equals("XMLHttpRequest")) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Bạn không có quyền truy cập trang này\"}");
                    } else {
                        // Redirect về home nếu không có quyền
                        response.sendRedirect("/home");
                    }
                })
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        
        return http.build();
    }
}

