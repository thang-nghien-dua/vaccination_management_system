package ut.edu.vaccinationmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ut.edu.vaccinationmanagementsystem.dto.BookAppointmentDTO;
import ut.edu.vaccinationmanagementsystem.dto.ConsultationRequestDTO;
import ut.edu.vaccinationmanagementsystem.entity.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AppointmentStatus;
import ut.edu.vaccinationmanagementsystem.entity.enums.PaymentMethod;
import ut.edu.vaccinationmanagementsystem.entity.enums.VaccineLotStatus;
import ut.edu.vaccinationmanagementsystem.repository.*;
import ut.edu.vaccinationmanagementsystem.service.CustomUserDetails;
import ut.edu.vaccinationmanagementsystem.service.CustomOAuth2User;

import java.math.BigDecimal;
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
    private VaccineLotRepository vaccineLotRepository;
    
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
            // Kiểm tra phí hủy chưa thanh toán
            List<Payment> unpaidCancellationFees = paymentRepository.findUnpaidCancellationFeesByUser(currentUser.getId());
            if (!unpaidCancellationFees.isEmpty()) {
                BigDecimal totalUnpaidFee = unpaidCancellationFees.stream()
                    .map(p -> p.getCancellationFee() != null ? p.getCancellationFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                throw new RuntimeException(
                    String.format("Bạn có phí hủy lịch chưa thanh toán: %,.0f VNĐ. Vui lòng thanh toán để đặt lịch mới.", 
                        totalUnpaidFee.doubleValue())
                );
            }
            
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
            
            // Build notes with workUnit if provided
            StringBuilder notesBuilder = new StringBuilder();
            if (dto.getWorkUnit() != null && !dto.getWorkUnit().trim().isEmpty()) {
                notesBuilder.append("Đơn vị công tác: ").append(dto.getWorkUnit()).append(". ");
            }
            if (dto.getReason() != null && !dto.getReason().trim().isEmpty()) {
                notesBuilder.append("Lý do: ").append(dto.getReason()).append(". ");
            }
            if (dto.getNotes() != null && !dto.getNotes().trim().isEmpty()) {
                notesBuilder.append("Ghi chú: ").append(dto.getNotes());
            }
            appointment.setNotes(notesBuilder.toString().trim());
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
        
        // Kiểm tra phí hủy chưa thanh toán
        List<Payment> unpaidCancellationFees = paymentRepository.findUnpaidCancellationFeesByUser(currentUser.getId());
        if (!unpaidCancellationFees.isEmpty()) {
            BigDecimal totalUnpaidFee = unpaidCancellationFees.stream()
                .map(p -> p.getCancellationFee() != null ? p.getCancellationFee() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            throw new RuntimeException(
                String.format("Bạn có phí hủy lịch chưa thanh toán: %,.0f VNĐ. Vui lòng thanh toán để đặt lịch mới.", 
                    totalUnpaidFee.doubleValue())
            );
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
            bookedForUser = currentUser; // Set bookedForUser = currentUser khi đặt cho bản thân
            userIdToCheck = currentUser.getId();
            
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
        
        // Trừ vaccine khi CONFIRMED (giữ vaccine cho appointment)
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED && 
            appointment.getVaccine() != null && 
            appointment.getCenter() != null) {
            reserveVaccineForAppointmentV2(appointment);
        }
        
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
        // GIẢI PHÁP 1: Ưu tiên kiểm tra số trong database trước
        // Nếu số trong database đã được xác thực → cho phép luôn, không cần kiểm tra số từ form
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty() &&
            user.getPhoneVerified() != null && user.getPhoneVerified()) {
            // Số trong database đã được xác thực → cho phép đặt lịch
            // Không cần kiểm tra số từ form vì đã có số verified trong database
            return;
        }
        
        // Nếu số trong database chưa được xác thực hoặc không có số
        // Kiểm tra số từ form (nếu có)
        if (phoneNumberFromForm != null && !phoneNumberFromForm.trim().isEmpty()) {
            // Kiểm tra số từ form có được xác thực không
            boolean verified = phoneVerificationService.isPhoneVerifiedForUser(user.getId(), phoneNumberFromForm);
            if (!verified) {
                throw new RuntimeException("Số điện thoại chưa được xác thực. Vui lòng xác thực số điện thoại trước khi đặt lịch tiêm");
            }
            return; // Đã xác thực, không cần check tiếp
        }
        
        // Nếu không có số từ form và số trong database chưa được xác thực
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
        // GIẢI PHÁP 1: Ưu tiên kiểm tra số trong database trước
        // Nếu số của family member trong database đã được xác thực → cho phép luôn
        if (familyMember.getPhoneNumber() != null && !familyMember.getPhoneNumber().trim().isEmpty() &&
            familyMember.getPhoneVerified() != null && familyMember.getPhoneVerified()) {
            // Số của family member trong database đã được xác thực → cho phép đặt lịch
            return;
        }
        
        // Nếu số của user trong database đã được xác thực → cho phép luôn (có thể dùng số của user)
        if (currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().trim().isEmpty() &&
            currentUser.getPhoneVerified() != null && currentUser.getPhoneVerified()) {
            // Số của user trong database đã được xác thực → cho phép đặt lịch
            return;
        }
        
        // Nếu số trong database chưa được xác thực, kiểm tra số từ form (nếu có)
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
        
        // Cho phép hủy nếu trạng thái là PENDING hoặc CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.PENDING && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException(
                String.format("Chỉ có thể hủy lịch hẹn khi trạng thái là 'Chờ xác nhận' hoặc 'Đã xác nhận'. Trạng thái hiện tại: %s", 
                    getStatusLabel(appointment.getStatus()))
            );
        }
        
        // Tính phí hủy và kiểm tra thời gian hủy cho CONFIRMED
        BigDecimal cancellationFee = BigDecimal.ZERO;
        long hoursUntilAppointment = 0;
        
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            // Kiểm tra thời gian hủy
            if (appointment.getAppointmentDate() != null && appointment.getAppointmentTime() != null) {
                LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getAppointmentTime());
                LocalDateTime now = LocalDateTime.now();
                hoursUntilAppointment = java.time.Duration.between(now, appointmentDateTime).toHours();
                
                // Không cho hủy nếu < 6 giờ
                if (hoursUntilAppointment < 6) {
                    throw new RuntimeException(
                        String.format("Không thể hủy lịch hẹn trong vòng 6 giờ trước giờ hẹn. Lịch hẹn của bạn còn %d giờ nữa.", hoursUntilAppointment)
                    );
                }
                
                // Tính phí hủy nếu có payment
                Payment payment = appointment.getPayment();
                if (payment != null && payment.getAmount() != null) {
                    cancellationFee = paymentService.calculateCancellationFee(
                        payment.getAmount(), hoursUntilAppointment);
                    
                    // Lưu phí hủy vào payment cũ
                    payment.setCancellationFee(cancellationFee);
                    payment.setCancellationFeePaid(false); // Đánh dấu chưa thanh toán phí hủy
                    payment.setCancellationReason("Hủy lịch hẹn - " + appointment.getBookingCode());
                    paymentRepository.save(payment);
                    
                    // Note: Phí hủy sẽ được thanh toán thông qua endpoint riêng
                    // Payment mới cho phí hủy sẽ được tạo khi user thanh toán (nếu cần)
                }
                
                // Trả lại vaccine vào kho
                returnVaccineToStock(appointment);
            }
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
     * Tạo walk-in appointment (đăng ký tại quầy, không cần đăng nhập)
     * @param fullName Họ tên khách hàng
     * @param phoneNumber Số điện thoại
     * @param email Email (optional)
     * @param dayOfBirth Ngày sinh (optional)
     * @param gender Giới tính (optional)
     * @param vaccineId ID vaccine
     * @param centerId ID trung tâm
     * @param slotId ID slot
     * @param appointmentDate Ngày hẹn
     * @param appointmentTime Giờ hẹn
     * @param doseNumber Mũi thứ mấy
     * @param paymentMethod Phương thức thanh toán
     * @param notes Ghi chú (optional)
     * @return Appointment đã tạo
     */
    public Appointment createWalkInAppointment(
            String fullName,
            String phoneNumber,
            String email,
            LocalDate dayOfBirth,
            String gender,
            Long vaccineId,
            Long centerId,
            Long slotId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            Integer doseNumber,
            String paymentMethod,
            String notes) {
        
        // Validate required fields
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new RuntimeException("Họ tên là bắt buộc");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new RuntimeException("Số điện thoại là bắt buộc");
        }
        if (vaccineId == null) {
            throw new RuntimeException("Vaccine ID là bắt buộc");
        }
        if (centerId == null) {
            throw new RuntimeException("Center ID là bắt buộc");
        }
        if (slotId == null) {
            throw new RuntimeException("Slot ID là bắt buộc");
        }
        if (appointmentDate == null) {
            throw new RuntimeException("Ngày hẹn là bắt buộc");
        }
        if (appointmentTime == null) {
            throw new RuntimeException("Giờ hẹn là bắt buộc");
        }
        
        // Validate vaccine exists
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new RuntimeException("Vaccine not found"));
        
        // Validate center exists
        VaccinationCenter center = vaccinationCenterRepository.findById(centerId)
                .orElseThrow(() -> new RuntimeException("Vaccination center not found"));
        
        // Validate slot exists and is available
        AppointmentSlot slot = appointmentSlotRepository.findById(slotId)
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
        
        // Validate doseNumber
        if (doseNumber == null || doseNumber <= 0) {
            doseNumber = 1; // Default to dose 1
        }
        
        if (vaccine.getDosesRequired() != null && doseNumber > vaccine.getDosesRequired()) {
            throw new RuntimeException(
                String.format("Vaccine '%s' chỉ cần %d mũi. Không thể đặt mũi thứ %d.", 
                    vaccine.getName(), vaccine.getDosesRequired(), doseNumber)
            );
        }
        
        // Validate: Kiểm tra stock quantity
        Optional<CenterVaccine> centerVaccineOpt = centerVaccineRepository.findByCenterAndVaccine(center, vaccine);
        if (centerVaccineOpt.isEmpty()) {
            throw new RuntimeException("Vaccine không có tại trung tâm này");
        }
        CenterVaccine centerVaccine = centerVaccineOpt.get();
        if (centerVaccine.getStockQuantity() == null || centerVaccine.getStockQuantity() <= 0) {
            throw new RuntimeException("Vaccine hiện đang hết hàng tại trung tâm này");
        }
        
        // Parse gender
        ut.edu.vaccinationmanagementsystem.entity.enums.Gender genderEnum = null;
        if (gender != null && !gender.trim().isEmpty()) {
            try {
                genderEnum = ut.edu.vaccinationmanagementsystem.entity.enums.Gender.valueOf(gender.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid gender, will be null
            }
        }
        
        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setBookingCode(generateBookingCode());
        appointment.setBookedByUser(null); // Walk-in không cần đăng nhập
        appointment.setBookedForUser(null);
        appointment.setFamilyMember(null);
        appointment.setVaccine(vaccine);
        appointment.setCenter(center);
        appointment.setSlot(slot);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setRequiresConsultation(false); // Walk-in không cần tư vấn
        appointment.setStatus(AppointmentStatus.CONFIRMED); // Tự động xác nhận
        appointment.setDoseNumber(doseNumber);
        appointment.setNotes(notes);
        
        // Set guest information
        appointment.setGuestFullName(fullName.trim());
        appointment.setConsultationPhone(phoneNumber.trim());
        if (email != null && !email.trim().isEmpty()) {
            appointment.setGuestEmail(email.trim());
        }
        if (dayOfBirth != null) {
            appointment.setGuestDayOfBirth(dayOfBirth);
        }
        if (genderEnum != null) {
            appointment.setGuestGender(genderEnum);
        }
        
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
        PaymentMethod paymentMethodEnum = PaymentMethod.CASH; // Default to CASH
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            try {
                paymentMethodEnum = PaymentMethod.valueOf(paymentMethod.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid payment method, use default CASH
            }
        }
        
        Payment payment = paymentService.createPayment(appointment, paymentMethodEnum);
        appointment.setPayment(payment);
        
        appointment = appointmentRepository.save(appointment);
        
        // Tạo thông báo đặt lịch thành công (nếu có email)
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
     * Giữ vaccine cho appointment khi CONFIRMED (trừ vaccine khỏi kho)
     * Chọn lot theo FIFO (hết hạn trước)
     */
    private void reserveVaccineForAppointment(Appointment appointment) {
        Vaccine vaccine = appointment.getVaccine();
        VaccinationCenter center = appointment.getCenter();
        
        // Lấy CenterVaccine
        Optional<CenterVaccine> centerVaccineOpt = centerVaccineRepository.findByCenterAndVaccine(center, vaccine);
        if (centerVaccineOpt.isEmpty()) {
            throw new RuntimeException("Vaccine không có tại trung tâm này");
        }
        
        CenterVaccine centerVaccine = centerVaccineOpt.get();
        
        // Kiểm tra stock còn đủ không
        if (centerVaccine.getStockQuantity() == null || centerVaccine.getStockQuantity() <= 0) {
            throw new RuntimeException("Vaccine hiện đang hết hàng tại trung tâm này");
        }
        
        // Chọn lot vaccine theo FIFO (hết hạn trước)
        LocalDate today = LocalDate.now();
        
        // Lấy tất cả lot của vaccine này
        List<VaccineLot> allLots = vaccineLotRepository.findByVaccineId(vaccine.getId());
        
        // Filter lot khả dụng: status=AVAILABLE, remainingQuantity>0, expiryDate>today
        List<VaccineLot> availableLots = allLots.stream()
            .filter(lot -> lot.getStatus() == VaccineLotStatus.AVAILABLE)
            .filter(lot -> lot.getRemainingQuantity() != null && lot.getRemainingQuantity() > 0)
            .filter(lot -> lot.getExpiryDate() != null && lot.getExpiryDate().isAfter(today))
            .sorted((l1, l2) -> {
                // Sắp xếp theo expiryDate (hết hạn trước), sau đó theo remainingQuantity (ít trước)
                int dateCompare = l1.getExpiryDate().compareTo(l2.getExpiryDate());
                if (dateCompare != 0) return dateCompare;
                return Integer.compare(l1.getRemainingQuantity(), l2.getRemainingQuantity());
            })
            .collect(java.util.stream.Collectors.toList());
        
        if (availableLots.isEmpty()) {
            // Debug info
            long availableCount = allLots.stream()
                .filter(lot -> lot.getStatus() == VaccineLotStatus.AVAILABLE).count();
            long withStockCount = allLots.stream()
                .filter(lot -> lot.getRemainingQuantity() != null && lot.getRemainingQuantity() > 0).count();
            long notExpiredCount = allLots.stream()
                .filter(lot -> lot.getExpiryDate() != null && lot.getExpiryDate().isAfter(today)).count();
            
            String debugInfo = String.format(
                "Không có lô vaccine nào khả dụng cho vaccine '%s' (ID: %d). " +
                "Tổng số lot: %d, Status=AVAILABLE: %d, Còn hàng: %d, Chưa hết hạn: %d, Ngày hôm nay: %s",
                vaccine.getName(), vaccine.getId(), allLots.size(), availableCount, withStockCount, notExpiredCount, today
            );
            System.err.println("[DEBUG] " + debugInfo);
            throw new RuntimeException("Không có lô vaccine nào khả dụng cho vaccine này. Vui lòng liên hệ quản trị viên.");
        }
        
        // Chọn lot đầu tiên (hết hạn sớm nhất)
        VaccineLot selectedLot = availableLots.get(0);
        
        // Kiểm tra lot còn vaccine không
        if (selectedLot.getRemainingQuantity() == null || selectedLot.getRemainingQuantity() <= 0) {
            throw new RuntimeException("Lô vaccine đã hết hàng");
        }
        
        // Trừ vaccine từ lot
        int currentLotRemaining = selectedLot.getRemainingQuantity();
        selectedLot.setRemainingQuantity(currentLotRemaining - 1);
        vaccineLotRepository.save(selectedLot);
        
        // Trừ vaccine từ center stock
        int currentStock = centerVaccine.getStockQuantity();
        centerVaccine.setStockQuantity(currentStock - 1);
        centerVaccineRepository.save(centerVaccine);
        
        // Lưu lot đã giữ vào appointment
        appointment.setReservedVaccineLot(selectedLot);
    }
    
    /**
     * Giữ vaccine cho appointment khi CONFIRMED (trừ vaccine khỏi kho) - Version 2
     * Sử dụng query có sẵn để tối ưu hiệu suất
     * Chọn lot theo FIFO (hết hạn trước)
     */
    private void reserveVaccineForAppointmentV2(Appointment appointment) {
        Vaccine vaccine = appointment.getVaccine();
        VaccinationCenter center = appointment.getCenter();
        
        // Lấy CenterVaccine
        Optional<CenterVaccine> centerVaccineOpt = centerVaccineRepository.findByCenterAndVaccine(center, vaccine);
        if (centerVaccineOpt.isEmpty()) {
            throw new RuntimeException("Vaccine không có tại trung tâm này");
        }
        
        CenterVaccine centerVaccine = centerVaccineOpt.get();
        
        // Kiểm tra stock còn đủ không
        if (centerVaccine.getStockQuantity() == null || centerVaccine.getStockQuantity() <= 0) {
            throw new RuntimeException("Vaccine hiện đang hết hàng tại trung tâm này");
        }
        
        // Tự động cập nhật status cho các lot của vaccine này trước khi query
        // (Đảm bảo lot hết hạn được đánh dấu EXPIRED)
        LocalDate today = LocalDate.now();
        List<VaccineLot> allLotsForVaccine = vaccineLotRepository.findByVaccineId(vaccine.getId());
        List<VaccineLot> lotsToUpdate = new java.util.ArrayList<>();
        for (VaccineLot lot : allLotsForVaccine) {
            if (lot.getExpiryDate() != null && lot.getExpiryDate().isBefore(today) && 
                lot.getStatus() == VaccineLotStatus.AVAILABLE) {
                lot.setStatus(VaccineLotStatus.EXPIRED);
                lotsToUpdate.add(lot);
            } else if (lot.getRemainingQuantity() != null && lot.getRemainingQuantity() == 0 && 
                       lot.getStatus() == VaccineLotStatus.AVAILABLE) {
                lot.setStatus(VaccineLotStatus.DEPLETED);
                lotsToUpdate.add(lot);
            }
        }
        if (!lotsToUpdate.isEmpty()) {
            vaccineLotRepository.saveAll(lotsToUpdate);
        }
        
        // Chọn lot vaccine theo FIFO (hết hạn trước) - sử dụng query có sẵn
        List<VaccineLot> availableLots = vaccineLotRepository.findAvailableLotsByVaccineIdOrderByExpiryDate(
            vaccine.getId(), today);
        
        if (availableLots.isEmpty()) {
            // Debug: Kiểm tra xem có lot nào không (không filter status/expiry)
            List<VaccineLot> allLots = vaccineLotRepository.findByVaccineId(vaccine.getId());
            long availableCount = allLots.stream()
                .filter(lot -> lot.getStatus() == VaccineLotStatus.AVAILABLE).count();
            long withStockCount = allLots.stream()
                .filter(lot -> lot.getRemainingQuantity() != null && lot.getRemainingQuantity() > 0).count();
            long notExpiredCount = allLots.stream()
                .filter(lot -> lot.getExpiryDate() != null && lot.getExpiryDate().isAfter(today)).count();
            
            String debugInfo = String.format(
                "Không có lô vaccine nào khả dụng cho vaccine '%s' (ID: %d). " +
                "Tổng số lot: %d, Status=AVAILABLE: %d, Còn hàng: %d, Chưa hết hạn: %d, Ngày hôm nay: %s",
                vaccine.getName(), vaccine.getId(), allLots.size(), availableCount, withStockCount, notExpiredCount, today
            );
            System.err.println("[DEBUG] " + debugInfo);
            throw new RuntimeException("Không có lô vaccine nào khả dụng cho vaccine này. Vui lòng liên hệ quản trị viên.");
        }
        
        // Chọn lot đầu tiên (hết hạn sớm nhất) - đã được sắp xếp bởi query
        VaccineLot selectedLot = availableLots.get(0);
        
        // Kiểm tra lot còn vaccine không (double check)
        if (selectedLot.getRemainingQuantity() == null || selectedLot.getRemainingQuantity() <= 0) {
            throw new RuntimeException("Lô vaccine đã hết hàng");
        }
        
        // Trừ vaccine từ lot
        int currentLotRemaining = selectedLot.getRemainingQuantity();
        selectedLot.setRemainingQuantity(currentLotRemaining - 1);
        vaccineLotRepository.save(selectedLot);
        
        // Trừ vaccine từ center stock
        int currentStock = centerVaccine.getStockQuantity();
        centerVaccine.setStockQuantity(currentStock - 1);
        centerVaccineRepository.save(centerVaccine);
        
        // Lưu lot đã giữ vào appointment
        appointment.setReservedVaccineLot(selectedLot);
    }
    
    /**
     * Trả lại vaccine vào kho khi hủy appointment CONFIRMED
     */
    private void returnVaccineToStock(Appointment appointment) {
        if (appointment.getReservedVaccineLot() == null) {
            return; // Không có vaccine đã giữ, không cần trả lại
        }
        
        VaccineLot reservedLot = appointment.getReservedVaccineLot();
        Vaccine vaccine = appointment.getVaccine();
        VaccinationCenter center = appointment.getCenter();
        
        // Cộng lại vaccine vào lot
        int currentLotRemaining = reservedLot.getRemainingQuantity();
        reservedLot.setRemainingQuantity(currentLotRemaining + 1);
        vaccineLotRepository.save(reservedLot);
        
        // Cộng lại vaccine vào center stock
        Optional<CenterVaccine> centerVaccineOpt = centerVaccineRepository.findByCenterAndVaccine(center, vaccine);
        if (centerVaccineOpt.isPresent()) {
            CenterVaccine centerVaccine = centerVaccineOpt.get();
            int currentStock = centerVaccine.getStockQuantity();
            centerVaccine.setStockQuantity(currentStock + 1);
            centerVaccineRepository.save(centerVaccine);
        }
        
        // Xóa reserved lot khỏi appointment
        appointment.setReservedVaccineLot(null);
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
}


