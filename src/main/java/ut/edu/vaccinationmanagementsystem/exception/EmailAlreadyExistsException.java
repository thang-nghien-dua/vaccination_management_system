package ut.edu.vaccinationmanagementsystem.exception;

import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;

/**
 * Exception khi email đã tồn tại
 */
public class EmailAlreadyExistsException extends RuntimeException {
    private final UserStatus userStatus;
    private final String email;
    
    public EmailAlreadyExistsException(String email, UserStatus userStatus) {
        super("Email already exists: " + email);
        this.email = email;
        this.userStatus = userStatus;
    }
    
    public UserStatus getUserStatus() {
        return userStatus;
    }
    
    public String getEmail() {
        return email;
    }
}




