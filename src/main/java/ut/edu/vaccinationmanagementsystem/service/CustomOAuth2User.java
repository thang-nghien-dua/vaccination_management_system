package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import ut.edu.vaccinationmanagementsystem.entity.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


public class CustomOAuth2User implements OAuth2User {
    
    private final OAuth2User oAuth2User;
    private final User user;
    
    public CustomOAuth2User(OAuth2User oAuth2User, User user) {
        this.oAuth2User = oAuth2User;
        this.user = user;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Trả về role của user
        if (user.getRole() == null) {
            // Nếu role null, trả về role CUSTOMER mặc định
            return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
            );
        }
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
    
    @Override
    public String getName() {
        return oAuth2User.getName();
    }
    
    public User getUser() {
        return user;
    }
}





