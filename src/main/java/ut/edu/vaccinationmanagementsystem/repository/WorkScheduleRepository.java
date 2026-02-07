package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.entity.WorkSchedule;

import java.util.List;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    List<WorkSchedule> findByUser(User user);

    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.user.id = :userId")
    List<WorkSchedule> findByUserId(@Param("userId") Long userId);
}

