-- ============================================
-- XEM DANH SÁCH TẤT CẢ TRUNG TÂM Y TẾ
-- ============================================

-- Xem tất cả trung tâm
SELECT 
    id,
    name AS 'Tên trung tâm',
    address AS 'Địa chỉ',
    phone_number AS 'Số điện thoại',
    email AS 'Email',
    capacity AS 'Sức chứa',
    status AS 'Trạng thái',
    created_at AS 'Ngày tạo'
FROM vaccination_centers
ORDER BY id;

-- Xem chi tiết trung tâm kèm số lượng nhân viên
SELECT 
    c.id,
    c.name AS 'Tên trung tâm',
    c.address AS 'Địa chỉ',
    c.phone_number AS 'Số điện thoại',
    c.email AS 'Email',
    c.capacity AS 'Sức chứa',
    c.status AS 'Trạng thái',
    COUNT(DISTINCT si.user_id) AS 'Số nhân viên',
    COUNT(DISTINCT CASE WHEN u.role = 'RECEPTIONIST' THEN si.user_id END) AS 'Số lễ tân',
    COUNT(DISTINCT CASE WHEN u.role = 'DOCTOR' THEN si.user_id END) AS 'Số bác sĩ',
    COUNT(DISTINCT CASE WHEN u.role = 'NURSE' THEN si.user_id END) AS 'Số y tá',
    COUNT(DISTINCT a.id) AS 'Số lịch hẹn',
    c.created_at AS 'Ngày tạo'
FROM vaccination_centers c
LEFT JOIN staff_infos si ON si.center_id = c.id
LEFT JOIN users u ON u.id = si.user_id
LEFT JOIN appointments a ON a.center_id = c.id
GROUP BY c.id, c.name, c.address, c.phone_number, c.email, c.capacity, c.status, c.created_at
ORDER BY c.id;

-- Xem trung tâm kèm danh sách nhân viên
SELECT 
    c.id AS 'Trung tâm ID',
    c.name AS 'Tên trung tâm',
    u.id AS 'User ID',
    u.full_name AS 'Họ tên',
    u.email AS 'Email',
    u.role AS 'Vai trò',
    u.status AS 'Trạng thái',
    si.employee_id AS 'Mã nhân viên',
    si.department AS 'Phòng ban'
FROM vaccination_centers c
LEFT JOIN staff_infos si ON si.center_id = c.id
LEFT JOIN users u ON u.id = si.user_id
ORDER BY c.id, u.role, u.full_name;

