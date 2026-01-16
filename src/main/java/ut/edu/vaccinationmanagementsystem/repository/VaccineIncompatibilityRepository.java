package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.VaccineIncompatibility;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaccineIncompatibilityRepository extends JpaRepository<VaccineIncompatibility, Long> {
    
    /**
     * Tìm incompatibility giữa 2 vaccine (check cả 2 chiều)
     * @param vaccine1Id ID của vaccine 1
     * @param vaccine2Id ID của vaccine 2
     * @return VaccineIncompatibility nếu có
     */
    @Query("SELECT vi FROM VaccineIncompatibility vi WHERE " +
           "((vi.vaccine1.id = :vaccine1Id AND vi.vaccine2.id = :vaccine2Id) OR " +
           "(vi.vaccine1.id = :vaccine2Id AND vi.vaccine2.id = :vaccine1Id))")
    Optional<VaccineIncompatibility> findIncompatibility(
            @Param("vaccine1Id") Long vaccine1Id,
            @Param("vaccine2Id") Long vaccine2Id
    );
    
    /**
     * Tìm tất cả vaccine không tương thích với một vaccine
     * @param vaccineId ID của vaccine
     * @return Danh sách incompatibility
     */
    @Query("SELECT vi FROM VaccineIncompatibility vi WHERE " +
           "vi.vaccine1.id = :vaccineId OR vi.vaccine2.id = :vaccineId")
    List<VaccineIncompatibility> findAllIncompatibleWithVaccine(@Param("vaccineId") Long vaccineId);
}


