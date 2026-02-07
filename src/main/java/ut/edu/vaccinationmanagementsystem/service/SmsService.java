package ut.edu.vaccinationmanagementsystem.service;


public interface SmsService {

    void sendSms(String phoneNumber, String message) throws SmsException;
}


