package ut.edu.vaccinationmanagementsystem.entity;
import jakarta.persistence.*;
import ut.edu.vaccinationmanagementsystem.entity.enums.AuthProvider;
import ut.edu.vaccinationmanagementsystem.entity.enums.Gender;
import ut.edu.vaccinationmanagementsystem.entity.enums.Role;
import ut.edu.vaccinationmanagementsystem.entity.enums.UserStatus;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(unique = true,nullable = false)
    private String fireBaseUid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = true)
    private String phoneNumber;

    @Column(nullable = true)
    private LocalDate dayOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Gender gender;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = true, unique = true)
    private String citizenId; // CMND/CCCD (nullable, unique nếu có)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private LocalDate createAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private StaffInfo staffInfo; // Thông tin nhân viên (chỉ có khi role là STAFF)

    @OneToMany(mappedBy = "bookedByUser")
    private List<Appointment> bookedAppointments; // Danh sách lịch hẹn mà user này đặt

    @OneToMany(mappedBy = "bookedForUser")
    private List<Appointment> appointmentsForMe; // Danh sách lịch hẹn được đặt cho user này

    @OneToMany(mappedBy = "user")
    private List<VaccinationRecord> vaccinationRecords; // Lịch sử tiêm chủng

    @OneToMany(mappedBy = "user")
    private List<FamilyMember> familyMembers; // Danh sách người thân

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications; // Danh sách thông báo

    @OneToMany(mappedBy = "user")
    private List<WorkSchedule> workSchedules; // Lịch làm việc (cho nhân viên)

    @OneToMany(mappedBy = "changedBy")
    private List<AppointmentHistory> appointmentHistories; // Lịch sử thay đổi trạng thái lịch hẹn

    @OneToMany(mappedBy = "doctor")
    private List<Screening> screenings; // Danh sách khám sàng lọc (cho bác sĩ)

    @OneToMany(mappedBy = "nurse")
    private List<VaccinationRecord> injections; // Danh sách tiêm vaccine (cho bác sĩ/y tá)

    @OneToMany(mappedBy = "handledBy")
    private List<AdverseReaction> adverseReactions;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFireBaseUid() {
        return fireBaseUid;
    }

    public void setFireBaseUid(String fireBaseUid) {
        this.fireBaseUid = fireBaseUid;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDayOfBirth() {
        return dayOfBirth;
    }

    public void setDayOfBirth(LocalDate dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDate getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDate createAt) {
        this.createAt = createAt;
    }

    public StaffInfo getStaffInfo() {
        return staffInfo;
    }

    public void setStaffInfo(StaffInfo staffInfo) {
        this.staffInfo = staffInfo;
    }

    public List<Appointment> getBookedAppointments() {
        return bookedAppointments;
    }

    public void setBookedAppointments(List<Appointment> bookedAppointments) {
        this.bookedAppointments = bookedAppointments;
    }

    public List<Appointment> getAppointmentsForMe() {
        return appointmentsForMe;
    }

    public void setAppointmentsForMe(List<Appointment> appointmentsForMe) {
        this.appointmentsForMe = appointmentsForMe;
    }

    public List<VaccinationRecord> getVaccinationRecords() {
        return vaccinationRecords;
    }

    public void setVaccinationRecords(List<VaccinationRecord> vaccinationRecords) {
        this.vaccinationRecords = vaccinationRecords;
    }

    public List<FamilyMember> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<FamilyMember> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<WorkSchedule> getWorkSchedules() {
        return workSchedules;
    }

    public void setWorkSchedules(List<WorkSchedule> workSchedules) {
        this.workSchedules = workSchedules;
    }

    public List<AppointmentHistory> getAppointmentHistories() {
        return appointmentHistories;
    }

    public void setAppointmentHistories(List<AppointmentHistory> appointmentHistories) {
        this.appointmentHistories = appointmentHistories;
    }

    public List<Screening> getScreenings() {
        return screenings;
    }

    public void setScreenings(List<Screening> screenings) {
        this.screenings = screenings;
    }

    public List<VaccinationRecord> getInjections() {
        return injections;
    }

    public void setInjections(List<VaccinationRecord> injections) {
        this.injections = injections;
    }

    public List<AdverseReaction> getAdverseReactions() {
        return adverseReactions;
    }

    public void setAdverseReactions(List<AdverseReaction> adverseReactions) {
        this.adverseReactions = adverseReactions;
    }
}
