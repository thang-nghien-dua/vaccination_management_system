package ut.edu.vaccinationmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateVaccinationRecordDTO {
    private Long appointmentId;
    private Long vaccineLotId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate injectionDate;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime injectionTime;
    
    private String injectionSite;
    private Double doseAmount;
    
    // Getters and Setters
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public Long getVaccineLotId() {
        return vaccineLotId;
    }
    
    public void setVaccineLotId(Long vaccineLotId) {
        this.vaccineLotId = vaccineLotId;
    }
    
    public LocalDate getInjectionDate() {
        return injectionDate;
    }
    
    public void setInjectionDate(LocalDate injectionDate) {
        this.injectionDate = injectionDate;
    }
    
    public LocalTime getInjectionTime() {
        return injectionTime;
    }
    
    public void setInjectionTime(LocalTime injectionTime) {
        this.injectionTime = injectionTime;
    }
    
    public String getInjectionSite() {
        return injectionSite;
    }
    
    public void setInjectionSite(String injectionSite) {
        this.injectionSite = injectionSite;
    }
    
    public Double getDoseAmount() {
        return doseAmount;
    }
    
    public void setDoseAmount(Double doseAmount) {
        this.doseAmount = doseAmount;
    }
}
