package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.AdverseReaction;

import java.util.List;

@Repository
public interface AdverseReactionRepository extends JpaRepository<AdverseReaction, Long> {
    /**
     * Tìm tất cả phản ứng phụ của một vaccination record
     */
    List<AdverseReaction> findByVaccinationRecordId(Long vaccinationRecordId);
    
    /**
     * Tìm tất cả phản ứng phụ chưa được xử lý (resolved = false)
     */
    @Query("SELECT ar FROM AdverseReaction ar WHERE ar.resolved = false ORDER BY ar.occurredAt DESC")
    List<AdverseReaction> findUnresolvedReactions();
    
    /**
     * Tìm tất cả phản ứng phụ đã được xử lý bởi một user cụ thể
     */
    List<AdverseReaction> findByHandledById(Long handledById);
}

