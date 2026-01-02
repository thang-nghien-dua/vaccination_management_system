package ut.edu.vaccinationmanagement_system.entity;

import jakarta.persistence.*;
import ut.edu.vaccinationmanagement_system.entity.enums.ReactionType;

import java.time.LocalDateTime;

/**
 * Phản ứng phụ sau tiêm vaccine
 */
@Entity
@Table(name = "adverse_reaction")
public class AdverseReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID tự động tăng
    
    @ManyToOne
    @JoinColumn(name = "vaccination_record_id", nullable = false)
    private VaccinationRecord vaccinationRecord; // Hồ sơ tiêm chủng liên quan
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType reactionType; // Loại phản ứng (MILD, MODERATE, SEVERE)
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String symptoms; // Triệu chứng phản ứng
    
    @Column(nullable = false)
    private LocalDateTime occurredAt; // Thời gian xảy ra phản ứng
    
    @ManyToOne
    @JoinColumn(name = "handled_by", nullable = true)
    private User handledBy; // Bác sĩ xử lý phản ứng
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String treatment; // Phương pháp điều trị
    
    @Column(nullable = false)
    private Boolean resolved; // Đã giải quyết chưa
    
    @Column(nullable = true, columnDefinition = "TEXT")
    private String notes; // Ghi chú
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    
    public VaccinationRecord getVaccinationRecord() {
        return vaccinationRecord;
    }
    
    public void setVaccinationRecord(VaccinationRecord vaccinationRecord) {
        this.vaccinationRecord = vaccinationRecord;
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
    
    public User getHandledBy() {
        return handledBy;
    }
    
    public void setHandledBy(User handledBy) {
        this.handledBy = handledBy;
    }
    
    public String getTreatment() {
        return treatment;
    }
    
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
    
    public Boolean getResolved() {
        return resolved;
    }
    
    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}

