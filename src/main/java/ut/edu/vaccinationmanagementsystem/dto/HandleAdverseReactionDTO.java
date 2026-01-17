package ut.edu.vaccinationmanagementsystem.dto;

public class HandleAdverseReactionDTO {
    private String treatment;
    private String notes;
    private Boolean resolved;
    
    // Getters and Setters
    public String getTreatment() {
        return treatment;
    }
    
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Boolean getResolved() {
        return resolved;
    }
    
    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }
}
