package ut.edu.vaccinationmanagementsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mock SMS Service - In mã OTP ra console/log
 * Dùng cho development và testing
 */
@Service
public class MockSmsService implements SmsService {
    
    private static final Logger log = LoggerFactory.getLogger(MockSmsService.class);
    
    @Override
    public void sendSms(String phoneNumber, String message) throws SmsException {
        // In ra console/log thay vì gửi SMS thật
        log.info("═══════════════════════════════════════════════════════");
        log.info("📱 [MOCK SMS] Gửi tin nhắn đến: {}", phoneNumber);
        log.info("📱 [MOCK SMS] Nội dung: {}", message);
        log.info("═══════════════════════════════════════════════════════");
        
        // Có thể thêm logic lưu vào database để test nếu cần
        // Hoặc gửi email thay thế trong development
    }
}


