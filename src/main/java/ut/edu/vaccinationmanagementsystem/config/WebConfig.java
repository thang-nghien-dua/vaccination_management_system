package ut.edu.vaccinationmanagementsystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ut.edu.vaccinationmanagementsystem.interceptor.UserStatusInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private UserStatusInterceptor userStatusInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userStatusInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/login",
                    "/register",
                    "/api/auth/**",
                    "/oauth2/**",
                    "/error",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/",
                    "/logout"
                );
    }
}

