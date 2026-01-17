package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.CreateAdverseReactionDTO;
import ut.edu.vaccinationmanagementsystem.dto.HandleAdverseReactionDTO;
import ut.edu.vaccinationmanagementsystem.entity.AdverseReaction;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.VaccinationRecord;
import ut.edu.vaccinationmanagementsystem.repository.AdverseReactionRepository;
import ut.edu.vaccinationmanagementsystem.repository.VaccinationRecordRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service xử lý logic cho AdverseReaction
 */
@Service
@Transactional
public class AdverseReactionService {
    
    @Autowired
    private AdverseReactionRepository adverseReactionRepository;
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    /**
     * Tạo phản ứng phụ mới
     */
    public AdverseReaction createAdverseReaction(CreateAdverseReactionDTO dto) {
        VaccinationRecord record = vaccinationRecordRepository.findById(dto.getVaccinationRecordId())
                .orElseThrow(() -> new RuntimeException("Vaccination record not found"));
        
        AdverseReaction reaction = new AdverseReaction();
        reaction.setVaccinationRecord(record);
        reaction.setReactionType(dto.getReactionType());
        reaction.setSymptoms(dto.getSymptoms());
        reaction.setOccurredAt(dto.getOccurredAt() != null ? dto.getOccurredAt() : LocalDateTime.now());
        reaction.setNotes(dto.getNotes());
        reaction.setResolved(false); // Mặc định chưa giải quyết
        reaction.setHandledBy(null); // Chưa có người xử lý
        reaction.setTreatment(null); // Chưa có phương pháp điều trị
        
        return adverseReactionRepository.save(reaction);
    }
    
    /**
     * Lấy phản ứng phụ theo ID
     */
    public AdverseReaction getAdverseReactionById(Long id) {
        return adverseReactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adverse reaction not found"));
    }
    
    /**
     * Xử lý phản ứng phụ (bởi bác sĩ)
     */
    public AdverseReaction handleAdverseReaction(Long id, HandleAdverseReactionDTO dto, User handledBy) {
        AdverseReaction reaction = getAdverseReactionById(id);
        
        reaction.setHandledBy(handledBy);
        reaction.setTreatment(dto.getTreatment());
        reaction.setNotes(dto.getNotes() != null ? dto.getNotes() : reaction.getNotes());
        reaction.setResolved(dto.getResolved() != null ? dto.getResolved() : false);
        
        return adverseReactionRepository.save(reaction);
    }
    
    /**
     * Lấy danh sách phản ứng phụ của một vaccination record
     */
    public List<AdverseReaction> getAdverseReactionsByVaccinationRecordId(Long vaccinationRecordId) {
        return adverseReactionRepository.findByVaccinationRecordId(vaccinationRecordId);
    }
    
    /**
     * Lấy danh sách phản ứng phụ chưa được xử lý
     */
    public List<AdverseReaction> getUnresolvedReactions() {
        return adverseReactionRepository.findUnresolvedReactions();
    }
}
