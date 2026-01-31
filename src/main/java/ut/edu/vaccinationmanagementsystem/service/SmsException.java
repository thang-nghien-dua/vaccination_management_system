package ut.edu.vaccinationmanagementsystem.service;

/**
 * Exception khi gửi SMS thất bại
 */
public class SmsException extends Exception {
    public SmsException(String message) {
        super(message);
    }

    public SmsException(String message, Throwable cause) {
        super(message, cause);
    }
}


