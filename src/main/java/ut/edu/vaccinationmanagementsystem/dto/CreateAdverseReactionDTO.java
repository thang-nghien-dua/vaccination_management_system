package ut.edu.vaccinationmanagementsystem.dto;

import ut.edu.vaccinationmanagementsystem.entity.enums.ReactionType;

import java.time.LocalDateTime;

public class CreateAdverseReactionDTO {
    private Long vaccinationRecordId;
    private ReactionType reactionType;
    private String symptoms;
    private LocalDateTime occurredAt;
    private String notes;
    
    // Getters and Setters
    public Long getVaccinationRecordId() {
        return vaccinationRecordId;
    }
    
    public void setVaccinationRecordId(Long vaccinationRecordId) {
        this.vaccinationRecordId = vaccinationRecordId;
    }
    
    public ReactionType getReactionType() {
        return reactionType;
    }
    
    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
