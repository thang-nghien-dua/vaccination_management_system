package ut.edu.vaccinationmanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ut.edu.vaccinationmanagementsystem.entity.Vaccine;

import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {

    //Tìm kiếm vaccine theo tên hoặc mã vaccine
    @Query("SELECT v FROM Vaccine v WHERE " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Vaccine> searchByKeyword(@Param("keyword") String keyword);
    
    //Tìm vaccine theo mã vaccine (duy nhất)
    Vaccine findByCode(String code);
    
    //Kiểm tra vaccine có tồn tại theo mã code không
    boolean existsByCode(String code);
}



