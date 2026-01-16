package ut.edu.vaccinationmanagementsystem.service;

/**
 * Interface cho dịch vụ gửi SMS
 * Có thể implement bằng Mock (development) hoặc Twilio/Real SMS (production)
 */
public interface SmsService {
    /**
     * Gửi SMS đến số điện thoại
     * @param phoneNumber Số điện thoại người nhận
     * @param message Nội dung tin nhắn
     * @throws SmsException Nếu gửi SMS thất bại
     */
    void sendSms(String phoneNumber, String message) throws SmsException;
}


