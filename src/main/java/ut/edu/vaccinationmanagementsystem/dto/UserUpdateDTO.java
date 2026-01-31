package ut.edu.vaccinationmanagementsystem.dto;

import ut.edu.vaccinationmanagementsystem.entity.enums.Gender;

import java.time.LocalDate;

/**
 * DTO cho cập nhật profile
 */
public class UserUpdateDTO {
    private String fullName;
    private String phoneNumber;
    private LocalDate dayOfBirth;
    private Gender gender;
    private String address;
    private String citizenId;
    
    // Getters and Setters
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public LocalDate getDayOfBirth() {
        return dayOfBirth;
    }
    
    public void setDayOfBirth(LocalDate dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCitizenId() {
        return citizenId;
    }
    
    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }
}





