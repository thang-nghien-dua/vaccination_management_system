package ut.edu.vaccinationmanagement_system.entity.enums;

public enum AppointmentStatus {
    PENDING,      // Chờ xác nhận
    CONFIRMED,    // Đã xác nhận
    CHECKED_IN,   // Đã check-in tại quầy lễ tân
    SCREENING,    // Đang khám sàng lọc
    APPROVED,     // Đủ điều kiện tiêm
    REJECTED,     // Không đủ điều kiện tiêm
    INJECTING,    // Đang tiêm vaccine
    MONITORING,   // Đang theo dõi sau tiêm
    COMPLETED,    // Hoàn thành
    CANCELLED,    // Đã hủy
    RESCHEDULED   // Đã đổi lịch
}
