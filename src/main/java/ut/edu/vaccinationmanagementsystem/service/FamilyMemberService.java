package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.FamilyMemberDTO;
import ut.edu.vaccinationmanagementsystem.entity.FamilyMember;
import ut.edu.vaccinationmanagementsystem.entity.User;
import ut.edu.vaccinationmanagementsystem.repository.FamilyMemberRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FamilyMemberService {
    
    @Autowired
    private FamilyMemberRepository familyMemberRepository;
    
    /**
     * Lấy danh sách người thân của user hiện tại
     */
    public List<FamilyMember> getFamilyMembersByUser(User user) {
        return familyMemberRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Lấy thông tin một người thân theo ID (chỉ lấy được nếu thuộc về user hiện tại)
     */
    public FamilyMember getFamilyMemberById(Long id, User user) {
        return familyMemberRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Family member not found or you don't have permission to access it"));
    }
    
    /**
     * Thêm người thân mới
     */
    @Transactional
    public FamilyMember createFamilyMember(FamilyMemberDTO dto, User user) {
        // Validate
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (dto.getRelationship() == null) {
            throw new RuntimeException("Relationship is required");
        }
        
        // Kiểm tra citizenId trùng (nếu có)
        if (dto.getCitizenId() != null && !dto.getCitizenId().trim().isEmpty()) {
            if (familyMemberRepository.existsByUserAndCitizenId(user, dto.getCitizenId().trim())) {
                throw new RuntimeException("Citizen ID already exists in your family members list");
            }
        }
        
        // Tạo FamilyMember mới
        FamilyMember familyMember = new FamilyMember();
        familyMember.setUser(user);
        familyMember.setFullName(dto.getFullName().trim());
        familyMember.setDateOfBirth(dto.getDateOfBirth());
        familyMember.setGender(dto.getGender());
        familyMember.setCitizenId(dto.getCitizenId() != null && !dto.getCitizenId().trim().isEmpty() ? dto.getCitizenId().trim() : null);
        familyMember.setPhoneNumber(dto.getPhoneNumber());
        familyMember.setRelationship(dto.getRelationship());
        familyMember.setCreatedAt(LocalDateTime.now());
        
        return familyMemberRepository.save(familyMember);
    }
    
    /**
     * Cập nhật thông tin người thân
     */
    @Transactional
    public FamilyMember updateFamilyMember(Long id, FamilyMemberDTO dto, User user) {
        // Lấy family member (đảm bảo thuộc về user hiện tại)
        FamilyMember familyMember = getFamilyMemberById(id, user);
        
        // Validate
        if (dto.getFullName() != null && dto.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name cannot be empty");
        }
        
        // Kiểm tra citizenId trùng (nếu có và khác với citizenId hiện tại)
        if (dto.getCitizenId() != null && !dto.getCitizenId().trim().isEmpty()) {
            if (!dto.getCitizenId().trim().equals(familyMember.getCitizenId()) 
                && familyMemberRepository.existsByUserAndCitizenId(user, dto.getCitizenId().trim())) {
                throw new RuntimeException("Citizen ID already exists in your family members list");
            }
        }
        
        // Cập nhật các field
        if (dto.getFullName() != null) {
            familyMember.setFullName(dto.getFullName().trim());
        }
        if (dto.getDateOfBirth() != null) {
            familyMember.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getGender() != null) {
            familyMember.setGender(dto.getGender());
        }
        if (dto.getCitizenId() != null) {
            familyMember.setCitizenId(dto.getCitizenId().trim().isEmpty() ? null : dto.getCitizenId().trim());
        }
        if (dto.getPhoneNumber() != null) {
            familyMember.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getRelationship() != null) {
            familyMember.setRelationship(dto.getRelationship());
        }
        
        return familyMemberRepository.save(familyMember);
    }
    
    /**
     * Xóa người thân
     */
    @Transactional
    public void deleteFamilyMember(Long id, User user) {
        // Lấy family member (đảm bảo thuộc về user hiện tại)
        FamilyMember familyMember = getFamilyMemberById(id, user);
        
        // Xóa
        familyMemberRepository.delete(familyMember);
    }
    
    /**
     * Đếm số lượng người thân của user
     */
    public long countFamilyMembersByUser(User user) {
        return familyMemberRepository.countByUser(user);
    }
}




