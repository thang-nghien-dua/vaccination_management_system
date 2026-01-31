package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ut.edu.vaccinationmanagementsystem.entity.EmailVerificationToken;
import ut.edu.vaccinationmanagementsystem.entity.PasswordResetToken;

/**
 * Service để gửi email
 */
@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${spring.mail.username:noreply@tiemchung.gov.vn}")
    private String fromEmail;
    
    /**
     * Gửi email xác thực
     */
    public void sendVerificationEmail(EmailVerificationToken token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(token.getUser().getEmail());
            message.setSubject("Xác thực email đăng ký tài khoản - Hệ thống Tiêm chủng VacciCare");
            
            String verificationUrl = baseUrl + "/api/auth/verify-email?token=" + token.getToken();
            
            String emailContent = "Xin chào " + token.getUser().getFullName() + ",\n\n" +
                    "Cảm ơn bạn đã đăng ký tài khoản tại Hệ thống Tiêm chủng VacciCare.\n\n" +
                    "Vui lòng click vào link sau để xác thực email của bạn:\n" +
                    verificationUrl + "\n\n" +
                    "Link này sẽ hết hạn sau 24 giờ.\n\n" +
                    "Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\n" +
                    "Hệ thống Tiêm chủng VacciCare";
            
            message.setText(emailContent);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error nhưng không throw exception để không làm gián đoạn quá trình đăng ký
            System.err.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gửi email xác thực lại (resend)
     */
    public void resendVerificationEmail(EmailVerificationToken token) {
        sendVerificationEmail(token);
    }
    
    /**
     * Gửi email reset password
     */
    public void sendPasswordResetEmail(PasswordResetToken token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(token.getUser().getEmail());
            message.setSubject("Đặt lại mật khẩu - Hệ thống Tiêm chủng VacciCare");
            
            String resetUrl = baseUrl + "/reset-password?token=" + token.getToken();
            
            String emailContent = "Xin chào " + token.getUser().getFullName() + ",\n\n" +
                    "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản tại Hệ thống Tiêm chủng VacciCare.\n\n" +
                    "Vui lòng click vào link sau để đặt lại mật khẩu:\n" +
                    resetUrl + "\n\n" +
                    "Link này sẽ hết hạn sau 1 giờ.\n\n" +
                    "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và mật khẩu của bạn sẽ không thay đổi.\n\n" +
                    "Trân trọng,\n" +
                    "Hệ thống Tiêm chủng VacciCare";
            
            message.setText(emailContent);
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error nhưng không throw exception
            System.err.println("Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gửi email đơn giản
     */
    public void sendEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            // Log error nhưng không throw exception
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

