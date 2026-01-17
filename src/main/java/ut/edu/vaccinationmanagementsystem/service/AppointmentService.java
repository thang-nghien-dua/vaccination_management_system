package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.BookAppointmentDTO;
import ut.edu.vaccinationmanagementsystem.dto.ConsultationRequestDTO;
import ut.edu.vaccinationmanagementsystem.dto.WalkInAppointmentDTO;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.PaymentMethod;
import ut.edu.vaccinationmanagementsystem.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FamilyMemberRepository familyMemberRepository;
    
    @Autowired
    private VaccineRepository vaccineRepository;
    
    @Autowired
    private VaccinationCenterRepository vaccinationCenterRepository;
    
    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;
    
    @Autowired
    private VaccineIncompatibilityRepository vaccineIncompatibilityRepository;
    
    @Autowired
    private PhoneVerificationService phoneVerificationService;
    
    @Autowired
    private CenterVaccineRepository centerVaccineRepository;
    
    @Autowired
    private VaccinationRecordRepository vaccinationRecordRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AppointmentHistoryRepository appointmentHistoryRepository;
    
    @Autowired
    private ClinicRoomRepository clinicRoomRepository;
    
    /**
     * Tạo consultation request (yêu cầu tư vấn)
     * Hỗ trợ cả user đã đăng nhập và guest chưa đăng nhập
     */
    public Appointment createConsultationRequest(ConsultationRequestDTO dto) {
        Appointment appointment = new Appointment();
        
        // Generate booking code
        String bookingCode = generateBookingCode();
        appointment.setBookingCode(bookingCode);
        
        // Set requiresConsultation = true
        appointment.setRequiresConsultation(true);
        appointment.setStatus(AppointmentStatus.PENDING);
        
        // Get current user (if authenticated)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                    CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                    currentUser = customOAuth2User.getUser();
                } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                    currentUser = customUserDetails.getUser();
                } else {
                    String email = authentication.getName();
                    Optional<User> userOpt = userRepository.findByEmail(email);
                    if (userOpt.isPresent()) {
                        currentUser = userOpt.get();
                    }
                }
            } catch (Exception e) {
                // User not found, treat as guest
            }
        }
        
        if (currentUser != null) {
            // User đã đăng nhập
            appointment.setBookedByUser(currentUser);
            
            // Set bookedForUser
            if (dto.getBookedForUserId() != null) {
                // Đặt cho người thân
                FamilyMember familyMember = familyMemberRepository.findById(dto.getBookedForUserId())
                        .orElseThrow(() -> new RuntimeException("Family member not found"));
                if (!familyMember.getUser().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("Family member does not belong to current user");
                }
                // Note: bookedForUser sẽ là User, không phải FamilyMember
                // Cần tạo User từ FamilyMember hoặc lưu thông tin vào notes
                appointment.setBookedForUser(null); // Tạm thời null, có thể cần entity riêng
                appointment.setNotes("Đặt cho người thân: " + familyMember.getFullName() + ". " + 
                                   (dto.getReason() != null ? "Lý do: " + dto.getReason() + ". " : "") +
                                   (dto.getNotes() != null ? "Ghi chú: " + dto.getNotes() : ""));
            } else {
                // Đặt cho bản thân
                appointment.setBookedForUser(null);
                appointment.setNotes((dto.getReason() != null ? "Lý do: " + dto.getReason() + ". " : "") +
                                   (dto.getNotes() != null ? "Ghi chú: " + dto.getNotes() : ""));
            }
        } else {
            // Guest chưa đăng nhập
            appointment.setBookedByUser(null);
            appointment.setBookedForUser(null);
            appointment.setGuestFullName(dto.getGuestFullName());
            appointment.setGuestEmail(dto.getGuestEmail());
            appointment.setGuestDayOfBirth(dto.getGuestDayOfBirth());
            appointment.setGuestGender(dto.getGuestGender());
            appointment.setNotes((dto.getReason() != null ? "Lý do: " + dto.getReason() + ". " : "") +
                               (dto.getNotes() != null ? "Ghi chú: " + dto.getNotes() : ""));
        }
        
        // Set vaccine (nullable)
        if (dto.getVaccineId() != null) {
            Vaccine vaccine = vaccineRepository.findById(dto.getVaccineId())
                    .orElseThrow(() -> new RuntimeException("Vaccine not found"));
            appointment.setVaccine(vaccine);
        }
        
        // Set consultation phone
        appointment.setConsultationPhone(dto.getConsultationPhone());
        
        // Center và slot sẽ null, lễ tân sẽ cập nhật sau khi tư vấn
        appointment.setCenter(null);
        appointment.setSlot(null);
        appointment.setAppointmentDate(null);
        appointment.setAppointmentTime(null);
        
        // Set dose number (mặc định 1)
        appointment.setDoseNumber(1);
        
        // Set timestamps
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Đặt lịch trực tiếp (không cần tư vấn)
     * Chỉ dành cho user đã đăng nhập
     * @return Appointment đã tạo
     */
    public Appointment bookAppointment(BookAppointmentDTO dto) {
        // Validate required fields
        if (dto.getVaccineId() == null) {
            throw new RuntimeException("Vaccine ID is required");
        }
        if (dto.getCenterId() == null) {
            throw new RuntimeException("Center ID is required");
        }
        if (dto.getSlotId() == null) {
            throw new RuntimeException("Slot ID is required");
        }
        
        // Get current user (must be authenticated)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                    CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                    currentUser = customOAuth2User.getUser();
                } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                    currentUser = customUserDetails.getUser();
                } else {
                    String email = authentication.getName();
                    Optional<User> userOpt = userRepository.findByEmail(email);
                    if (userOpt.isPresent()) {
                        currentUser = userOpt.get();
                    }
                }
            } catch (Exception e) {
                // User not found
            }
        }
        
        if (currentUser == null) {
            throw new RuntimeException("User must be authenticated to book appointment");
        }
        
        // Validate vaccine exists
        Vaccine vaccine = vaccineRepository.findById(dto.getVaccineId())
                .orElseThrow(() -> new RuntimeException("Vaccine not found"));
        
        // Validate center exists
        VaccinationCenter center = vaccinationCenterRepository.findById(dto.getCenterId())
                .orElseThrow(() -> new RuntimeException("Vaccination center not found"));
        
        // Validate slot exists and is available
        AppointmentSlot slot = appointmentSlotRepository.findById(dto.getSlotId())
                .orElseThrow(() -> new RuntimeException("Appointment slot not found"));
        
        // Check slot belongs to center
        if (!slot.getCenter().getId().equals(center.getId())) {
            throw new RuntimeException("Slot does not belong to selected center");
        }
        
        // Check slot is available
        if (!slot.getIsAvailable() || slot.getCurrentBookings() >= slot.getMaxCapacity()) {
            throw new RuntimeException("Slot is no longer available");
        }
        
        // Check slot date is not in the past
        if (slot.getDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot book appointment for past date");
        }
        
        // Check slot time is not in the past (nếu cùng ngày)
        if (slot.getDate().equals(LocalDate.now()) && slot.getStartTime().isBefore(LocalTime.now())) {
            throw new RuntimeException("Không thể đặt lịch cho slot đã qua trong ngày");
        }
        
        // Set bookedForUser and familyMember if booking for family member
        User bookedForUser = null;
        FamilyMember familyMember = null;
        Long userIdToCheck = null; // User ID cần check duplicate
        Long familyMemberIdToCheck = null; // FamilyMember ID cần check duplicate
        
        if (dto.getBookedForUserId() != null) {
            // Đặt cho người thân
            familyMember = familyMemberRepository.findById(dto.getBookedForUserId())
                    .orElseThrow(() -> new RuntimeException("Family member not found"));
            if (!familyMember.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Family member does not belong to current user");
            }
            // Lưu familyMember để check duplicate
            familyMemberIdToCheck = familyMember.getId();
            bookedForUser = null; // Đặt cho người thân nên bookedForUser = null

            // Validate phone verification cho người thân
            validatePhoneVerificationForFamilyMember(familyMember, currentUser, dto.getPhoneNumber());
        } else {
            // Đặt cho bản thân
            userIdToCheck = currentUser.getId();
            bookedForUser = currentUser; // Đặt bookedForUser = currentUser khi đặt cho bản thân

            // Validate phone verification cho bản thân
            validatePhoneVerificationForUser(currentUser, dto.getPhoneNumber());
        }
        
        // Validate: Kiểm tra xem user đã có lịch trong slot này chưa
        // Logic mới: Cho phép đặt nhiều vaccine TƯƠNG THÍCH trong cùng slot
        // Chỉ chặn nếu: (1) Trùng vaccine, hoặc (2) Vaccine không tương thích
        if (userIdToCheck != null) {
            List<Appointment> existingAppointments = appointmentRepository.findExistingAppointmentsByUserAndSlot(
                    userIdToCheck,
                    dto.getSlotId(),
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
            );
            
            // Tối ưu: Load tất cả incompatibilities của vaccine mới một lần thay vì query trong vòng lặp
            List<VaccineIncompatibility> vaccineIncompatibilities = 
                    vaccineIncompatibilityRepository.findAllIncompatibleWithVaccine(vaccine.getId());
            
            // Tạo Map để lookup nhanh: vaccineId -> incompatibility
            Map<Long, VaccineIncompatibility> incompatibilityMap = new HashMap<>();
            for (VaccineIncompatibility vi : vaccineIncompatibilities) {
                Long otherVaccineId = vi.getVaccine1().getId().equals(vaccine.getId()) 
                    ? vi.getVaccine2().getId() 
                    : vi.getVaccine1().getId();
                incompatibilityMap.put(otherVaccineId, vi);
            }
            
            // Check từng appointment trong slot này
            for (Appointment existingAppt : existingAppointments) {
                if (existingAppt.getVaccine() == null) {
                    continue; // Bỏ qua nếu không có vaccine
                }
                
                Long existingVaccineId = existingAppt.getVaccine().getId();
                
                // Nếu trùng vaccine → chặn
                if (existingVaccineId.equals(vaccine.getId())) {
                    throw new RuntimeException(
                            String.format("Bạn đã đặt vaccine '%s' trong khung giờ này rồi. Vui lòng chọn khung giờ khác.", 
                                    vaccine.getName())
                    );
                }
                
                // Nếu vaccine khác nhau → check incompatibility từ Map (đã load sẵn)
                VaccineIncompatibility incompatibility = incompatibilityMap.get(existingVaccineId);
                if (incompatibility != null) {
                    throw new RuntimeException(
                            String.format(
                                    "Vaccine '%s' không thể tiêm cùng lúc với vaccine '%s' đã đặt trong khung giờ này. " +
                                    "Hai vaccine này cần cách nhau ít nhất %d ngày.",
                                    vaccine.getName(),
                                    existingAppt.getVaccine().getName(),
                                    incompatibility.getMinDaysBetween()
                            )
                    );
                }
                // Nếu không có incompatibility → cho phép (có thể tiêm cùng lúc)
            }
        }
        
        // Validate: Kiểm tra xem người thân đã có lịch trong slot này chưa
        if (familyMemberIdToCheck != null) {
            List<Appointment> existingAppointments = appointmentRepository.findExistingAppointmentsByFamilyMemberAndSlot(
                    familyMemberIdToCheck,
                    dto.getSlotId(),
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
            );
            
            // Tối ưu: Load tất cả incompatibilities của vaccine mới một lần thay vì query trong vòng lặp
            List<VaccineIncompatibility> vaccineIncompatibilities = 
                    vaccineIncompatibilityRepository.findAllIncompatibleWithVaccine(vaccine.getId());
            
            // Tạo Map để lookup nhanh: vaccineId -> incompatibility
            Map<Long, VaccineIncompatibility> incompatibilityMap = new HashMap<>();
            for (VaccineIncompatibility vi : vaccineIncompatibilities) {
                Long otherVaccineId = vi.getVaccine1().getId().equals(vaccine.getId()) 
                    ? vi.getVaccine2().getId() 
                    : vi.getVaccine1().getId();
                incompatibilityMap.put(otherVaccineId, vi);
            }
            
            // Check từng appointment trong slot này
            for (Appointment existingAppt : existingAppointments) {
                if (existingAppt.getVaccine() == null) {
                    continue; // Bỏ qua nếu không có vaccine
                }
                
                Long existingVaccineId = existingAppt.getVaccine().getId();
                
                // Nếu trùng vaccine → chặn
                if (existingVaccineId.equals(vaccine.getId())) {
                    throw new RuntimeException(
                            String.format("Người này đã đặt vaccine '%s' trong khung giờ này rồi. Vui lòng chọn khung giờ khác.", 
                                    vaccine.getName())
                    );
                }
                
                // Nếu vaccine khác nhau → check incompatibility từ Map (đã load sẵn)
                VaccineIncompatibility incompatibility = incompatibilityMap.get(existingVaccineId);
                if (incompatibility != null) {
                    throw new RuntimeException(
                            String.format(
                                    "Vaccine '%s' không thể tiêm cùng lúc với vaccine '%s' đã đặt trong khung giờ này. " +
                                    "Hai vaccine này cần cách nhau ít nhất %d ngày.",
                                    vaccine.getName(),
                                    existingAppt.getVaccine().getName(),
                                    incompatibility.getMinDaysBetween()
                            )
                    );
                }
                // Nếu không có incompatibility → cho phép (có thể tiêm cùng lúc)
            }
        }
        
        // Tự động tính doseNumber dựa trên lịch sử tiêm chủng
        Integer doseNumber = calculateNextDoseNumber(vaccine, userIdToCheck, familyMemberIdToCheck);
        
        // Validate: Kiểm tra doseNumber (đã tiêm đủ mũi chưa, có hợp lệ không)
        validateDoseNumber(vaccine, doseNumber, userIdToCheck, familyMemberIdToCheck);
        
        // Validate: Kiểm tra vaccine incompatibility và days between doses (cho các appointment khác ngày)
        validateVaccineCompatibility(vaccine, slot.getDate(), doseNumber, userIdToCheck, familyMemberIdToCheck);
        
        // Validate: Kiểm tra stock quantity (chỉ check, không trừ - sẽ trừ khi tiêm thành công)
        Optional<CenterVaccine> centerVaccineOpt = centerVaccineRepository.findByCenterAndVaccine(center, vaccine);
        if (centerVaccineOpt.isEmpty()) {
            throw new RuntimeException("Vaccine không có tại trung tâm này");
        }
        CenterVaccine centerVaccine = centerVaccineOpt.get();
        if (centerVaccine.getStockQuantity() == null || centerVaccine.getStockQuantity() <= 0) {
            throw new RuntimeException("Vaccine hiện đang hết hàng tại trung tâm này. Vui lòng chọn trung tâm khác hoặc vaccine khác.");
        }
        
        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setBookingCode(generateBookingCode());
        appointment.setBookedByUser(currentUser);
        appointment.setBookedForUser(bookedForUser);
        appointment.setFamilyMember(familyMember); // Set familyMember nếu đặt cho người thân
        appointment.setVaccine(vaccine);
        appointment.setCenter(center);
        appointment.setSlot(slot);
        appointment.setAppointmentDate(slot.getDate());
        appointment.setAppointmentTime(slot.getStartTime());
        appointment.setRequiresConsultation(false); // Đặt trực tiếp, không cần tư vấn
        appointment.setStatus(AppointmentStatus.CONFIRMED); // Trạng thái đã xác nhận (tự động xác nhận)
        appointment.setDoseNumber(doseNumber); // Sử dụng doseNumber đã được tính tự động
        appointment.setNotes(dto.getNotes());
        
        // Lấy phòng từ slot (nếu slot có phòng)
        if (slot.getRoom() != null) {
            appointment.setRoom(slot.getRoom());
        }
        
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        
        // Update slot booking count
        slot.setCurrentBookings(slot.getCurrentBookings() + 1);
        if (slot.getCurrentBookings() >= slot.getMaxCapacity()) {
            slot.setIsAvailable(false);
        }
        appointmentSlotRepository.save(slot);
        
        // Save appointment first to get ID
        appointment = appointmentRepository.save(appointment);
        
        // Create Payment
        PaymentMethod paymentMethod = dto.getPaymentMethod() != null 
            ? PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase())
            : PaymentMethod.CASH; // Default to CASH
        
        Payment payment = paymentService.createPayment(appointment, paymentMethod);
        appointment.setPayment(payment);
        
        appointment = appointmentRepository.save(appointment);
        
        // Tạo thông báo đặt lịch thành công
        try {
            notificationService.createAppointmentCreatedNotification(appointment);
        } catch (Exception e) {
            // Log error nhưng không throw để không làm gián đoạn quá trình đặt lịch
            System.err.println("Failed to create notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return appointment;
    }
    
    /**
     * Tính tự động mũi tiếp theo dựa trên lịch sử tiêm chủng
     * 
     * @param vaccine Vaccine muốn đặt
     * @param userIdToCheck User ID cần check (null nếu đặt cho người thân)
     * @param familyMemberIdToCheck FamilyMember ID cần check (null nếu đặt cho bản thân)
     * @return Mũi tiếp theo (1 nếu chưa tiêm mũi nào)
     */
    private Integer calculateNextDoseNumber(Vaccine vaccine, Long userIdToCheck, Long familyMemberIdToCheck) {
        // Lấy danh sách vaccination records đã tiêm (COMPLETED)
        List<VaccinationRecord> completedRecords;
        List<Appointment> pendingAppointments;
        
        if (userIdToCheck != null) {
            // Check cho bản thân
            User user = userRepository.findById(userIdToCheck).orElse(null);
            if (user == null) {
                return 1; // Chưa có thông tin, mặc định mũi 1
            }
            completedRecords = vaccinationRecordRepository.findByUserAndVaccine(user, vaccine);
            pendingAppointments = appointmentRepository.findByBookedForUserAndVaccineAndStatusIn(
                userIdToCheck, vaccine.getId(),
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));
        } else if (familyMemberIdToCheck != null) {
            // Check cho người thân
            FamilyMember familyMember = familyMemberRepository.findById(familyMemberIdToCheck).orElse(null);
            if (familyMember == null) {
                return 1; // Chưa có thông tin, mặc định mũi 1
            }
            completedRecords = vaccinationRecordRepository.findByAppointmentFamilyMemberIdAndVaccine(
                familyMemberIdToCheck, vaccine.getId());
            pendingAppointments = appointmentRepository.findByFamilyMemberAndVaccineAndStatusIn(
                familyMemberIdToCheck, vaccine.getId(),
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));
        } else {
            return 1; // Không có thông tin, mặc định mũi 1
        }
        
        // Tìm mũi cao nhất đã tiêm (từ VaccinationRecord)
        int maxCompletedDose = 0;
        for (VaccinationRecord record : completedRecords) {
            if (record.getDoseNumber() != null && record.getDoseNumber() > maxCompletedDose) {
                maxCompletedDose = record.getDoseNumber();
            }
        }
        
        // Tìm mũi cao nhất đã đặt nhưng chưa tiêm (từ Appointment PENDING/CONFIRMED)
        int maxPendingDose = 0;
        for (Appointment apt : pendingAppointments) {
            if (apt.getDoseNumber() != null && apt.getDoseNumber() > maxPendingDose) {
                maxPendingDose = apt.getDoseNumber();
            }
        }
        
        // Mũi tiếp theo = max(đã tiêm, đã đặt) + 1
        return Math.max(maxCompletedDose, maxPendingDose) + 1;
    }
    
    /**
     * Validate doseNumber: kiểm tra mũi thứ có hợp lệ không
     * - Không được vượt quá dosesRequired
     * - Không được đặt nếu đã tiêm đủ mũi
     * 
     * @param vaccine Vaccine muốn đặt
     * @param newDoseNumber Mũi thứ mấy muốn đặt (đã được tính tự động)
     * @param userIdToCheck User ID cần check (null nếu đặt cho người thân)
     * @param familyMemberIdToCheck FamilyMember ID cần check (null nếu đặt cho bản thân)
     */
    private void validateDoseNumber(Vaccine vaccine, Integer newDoseNumber, 
                                    Long userIdToCheck, Long familyMemberIdToCheck) {
        // Kiểm tra doseNumber không được <= 0
        if (newDoseNumber == null || newDoseNumber <= 0) {
            throw new RuntimeException("Mũi tiêm phải lớn hơn 0");
        }
        
        // Kiểm tra doseNumber không được vượt quá dosesRequired
        if (vaccine.getDosesRequired() != null && newDoseNumber > vaccine.getDosesRequired()) {
            throw new RuntimeException(
                String.format("Vaccine '%s' chỉ cần %d mũi. Bạn không thể đặt mũi thứ %d.", 
                    vaccine.getName(), vaccine.getDosesRequired(), newDoseNumber)
            );
        }
        
        // Lấy danh sách vaccination records đã tiêm (COMPLETED) để kiểm tra đã tiêm đủ mũi chưa
        List<VaccinationRecord> completedRecords;
        
        if (userIdToCheck != null) {
            // Check cho bản thân
            User user = userRepository.findById(userIdToCheck).orElse(null);
            if (user == null) {
                return; // Không có thông tin để check
            }
            completedRecords = vaccinationRecordRepository.findByUserAndVaccine(user, vaccine);
        } else if (familyMemberIdToCheck != null) {
            // Check cho người thân
            FamilyMember familyMember = familyMemberRepository.findById(familyMemberIdToCheck).orElse(null);
            if (familyMember == null) {
                return; // Không có thông tin để check
            }
            completedRecords = vaccinationRecordRepository.findByAppointmentFamilyMemberIdAndVaccine(
                familyMemberIdToCheck, vaccine.getId());
        } else {
            return; // Không có thông tin để check
        }
        
        // Tìm mũi cao nhất đã tiêm (từ VaccinationRecord)
        int maxCompletedDose = 0;
        for (VaccinationRecord record : completedRecords) {
            if (record.getDoseNumber() != null && record.getDoseNumber() > maxCompletedDose) {
                maxCompletedDose = record.getDoseNumber();
            }
        }
        
        // Kiểm tra nếu đã tiêm đủ mũi
        if (vaccine.getDosesRequired() != null && maxCompletedDose >= vaccine.getDosesRequired()) {
            throw new RuntimeException(
                String.format("Bạn đã tiêm đủ %d mũi vaccine '%s'. Không thể đặt thêm mũi nào nữa.", 
                    vaccine.getDosesRequired(), vaccine.getName())
            );
        }
    }
    
    /**
     * Validate vaccine compatibility và days between doses
     * @param newVaccine Vaccine mới muốn đặt
     * @param appointmentDate Ngày đặt lịch
     * @param newDoseNumber Mũi thứ mấy của vaccine mới
     * @param userIdToCheck User ID cần check (null nếu đặt cho người thân)
     * @param familyMemberIdToCheck FamilyMember ID cần check (null nếu đặt cho bản thân)
     */
    private void validateVaccineCompatibility(Vaccine newVaccine, LocalDate appointmentDate, 
                                             Integer newDoseNumber,
                                             Long userIdToCheck, Long familyMemberIdToCheck) {
        // Tính khoảng thời gian cần check (60 ngày trước và sau)
        LocalDate startDate = appointmentDate.minusDays(60);
        LocalDate endDate = appointmentDate.plusDays(60);
        
        List<Appointment> recentAppointments;
        
        // Lấy danh sách appointments trong khoảng thời gian
        if (userIdToCheck != null) {
            // Check cho bản thân
            recentAppointments = appointmentRepository.findAppointmentsByUserInDateRange(
                    userIdToCheck,
                    startDate,
                    endDate,
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
            );
        } else if (familyMemberIdToCheck != null) {
            // Check cho người thân
            recentAppointments = appointmentRepository.findAppointmentsByFamilyMemberInDateRange(
                    familyMemberIdToCheck,
                    startDate,
                    endDate,
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
            );
        } else {
            return; // Không có thông tin để check
        }
        
        // Tối ưu: Load tất cả incompatibilities của vaccine mới một lần thay vì query trong vòng lặp
        List<VaccineIncompatibility> vaccineIncompatibilities = 
                vaccineIncompatibilityRepository.findAllIncompatibleWithVaccine(newVaccine.getId());
        
        // Tạo Map để lookup nhanh: vaccineId -> incompatibility
        Map<Long, VaccineIncompatibility> incompatibilityMap = new HashMap<>();
        for (VaccineIncompatibility vi : vaccineIncompatibilities) {
            Long otherVaccineId = vi.getVaccine1().getId().equals(newVaccine.getId()) 
                ? vi.getVaccine2().getId() 
                : vi.getVaccine1().getId();
            incompatibilityMap.put(otherVaccineId, vi);
        }
        
        // Check từng appointment
        for (Appointment existingAppt : recentAppointments) {
            if (existingAppt.getVaccine() == null) {
                continue; // Bỏ qua nếu không có vaccine
            }
            
            Vaccine existingVaccine = existingAppt.getVaccine();
            LocalDate existingDate = existingAppt.getAppointmentDate();
            
            if (existingDate == null) {
                continue; // Bỏ qua nếu không có ngày
            }
            
            // Tính số ngày giữa 2 appointment
            long daysBetween = Math.abs(ChronoUnit.DAYS.between(existingDate, appointmentDate));
            
            // 1. Check vaccine incompatibility từ Map (đã load sẵn)
            VaccineIncompatibility incompatibility = incompatibilityMap.get(existingVaccine.getId());
            
            if (incompatibility != null) {
                if (daysBetween < incompatibility.getMinDaysBetween()) {
                    throw new RuntimeException(
                            String.format(
                                    "Vaccine '%s' không thể tiêm trong vòng %d ngày sau vaccine '%s'. " +
                                    "Khoảng cách tối thiểu là %d ngày. " +
                                    "(Hiện tại cách nhau %d ngày)",
                                    newVaccine.getName(),
                                    incompatibility.getMinDaysBetween(),
                                    existingVaccine.getName(),
                                    incompatibility.getMinDaysBetween(),
                                    daysBetween
                            )
                    );
                }
            }
            
            // 2. Check days between doses (cùng vaccine, check khoảng cách giữa các mũi)
            if (existingVaccine.getId().equals(newVaccine.getId())) {
                // Cùng vaccine, check daysBetweenDoses
                Integer daysBetweenDoses = existingVaccine.getDaysBetweenDoses();
                
                if (daysBetweenDoses != null && daysBetweenDoses > 0) {
                    // Lấy dose number của appointment hiện tại
                    Integer existingDose = existingAppt.getDoseNumber() != null ? existingAppt.getDoseNumber() : 1;
                    
                    // Check nếu đây là mũi tiếp theo hoặc mũi trước đó
                    boolean isSequentialDose = Math.abs(newDoseNumber - existingDose) == 1;
                    
                    if (isSequentialDose && daysBetween < daysBetweenDoses) {
                        // Xác định mũi nào là mũi trước
                        Integer earlierDose = existingDose < newDoseNumber ? existingDose : newDoseNumber;
                        Integer laterDose = existingDose > newDoseNumber ? existingDose : newDoseNumber;
                        
                        throw new RuntimeException(
                                String.format(
                                        "Vaccine '%s' mũi %d cần cách mũi %d ít nhất %d ngày. " +
                                        "(Hiện tại cách nhau %d ngày)",
                                        newVaccine.getName(),
                                        laterDose,
                                        earlierDose,
                                        daysBetweenDoses,
                                        daysBetween
                                )
                        );
                    }
                }
            }
        }
    }
    
    /**
     * Validate phone verification cho user (đặt cho bản thân)
     * @param user User cần kiểm tra
     * @param phoneNumberFromForm Số điện thoại từ form đặt lịch (có thể null nếu không có)
     */
    private void validatePhoneVerificationForUser(User user, String phoneNumberFromForm) {
        // Nếu có số điện thoại từ form, kiểm tra số đó có được xác thực không
        if (phoneNumberFromForm != null && !phoneNumberFromForm.trim().isEmpty()) {
            boolean verified = phoneVerificationService.isPhoneVerifiedForUser(user.getId(), phoneNumberFromForm);
            if (!verified) {
                throw new RuntimeException("Số điện thoại chưa được xác thực. Vui lòng xác thực số điện thoại trước khi đặt lịch tiêm");
            }
            return; // Đã xác thực, không cần check tiếp
        }
        
        // Nếu không có số từ form, kiểm tra số trong database
        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập và xác thực số điện thoại trước khi đặt lịch tiêm");
        }
        
        if (user.getPhoneVerified() == null || !user.getPhoneVerified()) {
            throw new RuntimeException("Số điện thoại chưa được xác thực. Vui lòng xác thực số điện thoại trước khi đặt lịch tiêm");
        }
    }
    
    /**
     * Validate phone verification cho family member (đặt cho người thân)
     * @param familyMember Family member cần kiểm tra
     * @param currentUser User hiện tại
     * @param phoneNumberFromForm Số điện thoại từ form đặt lịch (có thể null nếu không có)
     */
    private void validatePhoneVerificationForFamilyMember(FamilyMember familyMember, User currentUser, String phoneNumberFromForm) {
        // Nếu có số điện thoại từ form
        if (phoneNumberFromForm != null && !phoneNumberFromForm.trim().isEmpty()) {
            // Kiểm tra số điện thoại trong form có được xác thực không
            // Có thể là số của family member hoặc số của user
            boolean familyMemberVerified = false;
            boolean userVerified = false;
            
            // Check family member phone
            if (familyMember.getPhoneNumber() != null && !familyMember.getPhoneNumber().trim().isEmpty()) {
                familyMemberVerified = phoneVerificationService.isPhoneVerifiedForFamilyMember(
                    familyMember.getId(), phoneNumberFromForm
                );
            }
            
            // Check user phone (nếu số trùng với số của user)
            if (currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().trim().isEmpty()) {
                userVerified = phoneVerificationService.isPhoneVerifiedForUser(
                    currentUser.getId(), phoneNumberFromForm
                );
            }
            
            if (!familyMemberVerified && !userVerified) {
                throw new RuntimeException(
                    String.format("Số điện thoại chưa được xác thực. Vui lòng xác thực số điện thoại trước khi đặt lịch tiêm cho %s", 
                        familyMember.getFullName())
                );
            }
            return; // Đã xác thực
        }
        
        // Nếu không có số từ form, kiểm tra số trong database
        // Nếu người thân có số điện thoại riêng
        if (familyMember.getPhoneNumber() != null && !familyMember.getPhoneNumber().trim().isEmpty()) {
            // Kiểm tra số điện thoại của người thân đã xác thực chưa
            if (familyMember.getPhoneVerified() == null || !familyMember.getPhoneVerified()) {
                throw new RuntimeException(
                    String.format("Số điện thoại của %s chưa được xác thực. Vui lòng xác thực số điện thoại trước khi đặt lịch tiêm", 
                        familyMember.getFullName())
                );
            }
        } else {
            // Người thân chưa có số điện thoại riêng, cần dùng số của user
            // Kiểm tra số điện thoại của user đã xác thực chưa
            if (currentUser.getPhoneNumber() == null || currentUser.getPhoneNumber().trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập và xác thực số điện thoại của bạn trước khi đặt lịch tiêm cho người thân");
            }
            
            if (currentUser.getPhoneVerified() == null || !currentUser.getPhoneVerified()) {
                throw new RuntimeException("Số điện thoại của bạn chưa được xác thực. Vui lòng xác thực số điện thoại trước khi đặt lịch tiêm cho người thân");
            }
        }
    }
    
    /**
     * Hủy lịch hẹn (chỉ cho phép hủy nếu trạng thái là PENDING)
     * @param appointmentId ID của appointment cần hủy
     * @param currentUser User hiện tại (để kiểm tra quyền)
     * @return Appointment đã được hủy
     */
    public Appointment cancelAppointment(Long appointmentId, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Kiểm tra quyền: chỉ user đặt lịch mới được hủy
        if (appointment.getBookedByUser() == null || !appointment.getBookedByUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy lịch hẹn này");
        }
        
        // Cho phép hủy nếu trạng thái là PENDING hoặc CONFIRMED (nhưng chỉ cho hủy CONFIRMED trước 24h)
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            // Kiểm tra xem lịch hẹn có trong vòng 24h tới không
            if (appointment.getAppointmentDate() != null && appointment.getAppointmentTime() != null) {
                LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getAppointmentTime());
                LocalDateTime now = LocalDateTime.now();
                long hoursUntilAppointment = java.time.Duration.between(now, appointmentDateTime).toHours();
                
                if (hoursUntilAppointment < 24) {
                    throw new RuntimeException(
                        String.format("Chỉ có thể hủy lịch hẹn trước 24 giờ. Lịch hẹn của bạn còn %d giờ nữa.", hoursUntilAppointment)
                    );
                }
            }
        } else if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException(
                String.format("Chỉ có thể hủy lịch hẹn khi trạng thái là 'Chờ xác nhận' hoặc 'Đã xác nhận'. Trạng thái hiện tại: %s", 
                    getStatusLabel(appointment.getStatus()))
            );
        }
        
        // Cập nhật trạng thái
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        // Tạo thông báo hủy lịch
        try {
            notificationService.createAppointmentCancelledNotification(appointment);
        } catch (Exception e) {
            // Log error nhưng không throw để không làm gián đoạn quá trình hủy lịch
            System.err.println("Failed to create cancellation notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Cập nhật slot booking count (giảm số lượng booking)
        AppointmentSlot slot = appointment.getSlot();
        if (slot != null) {
            int currentBookings = slot.getCurrentBookings();
            if (currentBookings > 0) {
                slot.setCurrentBookings(currentBookings - 1);
            }
            // Nếu slot đã đầy trước đó, mở lại slot
            if (!slot.getIsAvailable() && slot.getCurrentBookings() < slot.getMaxCapacity()) {
                slot.setIsAvailable(true);
            }
            appointmentSlotRepository.save(slot);
        }
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Xóa appointment khi thanh toán VNPay thất bại
     * Rollback slot booking count và xóa payment
     * @param appointmentId ID của appointment cần xóa
     */
    public void deleteAppointmentWhenPaymentFailed(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Chỉ xóa nếu payment method là VNPAY và status là PENDING hoặc CONFIRMED
        if (appointment.getPayment() != null && 
            appointment.getPayment().getPaymentMethod() == PaymentMethod.VNPAY &&
            (appointment.getStatus() == AppointmentStatus.PENDING || appointment.getStatus() == AppointmentStatus.CONFIRMED)) {
            
            // Rollback slot booking count
            AppointmentSlot slot = appointment.getSlot();
            if (slot != null) {
                int currentBookings = slot.getCurrentBookings();
                if (currentBookings > 0) {
                    slot.setCurrentBookings(currentBookings - 1);
                }
                // Nếu slot đã đầy trước đó, mở lại slot
                if (!slot.getIsAvailable() && slot.getCurrentBookings() < slot.getMaxCapacity()) {
                    slot.setIsAvailable(true);
                }
                appointmentSlotRepository.save(slot);
            }
            
            // Xóa payment
            Payment payment = appointment.getPayment();
            if (payment != null) {
                paymentRepository.delete(payment);
            }
            
            // Xóa appointment
            appointmentRepository.delete(appointment);
        } else {
            throw new RuntimeException("Chỉ có thể xóa appointment khi payment method là VNPAY và status là PENDING hoặc CONFIRMED");
        }
    }
    
    /**
     * Kiểm tra cảnh báo cùng ngày: Check xem user đã có lịch cùng ngày chưa
     * @param appointmentDate Ngày đặt lịch
     * @param userIdToCheck User ID (null nếu đặt cho người thân)
     * @param familyMemberIdToCheck FamilyMember ID (null nếu đặt cho bản thân)
     * @param excludeAppointmentId Appointment ID cần exclude (để không tính appointment vừa tạo, có thể null)
     * @return Warning message nếu có lịch cùng ngày, null nếu không có
     */
    public String checkSameDayAppointmentWarning(LocalDate appointmentDate, Long userIdToCheck, Long familyMemberIdToCheck, Long excludeAppointmentId) {
        List<Appointment> sameDayAppointments;
        
        if (userIdToCheck != null) {
            // Check cho bản thân
            sameDayAppointments = appointmentRepository.findAppointmentsByUserInDateRange(
                    userIdToCheck,
                    appointmentDate,
                    appointmentDate,
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
            );
        } else if (familyMemberIdToCheck != null) {
            // Check cho người thân
            sameDayAppointments = appointmentRepository.findAppointmentsByFamilyMemberInDateRange(
                    familyMemberIdToCheck,
                    appointmentDate,
                    appointmentDate,
                    List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
            );
        } else {
            return null; // Không có thông tin để check
        }
        
        // Filter out excluded appointment
        if (excludeAppointmentId != null) {
            sameDayAppointments = sameDayAppointments.stream()
                    .filter(apt -> !apt.getId().equals(excludeAppointmentId))
                    .collect(Collectors.toList());
        }
        
        if (sameDayAppointments.isEmpty()) {
            return null; // Không có lịch cùng ngày
        }
        
        // Tạo message cảnh báo với thông tin các lịch cùng ngày
        StringBuilder warning = new StringBuilder("Bạn đã có ");
        warning.append(sameDayAppointments.size());
        warning.append(" lịch hẹn khác trong cùng ngày: ");
        
        for (int i = 0; i < sameDayAppointments.size() && i < 3; i++) {
            Appointment apt = sameDayAppointments.get(i);
            if (i > 0) warning.append(", ");
            if (apt.getAppointmentTime() != null) {
                warning.append(String.format("%02d:%02d", apt.getAppointmentTime().getHour(), apt.getAppointmentTime().getMinute()));
            }
            if (apt.getVaccine() != null) {
                warning.append(" (").append(apt.getVaccine().getName()).append(")");
            }
        }
        
        if (sameDayAppointments.size() > 3) {
            warning.append(" và ").append(sameDayAppointments.size() - 3).append(" lịch khác");
        }
        
        warning.append(". Bạn có chắc chắn muốn tiếp tục đặt lịch này không?");
        
        return warning.toString();
    }
    
    /**
     * Helper method để lấy label của status
     */
    private String getStatusLabel(AppointmentStatus status) {
        if (status == null) return "N/A";
        switch (status) {
            case PENDING: return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case CHECKED_IN: return "Đã check-in";
            case SCREENING: return "Đang khám sàng lọc";
            case APPROVED: return "Đủ điều kiện tiêm";
            case REJECTED: return "Không đủ điều kiện tiêm";
            case INJECTING: return "Đang tiêm vaccine";
            case MONITORING: return "Đang theo dõi sau tiêm";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Đã hủy";
            case RESCHEDULED: return "Đã đổi lịch";
            default: return status.name();
        }
    }
    
    /**
     * Generate unique booking code
     */
    private String generateBookingCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        long count = appointmentRepository.count();
        return String.format("BK-%s-%s-%03d", dateStr, timeStr, (count % 1000) + 1);
    }
    
    /**
     * Lấy danh sách lịch hẹn hôm nay
     * @param status Trạng thái để lọc (optional, null = lấy tất cả)
     * @return Danh sách appointments
     */
    public List<Appointment> getTodayAppointments(AppointmentStatus status) {
        LocalDate today = LocalDate.now();
        return getAppointmentsByDate(today, status);
    }
    
    /**
     * Lấy danh sách lịch hẹn theo ngày cụ thể
     * @param date Ngày cần lấy
     * @param status Trạng thái để lọc (optional, null = lấy tất cả)
     * @return Danh sách appointments
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date, AppointmentStatus status) {
        if (status != null) {
            return appointmentRepository.findByAppointmentDateAndStatus(date, status);
        } else {
            return appointmentRepository.findByAppointmentDate(date);
        }
    }
    
    /**
     * Lấy danh sách lịch hẹn đã phê duyệt (APPROVED) - cho Nurse
     * @return Danh sách appointments với status APPROVED
     */
    public List<Appointment> getApprovedAppointments() {
        return appointmentRepository.findByStatus(AppointmentStatus.APPROVED);
    }
    
    /**
     * Check-in khách hàng
     * - Cập nhật status: CHECKED_IN
     * - Gán số thứ tự
     * - Tạo AppointmentHistory
     * @param appointmentId ID của appointment
     * @param checkedByUser User thực hiện check-in (receptionist)
     * @return Appointment đã được check-in
     */
    public Appointment checkInAppointment(Long appointmentId, User checkedByUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Kiểm tra trạng thái hiện tại - cho phép check-in từ PENDING hoặc CONFIRMED
        // Receptionist có thể check-in trực tiếp mà không cần đợi xác nhận
        if (appointment.getStatus() != AppointmentStatus.PENDING && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException(
                String.format("Chỉ có thể check-in khi trạng thái là 'Chờ xác nhận' hoặc 'Đã xác nhận'. Trạng thái hiện tại: %s", 
                    getStatusLabel(appointment.getStatus()))
            );
        }
        
        // Kiểm tra ngày hẹn phải là hôm nay
        if (appointment.getAppointmentDate() == null || !appointment.getAppointmentDate().equals(LocalDate.now())) {
            throw new RuntimeException("Chỉ có thể check-in cho lịch hẹn hôm nay");
        }
        
        // Lấy số thứ tự tiếp theo cho ngày hôm nay
        Integer nextQueueNumber = getNextQueueNumber(LocalDate.now());
        
        // Lưu trạng thái cũ
        AppointmentStatus oldStatus = appointment.getStatus();
        
        // Cập nhật trạng thái và số thứ tự
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setQueueNumber(nextQueueNumber);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        // Lưu appointment
        appointment = appointmentRepository.save(appointment);
        
        // Tạo AppointmentHistory
        createAppointmentHistory(appointment, oldStatus, AppointmentStatus.CHECKED_IN, checkedByUser, "Check-in tại quầy lễ tân");
        
        return appointment;
    }
    
    /**
     * Xác nhận lịch hẹn (cho Receptionist)
     * @param appointmentId ID của appointment
     * @param confirmedByUser User thực hiện xác nhận (receptionist)
     * @return Appointment đã được xác nhận
     */
    public Appointment confirmAppointmentByReceptionist(Long appointmentId, User confirmedByUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Chỉ có thể xác nhận khi trạng thái là PENDING
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException(
                String.format("Chỉ có thể xác nhận lịch hẹn khi trạng thái là 'Chờ xác nhận'. Trạng thái hiện tại: %s",
                    getStatusLabel(appointment.getStatus()))
            );
        }
        
        // Lưu trạng thái cũ
        AppointmentStatus oldStatus = appointment.getStatus();
        
        // Cập nhật trạng thái
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        // Lưu appointment
        appointment = appointmentRepository.save(appointment);
        
        // Tạo AppointmentHistory
        createAppointmentHistory(appointment, oldStatus, AppointmentStatus.CONFIRMED, confirmedByUser, "Xác nhận lịch hẹn bởi lễ tân");
        
        return appointment;
    }
    
    /**
     * Hủy lịch hẹn bởi Receptionist (không cần kiểm tra quyền như cancelAppointment)
     * @param appointmentId ID của appointment
     * @param cancelledByUser User thực hiện hủy (receptionist)
     * @param reason Lý do hủy (optional)
     * @return Appointment đã được hủy
     */
    public Appointment cancelAppointmentByReceptionist(Long appointmentId, User cancelledByUser, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        // Cho phép hủy nếu trạng thái là PENDING hoặc CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.PENDING && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException(
                String.format("Chỉ có thể hủy lịch hẹn khi trạng thái là 'Chờ xác nhận' hoặc 'Đã xác nhận'. Trạng thái hiện tại: %s",
                    getStatusLabel(appointment.getStatus()))
            );
        }
        
        // Lưu trạng thái cũ
        AppointmentStatus oldStatus = appointment.getStatus();
        
        // Cập nhật trạng thái
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        // Thêm lý do vào notes nếu có
        if (reason != null && !reason.trim().isEmpty()) {
            String currentNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
            appointment.setNotes(currentNotes + (currentNotes.isEmpty() ? "" : "\n") + 
                "[Hủy bởi lễ tân: " + reason + "]");
        }
        
        // Tạo thông báo hủy lịch
        try {
            notificationService.createAppointmentCancelledNotification(appointment);
        } catch (Exception e) {
            // Log error nhưng không throw để không làm gián đoạn quá trình hủy lịch
            System.err.println("Failed to create cancellation notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Cập nhật slot booking count (giảm số lượng booking)
        AppointmentSlot slot = appointment.getSlot();
        if (slot != null) {
            int currentBookings = slot.getCurrentBookings();
            if (currentBookings > 0) {
                slot.setCurrentBookings(currentBookings - 1);
            }
            // Nếu slot đã đầy trước đó, mở lại slot
            if (!slot.getIsAvailable() && slot.getCurrentBookings() < slot.getMaxCapacity()) {
                slot.setIsAvailable(true);
            }
            appointmentSlotRepository.save(slot);
        }
        
        // Lưu appointment
        appointment = appointmentRepository.save(appointment);
        
        // Tạo AppointmentHistory
        String historyNote = "Hủy lịch hẹn bởi lễ tân" + (reason != null && !reason.trim().isEmpty() ? ": " + reason : "");
        createAppointmentHistory(appointment, oldStatus, AppointmentStatus.CANCELLED, cancelledByUser, historyNote);
        
        return appointment;
    }
    
    /**
     * Tìm kiếm appointment theo số điện thoại hoặc booking code
     * @param phone Số điện thoại (optional)
     * @param bookingCode Mã booking (optional)
     * @return Danh sách appointments tìm được
     */
    public List<Appointment> searchAppointments(String phone, String bookingCode) {
        if (bookingCode != null && !bookingCode.trim().isEmpty()) {
            // Tìm theo booking code
            Optional<Appointment> appointmentOpt = appointmentRepository.findByBookingCode(bookingCode.trim());
            return appointmentOpt.map(List::of).orElse(List.of());
        } else if (phone != null && !phone.trim().isEmpty()) {
            // Tìm theo số điện thoại
            // Tìm trong User
            List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getPhoneNumber() != null && u.getPhoneNumber().contains(phone.trim()))
                .collect(Collectors.toList());
            
            List<Appointment> results = new java.util.ArrayList<>();
            
            // Tìm appointments của các user này
            for (User user : users) {
                results.addAll(appointmentRepository.findByBookedByUserId(user.getId()));
                results.addAll(appointmentRepository.findByBookedForUserId(user.getId()));
            }
            
            // Tìm trong consultationPhone và guest phone
            results.addAll(appointmentRepository.findAll().stream()
                .filter(a -> (a.getConsultationPhone() != null && a.getConsultationPhone().contains(phone.trim())) ||
                            (a.getBookedByUser() == null && a.getGuestFullName() != null)) // Guest appointments
                .collect(Collectors.toList()));
            
            return results.stream().distinct().collect(Collectors.toList());
        } else {
            throw new RuntimeException("Phải cung cấp số điện thoại hoặc mã booking để tìm kiếm");
        }
    }
    
    /**
     * Tạo walk-in appointment (không đặt trước)
     * @param dto DTO chứa thông tin walk-in
     * @param createdByUser User tạo (receptionist)
     * @return Appointment đã tạo
     */
    public Appointment createWalkInAppointment(WalkInAppointmentDTO dto, User createdByUser) {
        // Validate required fields
        if (dto.getVaccineId() == null) {
            throw new RuntimeException("Vaccine ID is required");
        }
        if (dto.getCenterId() == null) {
            throw new RuntimeException("Center ID is required");
        }
        if (dto.getAppointmentDate() == null) {
            throw new RuntimeException("Appointment date is required");
        }
        if (dto.getAppointmentTime() == null) {
            throw new RuntimeException("Appointment time is required");
        }
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
            throw new RuntimeException("Phone number is required");
        }
        
        // Validate vaccine exists
        Vaccine vaccine = vaccineRepository.findById(dto.getVaccineId())
                .orElseThrow(() -> new RuntimeException("Vaccine not found"));
        
        // Validate center exists
        VaccinationCenter center = vaccinationCenterRepository.findById(dto.getCenterId())
                .orElseThrow(() -> new RuntimeException("Center not found"));
        
        // Validate room if provided
        ClinicRoom room = null;
        if (dto.getRoomId() != null) {
            room = clinicRoomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));
        }
        
        // Validate dose number
        Integer doseNumber = dto.getDoseNumber() != null ? dto.getDoseNumber() : 1;
        if (doseNumber <= 0) {
            throw new RuntimeException("Dose number must be greater than 0");
        }
        if (vaccine.getDosesRequired() != null && doseNumber > vaccine.getDosesRequired()) {
            throw new RuntimeException(
                String.format("Vaccine '%s' chỉ cần %d mũi. Không thể đặt mũi thứ %d.", 
                    vaccine.getName(), vaccine.getDosesRequired(), doseNumber)
            );
        }
        
        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setBookingCode(generateBookingCode());
        appointment.setBookedByUser(createdByUser); // Receptionist tạo
        appointment.setBookedForUser(null); // Walk-in không có user đặt
        appointment.setFamilyMember(null); // Walk-in không có family member
        appointment.setVaccine(vaccine);
        appointment.setCenter(center);
        
        // Set slot if provided
        if (dto.getSlotId() != null) {
            AppointmentSlot slot = appointmentSlotRepository.findById(dto.getSlotId())
                    .orElseThrow(() -> new RuntimeException("Slot not found"));
            
            // Validate slot is available
            if (!slot.getIsAvailable() || slot.getCurrentBookings() >= slot.getMaxCapacity()) {
                throw new RuntimeException("Slot đã hết chỗ hoặc không khả dụng");
            }
            
            // Validate slot matches center and date
            if (!slot.getCenter().getId().equals(center.getId())) {
                throw new RuntimeException("Slot không thuộc trung tâm đã chọn");
            }
            if (!slot.getDate().equals(dto.getAppointmentDate())) {
                throw new RuntimeException("Slot không khớp với ngày đã chọn");
            }
            
            appointment.setSlot(slot);
            // Update slot booking count
            slot.setCurrentBookings(slot.getCurrentBookings() + 1);
            if (slot.getCurrentBookings() >= slot.getMaxCapacity()) {
                slot.setIsAvailable(false);
            }
            appointmentSlotRepository.save(slot);
        } else {
            appointment.setSlot(null); // Walk-in không có slot
        }
        
        appointment.setRoom(room);
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setAppointmentTime(dto.getAppointmentTime());
        appointment.setRequiresConsultation(false);
        appointment.setStatus(AppointmentStatus.CONFIRMED); // Tự động xác nhận
        appointment.setDoseNumber(doseNumber);
        appointment.setNotes(dto.getNotes());
        
        // Set guest information
        appointment.setGuestFullName(dto.getFullName());
        appointment.setGuestEmail(dto.getEmail());
        appointment.setGuestDayOfBirth(dto.getDayOfBirth());
        if (dto.getGender() != null && !dto.getGender().trim().isEmpty()) {
            try {
                appointment.setGuestGender(ut.edu.vaccinationmanagementsystem.entity.enums.Gender.valueOf(dto.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid gender, skip
            }
        }
        appointment.setConsultationPhone(dto.getPhoneNumber());
        
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        
        // Save appointment first to get ID
        appointment = appointmentRepository.save(appointment);
        
        // Create Payment
        PaymentMethod paymentMethod = dto.getPaymentMethod() != null 
            ? PaymentMethod.valueOf(dto.getPaymentMethod().toUpperCase())
            : PaymentMethod.CASH; // Default to CASH
        
        Payment payment = paymentService.createPayment(appointment, paymentMethod);
        appointment.setPayment(payment);
        
        appointment = appointmentRepository.save(appointment);
        
        // Tạo AppointmentHistory
        createAppointmentHistory(appointment, null, AppointmentStatus.CONFIRMED, createdByUser, "Tạo walk-in appointment");
        
        return appointment;
    }
    
    /**
     * Lấy số thứ tự tiếp theo cho ngày hôm nay
     * @param date Ngày cần lấy số thứ tự
     * @return Số thứ tự tiếp theo
     */
    private Integer getNextQueueNumber(LocalDate date) {
        List<Appointment> todayAppointments = appointmentRepository.findByAppointmentDate(date);
        
        // Lọc các appointment có queueNumber
        List<Appointment> appointmentsWithQueue = todayAppointments.stream()
            .filter(a -> a.getQueueNumber() != null)
            .collect(Collectors.toList());
        
        if (appointmentsWithQueue.isEmpty()) {
            return 1;
        }
        
        Integer maxQueueNumber = appointmentsWithQueue.stream()
            .map(Appointment::getQueueNumber)
            .max(Integer::compareTo)
            .orElse(0);
        
        return maxQueueNumber + 1;
    }
    
    /**
     * Tạo AppointmentHistory để ghi lại thay đổi trạng thái
     * @param appointment Appointment
     * @param oldStatus Trạng thái cũ (null nếu là lần đầu tạo)
     * @param newStatus Trạng thái mới
     * @param changedBy User thực hiện thay đổi
     * @param reason Lý do thay đổi
     */
    private void createAppointmentHistory(Appointment appointment, AppointmentStatus oldStatus, 
                                         AppointmentStatus newStatus, User changedBy, String reason) {
        AppointmentHistory history = new AppointmentHistory();
        history.setAppointment(appointment);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        history.setReason(reason);
        
        appointmentHistoryRepository.save(history);
    }
}


