package ut.edu.vaccinationmanagementsystem.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO cho VaccinationRecord (tạo mới)
 */
public class VaccinationRecordDTO {
    private Long appointmentId;
    private Long vaccineLotId;
    private Long nurseId;
    private LocalDate injectionDate;
    private LocalTime injectionTime;
    private String injectionSite;
    private Double doseAmount;
    private String notes;
    
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
    
    public Long getNurseId() {
        return nurseId;
    }
    
    public void setNurseId(Long nurseId) {
        this.nurseId = nurseId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

