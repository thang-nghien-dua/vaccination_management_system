package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.AdverseReaction;

import java.util.List;

@Repository
public interface AdverseReactionRepository extends JpaRepository<AdverseReaction, Long> {

    List<AdverseReaction> findByVaccinationRecordId(Long vaccinationRecordId);

    @Query("SELECT ar FROM AdverseReaction ar WHERE ar.resolved = false ORDER BY ar.occurredAt DESC")
    List<AdverseReaction> findUnresolvedReactions();

    List<AdverseReaction> findByHandledById(Long handledById);

    @Query("SELECT DISTINCT ar FROM AdverseReaction ar " +
           "LEFT JOIN FETCH ar.vaccinationRecord vr " +
           "LEFT JOIN FETCH vr.user " +
           "LEFT JOIN FETCH vr.vaccine " +
           "LEFT JOIN FETCH vr.appointment a " +
           "LEFT JOIN FETCH a.center " +
           "LEFT JOIN FETCH ar.handledBy " +
           "ORDER BY ar.occurredAt DESC")
    List<AdverseReaction> findAllWithRelationships();
}

