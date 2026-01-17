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
                .requestMatchers("/receptionist/dashboard").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/nurse/dashboard").hasAnyRole("NURSE", "ADMIN")
                .requestMatchers("/forgot-password", "/reset-password").permitAll()
                .requestMatchers("/verify-email-success").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/api/vaccines/**").permitAll() // Tạm thời cho phép xem vaccine
                .requestMatchers("/api/appointments/consultation-request").permitAll() // Cho phép guest gửi yêu cầu tư vấn
                // Protected endpoints - cần đăng nhập
                .requestMatchers("/home", "/profile", "/family-members", "/vaccines", "/vaccines/**", "/vaccination-history", "/appointments", "/notifications").authenticated()
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/family-members/**").authenticated()
                .requestMatchers("/api/vaccination-history/**").authenticated() // Vaccination history endpoints
                .requestMatchers("/api/dashboard/**").authenticated() // Dashboard endpoints
                .requestMatchers("/api/notifications/**").authenticated() // Notification endpoints
                .requestMatchers("/api/centers/**").authenticated() // Tất cả endpoints của centers cần đăng nhập
                .requestMatchers("/api/phone/**").authenticated() // Phone verification endpoints
                .requestMatchers("/api/payment/**").authenticated() // Payment endpoints
                .requestMatchers("/api/vaccine-lots/**").hasAnyRole("ADMIN", "NURSE")
                .requestMatchers("/api/appointment-slots/**").authenticated()
                // Receptionist endpoints - quản lý lịch hẹn
                        .requestMatchers("/api/appointments/today").hasAnyRole("RECEPTIONIST", "ADMIN")
                        .requestMatchers("/api/appointments/{id}/check-in").hasAnyRole("RECEPTIONIST", "ADMIN")
                        .requestMatchers("/api/appointments/{id}/confirm").hasAnyRole("RECEPTIONIST", "ADMIN")
                        .requestMatchers("/api/appointments/{id}/cancel-by-receptionist").hasAnyRole("RECEPTIONIST", "ADMIN")
                        .requestMatchers("/api/appointments/search").hasAnyRole("RECEPTIONIST", "ADMIN")
                        .requestMatchers("/api/appointments/walk-in").hasAnyRole("RECEPTIONIST", "ADMIN")
                        .requestMatchers("/api/appointments/{id}/detail").hasAnyRole("RECEPTIONIST", "ADMIN", "NURSE", "DOCTOR")
                        .requestMatchers("/api/appointments/approved").hasAnyRole("NURSE", "ADMIN")
                        .requestMatchers("/api/payment/{appointmentId}/mark-paid-cash").hasAnyRole("RECEPTIONIST", "ADMIN")
                        .requestMatchers("/api/appointment-slots/available/**").hasAnyRole("RECEPTIONIST", "ADMIN")
                        // Nurse endpoints
                        .requestMatchers("/api/vaccine-lots/available").hasAnyRole("NURSE", "ADMIN")
                        .requestMatchers("/api/vaccination-records").hasAnyRole("NURSE", "ADMIN")
                        .requestMatchers("/api/vaccination-records/**").hasAnyRole("NURSE", "ADMIN", "DOCTOR")
                        .requestMatchers("/api/adverse-reactions").hasAnyRole("NURSE", "ADMIN", "DOCTOR")
                        .requestMatchers("/api/adverse-reactions/**").hasAnyRole("NURSE", "ADMIN", "DOCTOR")
                        // Nurse dashboard endpoints
                        .requestMatchers("/api/nurse/**").hasAnyRole("NURSE", "ADMIN")
                        // Trace endpoints - truy vết thông tin (chỉ dành cho Nurse)
                        .requestMatchers("/api/trace/**").hasAnyRole("NURSE", "ADMIN")
                        .requestMatchers("/trace").hasAnyRole("NURSE", "ADMIN")
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

