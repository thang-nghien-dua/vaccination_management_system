-- ============================================
-- CÁC CÂU LỆNH SQL ĐỂ XEM NHÂN VIÊN TRONG TRUNG TÂM
-- ============================================

-- 1. Xem TẤT CẢ nhân viên trong TẤT CẢ các trung tâm
-- Hiển thị: Tên, Email, Số điện thoại, Vai trò, Mã nhân viên, Tên trung tâm, Phòng ban
SELECT 
    u.full_name AS 'Họ và tên',
    u.email AS 'Email',
    u.phone_number AS 'Số điện thoại',
    u.role AS 'Vai trò',
    si.employee_id AS 'Mã nhân viên',
    vc.name AS 'Trung tâm',
    si.department AS 'Phòng ban',
    si.specialization AS 'Chuyên khoa',
    si.hire_date AS 'Ngày vào làm',
    u.status AS 'Trạng thái'
FROM users u
INNER JOIN staff_infos si ON u.id = si.user_id
LEFT JOIN vaccination_centers vc ON si.center_id = vc.id
WHERE u.role IN ('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')
ORDER BY vc.name, u.role, u.full_name;

-- 2. Xem nhân viên trong MỘT trung tâm cụ thể (thay 'Trung tâm Y tế Quận 1' bằng tên trung tâm bạn muốn)
SELECT 
    u.full_name AS 'Họ và tên',
    u.email AS 'Email',
    u.phone_number AS 'Số điện thoại',
    u.role AS 'Vai trò',
    si.employee_id AS 'Mã nhân viên',
    vc.name AS 'Trung tâm',
    si.department AS 'Phòng ban',
    si.specialization AS 'Chuyên khoa',
    si.hire_date AS 'Ngày vào làm',
    u.status AS 'Trạng thái'
FROM users u
INNER JOIN staff_infos si ON u.id = si.user_id
INNER JOIN vaccination_centers vc ON si.center_id = vc.id
WHERE vc.name = 'Trung tâm Y tế Quận 1'
  AND u.role IN ('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')
ORDER BY u.role, u.full_name;

-- 3. Xem nhân viên theo VAI TRÒ trong một trung tâm
-- Ví dụ: Xem tất cả BÁC SĨ trong trung tâm
SELECT 
    u.full_name AS 'Họ và tên',
    u.email AS 'Email',
    u.phone_number AS 'Số điện thoại',
    si.employee_id AS 'Mã nhân viên',
    vc.name AS 'Trung tâm',
    si.specialization AS 'Chuyên khoa',
    si.license_number AS 'Số chứng chỉ',
    si.hire_date AS 'Ngày vào làm'
FROM users u
INNER JOIN staff_infos si ON u.id = si.user_id
INNER JOIN vaccination_centers vc ON si.center_id = vc.id
WHERE u.role = 'DOCTOR'
  AND vc.name = 'Trung tâm Y tế Quận 1'
ORDER BY u.full_name;

-- 4. Xem nhân viên theo VAI TRÒ (tất cả trung tâm)
-- Ví dụ: Xem tất cả Y TÁ
SELECT 
    u.full_name AS 'Họ và tên',
    u.email AS 'Email',
    u.phone_number AS 'Số điện thoại',
    si.employee_id AS 'Mã nhân viên',
    vc.name AS 'Trung tâm',
    si.department AS 'Phòng ban',
    si.hire_date AS 'Ngày vào làm'
FROM users u
INNER JOIN staff_infos si ON u.id = si.user_id
LEFT JOIN vaccination_centers vc ON si.center_id = vc.id
WHERE u.role = 'NURSE'
ORDER BY vc.name, u.full_name;

-- 5. Đếm số lượng nhân viên theo trung tâm
SELECT 
    vc.name AS 'Trung tâm',
    COUNT(*) AS 'Tổng số nhân viên',
    SUM(CASE WHEN u.role = 'DOCTOR' THEN 1 ELSE 0 END) AS 'Số bác sĩ',
    SUM(CASE WHEN u.role = 'NURSE' THEN 1 ELSE 0 END) AS 'Số y tá',
    SUM(CASE WHEN u.role = 'RECEPTIONIST' THEN 1 ELSE 0 END) AS 'Số lễ tân',
    SUM(CASE WHEN u.role = 'ADMIN' THEN 1 ELSE 0 END) AS 'Số admin'
FROM users u
INNER JOIN staff_infos si ON u.id = si.user_id
INNER JOIN vaccination_centers vc ON si.center_id = vc.id
WHERE u.role IN ('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')
GROUP BY vc.id, vc.name
ORDER BY vc.name;

-- 6. Xem nhân viên CHƯA được gán vào trung tâm nào (center_id IS NULL)
SELECT 
    u.full_name AS 'Họ và tên',
    u.email AS 'Email',
    u.phone_number AS 'Số điện thoại',
    u.role AS 'Vai trò',
    si.employee_id AS 'Mã nhân viên',
    si.department AS 'Phòng ban',
    si.hire_date AS 'Ngày vào làm',
    u.status AS 'Trạng thái'
FROM users u
INNER JOIN staff_infos si ON u.id = si.user_id
WHERE u.role IN ('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')
  AND si.center_id IS NULL
ORDER BY u.role, u.full_name;

-- 7. Xem chi tiết nhân viên theo ID trung tâm (thay 1 bằng ID trung tâm bạn muốn)
SELECT 
    u.id AS 'User ID',
    u.full_name AS 'Họ và tên',
    u.email AS 'Email',
    u.phone_number AS 'Số điện thoại',
    u.role AS 'Vai trò',
    u.status AS 'Trạng thái',
    si.employee_id AS 'Mã nhân viên',
    si.specialization AS 'Chuyên khoa',
    si.license_number AS 'Số chứng chỉ',
    si.hire_date AS 'Ngày vào làm',
    si.department AS 'Phòng ban',
    vc.id AS 'Center ID',
    vc.name AS 'Trung tâm',
    vc.address AS 'Địa chỉ trung tâm',
    vc.phone_number AS 'SĐT trung tâm'
FROM users u
INNER JOIN staff_infos si ON u.id = si.user_id
INNER JOIN vaccination_centers vc ON si.center_id = vc.id
WHERE vc.id = 1
  AND u.role IN ('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')
ORDER BY u.role, u.full_name;

