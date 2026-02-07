package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.FamilyMember;
import ut.edu.vaccinationmanagementsystem.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    List<FamilyMember> findByUserOrderByCreatedAtDesc(User user);

    Optional<FamilyMember> findByIdAndUser(Long id, User user);

    boolean existsByUserAndCitizenId(User user, String citizenId);

    long countByUser(User user);
}




