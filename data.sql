-- ============================================
-- DỮ LIỆU TEST ĐẦY ĐỦ CHO HỆ THỐNG VACCI CARE
-- Password mặc định cho tất cả user: 12345678
-- BCrypt hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe
-- ============================================

-- ============================================
-- 1. INSERT USERS (Người dùng)
-- ============================================
-- ADMIN
INSERT INTO users (email, password, provider_user_id, auth_provider, full_name, phone_number, phone_verified, day_of_birth, gender, address, citizen_id, role, status, create_at) VALUES
('admin@vaccicare.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Nguyễn Văn Admin', '0901234567', true, '1980-01-15', 'MALE', '123 Đường ABC, Quận 1, TP.HCM', '001234567890', 'ADMIN', 'ACTIVE', CURDATE());

-- DOCTOR
INSERT INTO users (email, password, provider_user_id, auth_provider, full_name, phone_number, phone_verified, day_of_birth, gender, address, citizen_id, role, status, create_at) VALUES
('doctor1@vaccicare.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Bác sĩ Nguyễn Thị Lan', '0901234568', true, '1985-05-20', 'FEMALE', '456 Đường XYZ, Quận 2, TP.HCM', '001234567891', 'DOCTOR', 'ACTIVE', CURDATE()),
('doctor2@vaccicare.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Bác sĩ Trần Văn Minh', '0901234569', true, '1988-08-10', 'MALE', '789 Đường DEF, Quận 3, TP.HCM', '001234567892', 'DOCTOR', 'ACTIVE', CURDATE());

-- NURSE
INSERT INTO users (email, password, provider_user_id, auth_provider, full_name, phone_number, phone_verified, day_of_birth, gender, address, citizen_id, role, status, create_at) VALUES
('nurse1@vaccicare.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Y tá Lê Thị Hoa', '0901234570', true, '1990-03-25', 'FEMALE', '321 Đường GHI, Quận 4, TP.HCM', '001234567893', 'NURSE', 'ACTIVE', CURDATE()),
('nurse2@vaccicare.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Y tá Phạm Văn Tuấn', '0901234571', true, '1992-07-15', 'MALE', '654 Đường JKL, Quận 5, TP.HCM', '001234567894', 'NURSE', 'ACTIVE', CURDATE()),
('nurse3@vaccicare.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Y tá Hoàng Thị Mai', '0901234572', true, '1991-11-30', 'FEMALE', '987 Đường MNO, Quận 6, TP.HCM', '001234567895', 'NURSE', 'ACTIVE', CURDATE());

-- RECEPTIONIST
INSERT INTO users (email, password, provider_user_id, auth_provider, full_name, phone_number, phone_verified, day_of_birth, gender, address, citizen_id, role, status, create_at) VALUES
('receptionist1@vaccicare.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Lễ tân Võ Thị Linh', '0901234573', true, '1993-04-12', 'FEMALE', '147 Đường PQR, Quận 7, TP.HCM', '001234567896', 'RECEPTIONIST', 'ACTIVE', CURDATE()),
('receptionist2@vaccicare.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Lễ tân Đặng Văn Nam', '0901234574', true, '1994-09-22', 'MALE', '258 Đường STU, Quận 8, TP.HCM', '001234567897', 'RECEPTIONIST', 'ACTIVE', CURDATE());

-- CUSTOMER (Người dùng thường)
INSERT INTO users (email, password, provider_user_id, auth_provider, full_name, phone_number, phone_verified, day_of_birth, gender, address, citizen_id, role, status, create_at) VALUES
('user1@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Nguyễn Văn An', '0901234575', true, '1995-01-10', 'MALE', '369 Đường VWX, Quận 9, TP.HCM', '001234567898', 'CUSTOMER', 'ACTIVE', CURDATE()),
('user2@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Trần Thị Bình', '0901234576', true, '1996-06-20', 'FEMALE', '741 Đường YZA, Quận 10, TP.HCM', '001234567899', 'CUSTOMER', 'ACTIVE', CURDATE()),
('user3@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Lê Văn Cường', '0901234577', true, '1997-12-05', 'MALE', '852 Đường BCD, Quận 11, TP.HCM', '001234567900', 'CUSTOMER', 'ACTIVE', CURDATE()),
('user4@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Phạm Thị Dung', '0901234578', true, '1998-03-15', 'FEMALE', '963 Đường EFG, Quận 12, TP.HCM', '001234567901', 'CUSTOMER', 'ACTIVE', CURDATE()),
('user5@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8p6OPe', NULL, 'EMAIL', 'Hoàng Văn Em', '0901234579', true, '1999-08-25', 'MALE', '159 Đường HIJ, Quận Bình Thạnh, TP.HCM', '001234567902', 'CUSTOMER', 'ACTIVE', CURDATE());

-- ============================================
-- 2. INSERT VACCINES (Vaccine)
-- ============================================
INSERT INTO vaccines (name, code, manufacturer, origin, description, price, min_age, max_age, doses_required, days_between_doses, contraindications, storage_temperature, image_url, status, created_at) VALUES
('Vaccine COVID-19 Pfizer-BioNTech', 'COVID19-PFIZER', 'Pfizer-BioNTech', 'Mỹ', 'Vaccine mRNA phòng COVID-19, hiệu quả cao trong việc ngăn ngừa bệnh nặng và tử vong.', 500000.00, 12, NULL, 2, 21, 'Không tiêm cho người có tiền sử dị ứng nặng với bất kỳ thành phần nào của vaccine.', '-70°C đến -80°C', 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400', 'AVAILABLE', NOW()),
('Vaccine COVID-19 Moderna', 'COVID19-MODERNA', 'Moderna', 'Mỹ', 'Vaccine mRNA phòng COVID-19, hiệu quả cao tương tự Pfizer.', 450000.00, 18, NULL, 2, 28, 'Không tiêm cho người dị ứng với polyethylene glycol (PEG).', '-25°C đến -15°C', 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400', 'AVAILABLE', NOW()),
('Vaccine COVID-19 AstraZeneca', 'COVID19-AZ', 'AstraZeneca', 'Anh/Thụy Điển', 'Vaccine vector virus phòng COVID-19, hiệu quả tốt và giá thành hợp lý.', 300000.00, 18, NULL, 2, 84, 'Không tiêm cho người có tiền sử huyết khối tĩnh mạch hoặc giảm tiểu cầu.', '2-8°C', 'https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=400', 'AVAILABLE', NOW()),
('Vaccine Cúm Vaxigrip', 'FLU-VAXIGRIP', 'Sanofi Pasteur', 'Pháp', 'Vaccine phòng cúm mùa, được khuyến nghị tiêm hàng năm cho mọi lứa tuổi từ 6 tháng trở lên.', 250000.00, 0, NULL, 1, NULL, 'Không tiêm cho người dị ứng với trứng hoặc các thành phần của vaccine.', '2-8°C', 'https://images.unsplash.com/photo-1582719471384-894fbb16e074?w=400', 'AVAILABLE', NOW()),
('Vaccine Sởi - Quai bị - Rubella (MMR)', 'MMR', 'Merck', 'Mỹ', 'Vaccine phòng 3 bệnh: Sởi, Quai bị và Rubella. Được tiêm cho trẻ em từ 12 tháng tuổi.', 200000.00, 12, NULL, 2, 28, 'Không tiêm cho phụ nữ mang thai hoặc người suy giảm miễn dịch.', '2-8°C', 'https://images.unsplash.com/photo-1582719471384-894fbb16e074?w=400', 'AVAILABLE', NOW()),
('Vaccine Viêm gan B', 'HEP-B', 'GSK', 'Bỉ', 'Vaccine phòng viêm gan B, được tiêm cho trẻ sơ sinh và người lớn chưa có kháng thể.', 150000.00, 0, NULL, 3, 30, 'Không tiêm cho người dị ứng với nấm men.', '2-8°C', 'https://images.unsplash.com/photo-1582719471384-894fbb16e074?w=400', 'AVAILABLE', NOW()),
('Vaccine HPV Gardasil 9', 'HPV-GARDASIL9', 'Merck', 'Mỹ', 'Vaccine phòng ung thư cổ tử cung và các bệnh liên quan đến HPV. Được khuyến nghị cho cả nam và nữ từ 9-26 tuổi.', 1800000.00, 9, 26, 3, 60, 'Không tiêm cho phụ nữ mang thai.', '2-8°C', 'https://images.unsplash.com/photo-1582719471384-894fbb16e074?w=400', 'AVAILABLE', NOW()),
('Vaccine Bạch hầu - Ho gà - Uốn ván (DPT)', 'DPT', 'Sanofi Pasteur', 'Pháp', 'Vaccine phòng 3 bệnh: Bạch hầu, Ho gà và Uốn ván. Được tiêm cho trẻ em.', 180000.00, 2, NULL, 5, 30, 'Không tiêm cho trẻ đang sốt cao hoặc có phản ứng nặng với liều trước.', '2-8°C', 'https://images.unsplash.com/photo-1582719471384-894fbb16e074?w=400', 'AVAILABLE', NOW());

-- ============================================
-- 3. INSERT VACCINATION_CENTERS (Trung tâm tiêm chủng)
-- ============================================
INSERT INTO vaccination_centers (name, address, phone_number, email, capacity, status, created_at) VALUES
('Trung tâm Y tế Quận 1', '123 Đường Nguyễn Du, Phường Bến Nghé, Quận 1, TP.HCM', '02838291234', 'ttyt.quan1@hcm.gov.vn', 200, 'ACTIVE', NOW()),
('Trung tâm Y tế Dự phòng TP.HCM', '699 Trần Hưng Đạo, Phường 1, Quận 5, TP.HCM', '02839231234', 'ttytdp@hcm.gov.vn', 300, 'ACTIVE', NOW()),
('Trung tâm Y tế Quận Bình Thạnh', '123 Đường Xô Viết Nghệ Tĩnh, Phường 21, Quận Bình Thạnh, TP.HCM', '02838401234', 'ttyt.binhthanh@hcm.gov.vn', 170, 'ACTIVE', NOW()),
('Trung tâm Y tế Quận Tân Bình', '456 Đường Cộng Hòa, Phường 13, Quận Tân Bình, TP.HCM', '02838121234', 'ttyt.tanbinh@hcm.gov.vn', 160, 'ACTIVE', NOW()),
('Bệnh viện Nhi Đồng 1', '341 Sư Vạn Hạnh, Phường 10, Quận 10, TP.HCM', '02839271119', 'info@nhidong1.org.vn', 250, 'ACTIVE', NOW());

-- ============================================
-- 4. INSERT VACCINE_LOTS (Lô vaccine)
-- ============================================
INSERT INTO vaccine_lots (lot_number, vaccine_id, quantity, remaining_quantity, manufacturing_date, expiry_date, supplier, import_date, status, created_at)
SELECT 'LOT-PFIZER-2024-001', v.id, 1000, 850, '2024-01-15', '2025-07-15', 'Pfizer Vietnam', '2024-02-01', 'AVAILABLE', NOW()
FROM vaccines v WHERE v.code = 'COVID19-PFIZER'
UNION ALL
SELECT 'LOT-PFIZER-2024-002', v.id, 1500, 1200, '2024-03-10', '2025-09-10', 'Pfizer Vietnam', '2024-03-25', 'AVAILABLE', NOW()
FROM vaccines v WHERE v.code = 'COVID19-PFIZER'
UNION ALL
SELECT 'LOT-MODERNA-2024-001', v.id, 800, 600, '2024-02-01', '2025-08-01', 'Moderna Distribution', '2024-02-20', 'AVAILABLE', NOW()
FROM vaccines v WHERE v.code = 'COVID19-MODERNA'
UNION ALL
SELECT 'LOT-AZ-2024-001', v.id, 2000, 1800, '2024-01-20', '2025-07-20', 'AstraZeneca Vietnam', '2024-02-10', 'AVAILABLE', NOW()
FROM vaccines v WHERE v.code = 'COVID19-AZ'
UNION ALL
SELECT 'LOT-VAXIGRIP-2024-001', v.id, 5000, 4200, '2024-01-10', '2025-01-10', 'Sanofi Pasteur Vietnam', '2024-01-25', 'AVAILABLE', NOW()
FROM vaccines v WHERE v.code = 'FLU-VAXIGRIP'
UNION ALL
SELECT 'LOT-MMR-2024-001', v.id, 3000, 2800, '2024-01-01', '2026-01-01', 'Merck Vietnam', '2024-01-15', 'AVAILABLE', NOW()
FROM vaccines v WHERE v.code = 'MMR'
UNION ALL
SELECT 'LOT-HEPB-2024-001', v.id, 4000, 3800, '2024-01-01', '2026-01-01', 'GSK Vietnam', '2024-01-10', 'AVAILABLE', NOW()
FROM vaccines v WHERE v.code = 'HEP-B'
UNION ALL
SELECT 'LOT-HPV-G9-2024-001', v.id, 2500, 2300, '2024-01-01', '2026-01-01', 'Merck Vietnam', '2024-01-20', 'AVAILABLE', NOW()
FROM vaccines v WHERE v.code = 'HPV-GARDASIL9'
UNION ALL
SELECT 'LOT-DPT-2024-001', v.id, 3500, 3300, '2024-01-01', '2026-01-01', 'Sanofi Pasteur Vietnam', '2024-01-05', 'AVAILABLE', NOW()
FROM vaccines v WHERE v.code = 'DPT';

-- ============================================
-- 5. INSERT CENTER_VACCINES (Vaccine có tại trung tâm)
-- ============================================
INSERT INTO center_vaccines (center_id, vaccine_id, stock_quantity, last_restocked)
SELECT c.id, v.id, 150, DATE_SUB(NOW(), INTERVAL 30 DAY)
FROM vaccination_centers c, vaccines v
WHERE c.name = 'Trung tâm Y tế Quận 1' AND v.code = 'COVID19-PFIZER'
UNION ALL
SELECT c.id, v.id, 120, DATE_SUB(NOW(), INTERVAL 20 DAY)
FROM vaccination_centers c, vaccines v
WHERE c.name = 'Trung tâm Y tế Quận 1' AND v.code = 'COVID19-MODERNA'
UNION ALL
SELECT c.id, v.id, 200, DATE_SUB(NOW(), INTERVAL 15 DAY)
FROM vaccination_centers c, vaccines v
WHERE c.name = 'Trung tâm Y tế Quận 1' AND v.code = 'FLU-VAXIGRIP'
UNION ALL
SELECT c.id, v.id, 100, DATE_SUB(NOW(), INTERVAL 25 DAY)
FROM vaccination_centers c, vaccines v
WHERE c.name = 'Trung tâm Y tế Dự phòng TP.HCM' AND v.code = 'COVID19-PFIZER'
UNION ALL
SELECT c.id, v.id, 180, DATE_SUB(NOW(), INTERVAL 10 DAY)
FROM vaccination_centers c, vaccines v
WHERE c.name = 'Trung tâm Y tế Dự phòng TP.HCM' AND v.code = 'MMR'
UNION ALL
SELECT c.id, v.id, 90, DATE_SUB(NOW(), INTERVAL 35 DAY)
FROM vaccination_centers c, vaccines v
WHERE c.name = 'Trung tâm Y tế Quận Bình Thạnh' AND v.code = 'HEP-B'
UNION ALL
SELECT c.id, v.id, 80, DATE_SUB(NOW(), INTERVAL 40 DAY)
FROM vaccination_centers c, vaccines v
WHERE c.name = 'Trung tâm Y tế Quận Tân Bình' AND v.code = 'HPV-GARDASIL9'
UNION ALL
SELECT c.id, v.id, 200, DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM vaccination_centers c, vaccines v
WHERE c.name = 'Bệnh viện Nhi Đồng 1' AND v.code = 'DPT';

-- ============================================
-- 6. INSERT APPOINTMENT_SLOTS (Khung giờ hẹn)
-- ============================================
INSERT INTO appointment_slots (center_id, date, start_time, end_time, max_capacity, current_bookings, is_available, created_at)
SELECT c.id, CURDATE(), '08:00:00', '08:30:00', 10, 0, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, CURDATE(), '08:30:00', '09:00:00', 10, 0, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, CURDATE(), '09:00:00', '09:30:00', 10, 2, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, CURDATE(), '09:30:00', '10:00:00', 10, 1, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, CURDATE(), '10:00:00', '10:30:00', 10, 0, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '08:30:00', 10, 0, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:30:00', '09:00:00', 10, 0, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '09:30:00', 10, 0, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Dự phòng TP.HCM'
UNION ALL
SELECT c.id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00', '10:00:00', 10, 0, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Dự phòng TP.HCM'
UNION ALL
SELECT c.id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', '10:30:00', 10, 0, true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Dự phòng TP.HCM';

-- ============================================
-- 7. INSERT APPOINTMENTS (Lịch hẹn)
-- ============================================
-- Appointments với các trạng thái khác nhau
-- PENDING - Chờ xác nhận
INSERT INTO appointments (booking_code, booked_by_user_id, booked_for_user_id, family_member_id, vaccine_id, center_id, slot_id, appointment_date, appointment_time, dose_number, status, notes, requires_consultation, consultation_phone, queue_number, created_at, updated_at)
SELECT 
    CONCAT('BK', DATE_FORMAT(NOW(), '%Y%m%d'), '001'),
    u1.id, u1.id, NULL,
    v1.id,
    c1.id,
    s1.id,
    CURDATE(), '09:00:00', 1, 'PENDING', 'Lần đầu tiêm COVID-19', false, NULL, NULL, NOW(), NOW()
FROM users u1, vaccines v1, vaccination_centers c1, appointment_slots s1
WHERE u1.email = 'user1@test.com' AND v1.code = 'COVID19-PFIZER' AND c1.name = 'Trung tâm Y tế Quận 1' 
    AND s1.center_id = c1.id AND s1.date = CURDATE() AND s1.start_time = '09:00:00'
LIMIT 1;

-- CONFIRMED - Đã xác nhận
INSERT INTO appointments (booking_code, booked_by_user_id, booked_for_user_id, family_member_id, vaccine_id, center_id, slot_id, appointment_date, appointment_time, dose_number, status, notes, requires_consultation, consultation_phone, queue_number, created_at, updated_at)
SELECT 
    CONCAT('BK', DATE_FORMAT(NOW(), '%Y%m%d'), '002'),
    u2.id, u2.id, NULL,
    v2.id,
    c1.id,
    s2.id,
    CURDATE(), '09:30:00', 1, 'CONFIRMED', NULL, false, NULL, 1, NOW(), NOW()
FROM users u2, vaccines v2, vaccination_centers c1, appointment_slots s2
WHERE u2.email = 'user2@test.com' AND v2.code = 'COVID19-MODERNA' AND c1.name = 'Trung tâm Y tế Quận 1'
    AND s2.center_id = c1.id AND s2.date = CURDATE() AND s2.start_time = '09:30:00'
LIMIT 1;

-- CHECKED_IN - Đã check-in
INSERT INTO appointments (booking_code, booked_by_user_id, booked_for_user_id, family_member_id, vaccine_id, center_id, slot_id, appointment_date, appointment_time, dose_number, status, notes, requires_consultation, consultation_phone, queue_number, created_at, updated_at)
SELECT 
    CONCAT('BK', DATE_FORMAT(NOW(), '%Y%m%d'), '003'),
    u3.id, u3.id, NULL,
    v3.id,
    c1.id,
    NULL,
    CURDATE(), '10:00:00', 1, 'CHECKED_IN', NULL, false, NULL, 2, NOW(), NOW()
FROM users u3, vaccines v3, vaccination_centers c1
WHERE u3.email = 'user3@test.com' AND v3.code = 'FLU-VAXIGRIP' AND c1.name = 'Trung tâm Y tế Quận 1'
LIMIT 1;

-- SCREENING - Đang khám sàng lọc
INSERT INTO appointments (booking_code, booked_by_user_id, booked_for_user_id, family_member_id, vaccine_id, center_id, slot_id, appointment_date, appointment_time, dose_number, status, notes, requires_consultation, consultation_phone, queue_number, created_at, updated_at)
SELECT 
    CONCAT('BK', DATE_FORMAT(NOW(), '%Y%m%d'), '004'),
    u4.id, u4.id, NULL,
    v1.id,
    c1.id,
    NULL,
    CURDATE(), '10:30:00', 1, 'SCREENING', NULL, false, NULL, 3, NOW(), NOW()
FROM users u4, vaccines v1, vaccination_centers c1
WHERE u4.email = 'user4@test.com' AND v1.code = 'COVID19-PFIZER' AND c1.name = 'Trung tâm Y tế Quận 1'
LIMIT 1;

-- APPROVED - Đủ điều kiện tiêm
INSERT INTO appointments (booking_code, booked_by_user_id, booked_for_user_id, family_member_id, vaccine_id, center_id, slot_id, appointment_date, appointment_time, dose_number, status, notes, requires_consultation, consultation_phone, queue_number, created_at, updated_at)
SELECT 
    CONCAT('BK', DATE_FORMAT(NOW(), '%Y%m%d'), '005'),
    u5.id, u5.id, NULL,
    v2.id,
    c1.id,
    NULL,
    CURDATE(), '11:00:00', 1, 'APPROVED', NULL, false, NULL, 4, NOW(), NOW()
FROM users u5, vaccines v2, vaccination_centers c1
WHERE u5.email = 'user5@test.com' AND v2.code = 'COVID19-MODERNA' AND c1.name = 'Trung tâm Y tế Quận 1'
LIMIT 1;

-- COMPLETED - Hoàn thành (đã tiêm)
INSERT INTO appointments (booking_code, booked_by_user_id, booked_for_user_id, family_member_id, vaccine_id, center_id, slot_id, appointment_date, appointment_time, dose_number, status, notes, requires_consultation, consultation_phone, queue_number, created_at, updated_at)
SELECT 
    CONCAT('BK', DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 DAY), '%Y%m%d'), '001'),
    u1.id, u1.id, NULL,
    v3.id,
    c2.id,
    NULL,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY), '14:00:00', 1, 'COMPLETED', NULL, false, NULL, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM users u1, vaccines v3, vaccination_centers c2
WHERE u1.email = 'user1@test.com' AND v3.code = 'FLU-VAXIGRIP' AND c2.name = 'Trung tâm Y tế Dự phòng TP.HCM'
LIMIT 1;

-- CANCELLED - Đã hủy
INSERT INTO appointments (booking_code, booked_by_user_id, booked_for_user_id, family_member_id, vaccine_id, center_id, slot_id, appointment_date, appointment_time, dose_number, status, notes, requires_consultation, consultation_phone, queue_number, created_at, updated_at)
SELECT 
    CONCAT('BK', DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY), '%Y%m%d'), '001'),
    u2.id, u2.id, NULL,
    v1.id,
    c1.id,
    NULL,
    DATE_SUB(CURDATE(), INTERVAL 2 DAY), '08:00:00', 1, 'CANCELLED', 'Khách hàng hủy do bận việc', false, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM users u2, vaccines v1, vaccination_centers c1
WHERE u2.email = 'user2@test.com' AND v1.code = 'COVID19-PFIZER' AND c1.name = 'Trung tâm Y tế Quận 1'
LIMIT 1;

-- ============================================
-- 8. INSERT VACCINATION_RECORDS (Hồ sơ tiêm chủng)
-- ============================================
INSERT INTO vaccination_records (appointment_id, user_id, vaccine_id, vaccine_lot_id, nurse_id, injection_date, injection_time, dose_number, injection_site, batch_number, dose_amount, certificate_number, next_dose_date, created_at)
SELECT 
    a.id,
    u1.id,
    v.id,
    vl.id,
    n.id,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY), '14:30:00', 1, 'LEFT_ARM', 'LOT-VAXIGRIP-2024-001', 0.5, 
    CONCAT('CERT-', DATE_FORMAT(NOW(), '%Y%m%d'), '-001'), NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM appointments a, users u1, vaccines v, vaccine_lots vl, users n
WHERE a.status = 'COMPLETED' AND u1.email = 'user1@test.com' AND v.code = 'FLU-VAXIGRIP'
    AND vl.lot_number = 'LOT-VAXIGRIP-2024-001' AND n.email = 'nurse1@vaccicare.com'
LIMIT 1;

-- ============================================
-- 9. INSERT ADVERSE_REACTIONS (Phản ứng phụ)
-- ============================================
INSERT INTO adverse_reactions (vaccination_record_id, reaction_type, symptoms, occurred_at, handled_by, treatment, resolved, notes)
SELECT 
    vr.id,
    'MILD',
    'Đau nhẹ tại vị trí tiêm, sốt nhẹ 37.5°C',
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    NULL,
    NULL,
    false,
    'Triệu chứng nhẹ, theo dõi tại nhà'
FROM vaccination_records vr
LIMIT 1;

INSERT INTO adverse_reactions (vaccination_record_id, reaction_type, symptoms, occurred_at, handled_by, treatment, resolved, notes)
SELECT 
    vr.id,
    'MODERATE',
    'Sốt 38.5°C, đau đầu, mệt mỏi',
    DATE_SUB(NOW(), INTERVAL 18 HOUR),
    n.id,
    'Uống thuốc hạ sốt, nghỉ ngơi',
    true,
    'Đã xử lý và theo dõi'
FROM vaccination_records vr, users n
WHERE n.email = 'nurse1@vaccicare.com'
LIMIT 1;

-- ============================================
-- 10. INSERT FAMILY_MEMBERS (Thành viên gia đình)
-- ============================================
INSERT INTO family_members (user_id, full_name, phone_number, date_of_birth, gender, relationship, citizen_id, created_at)
SELECT u.id, 'Nguyễn Văn Bố', '0901234580', '1970-05-15', 'MALE', 'PARENT', '001234567910', NOW()
FROM users u WHERE u.email = 'user1@test.com'
UNION ALL
SELECT u.id, 'Nguyễn Thị Mẹ', '0901234581', '1972-08-20', 'FEMALE', 'PARENT', '001234567911', NOW()
FROM users u WHERE u.email = 'user1@test.com'
UNION ALL
SELECT u.id, 'Trần Văn Con', '0901234582', '2015-03-10', 'MALE', 'CHILD', NULL, NOW()
FROM users u WHERE u.email = 'user2@test.com';

-- ============================================
-- 11. INSERT PAYMENTS (Thanh toán)
-- ============================================
INSERT INTO payments (appointment_id, amount, payment_method, payment_status, transaction_id, paid_at)
SELECT a.id, 500000.00, 'VNPAY', 'PENDING', NULL, NULL
FROM appointments a WHERE a.status = 'PENDING' LIMIT 1;

INSERT INTO payments (appointment_id, amount, payment_method, payment_status, transaction_id, paid_at)
SELECT a.id, 450000.00, 'CASH', 'PAID', 'CASH-001', NOW()
FROM appointments a WHERE a.status = 'CONFIRMED' LIMIT 1;

INSERT INTO payments (appointment_id, amount, payment_method, payment_status, transaction_id, paid_at)
SELECT a.id, 250000.00, 'CASH', 'PAID', 'CASH-002', NOW()
FROM appointments a WHERE a.status = 'CHECKED_IN' LIMIT 1;

-- ============================================
-- 12. INSERT CLINIC_ROOMS (Phòng khám)
-- ============================================
INSERT INTO clinic_rooms (center_id, room_number, description, is_active, created_at)
SELECT c.id, 'P101', 'Phòng khám sàng lọc số 1', true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, 'P102', 'Phòng khám sàng lọc số 2', true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, 'P201', 'Phòng tiêm chủng số 1', true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, 'P202', 'Phòng tiêm chủng số 2', true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'
UNION ALL
SELECT c.id, 'P101', 'Phòng khám sàng lọc số 1', true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Dự phòng TP.HCM'
UNION ALL
SELECT c.id, 'P201', 'Phòng tiêm chủng số 1', true, NOW()
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Dự phòng TP.HCM';

-- ============================================
-- HOÀN TẤT
-- ============================================
-- Dữ liệu test đã được tạo đầy đủ với:
-- - 1 ADMIN
-- - 2 DOCTOR
-- - 3 NURSE
-- - 2 RECEPTIONIST
-- - 5 CUSTOMER
-- - 8 Vaccines
-- - 5 Centers
-- - 10 Vaccine Lots
-- - 8 Center Vaccines
-- - 10 Appointment Slots
-- - 7 Appointments (với các trạng thái khác nhau)
-- - 1 Vaccination Record
-- - 2 Adverse Reactions
-- - 3 Family Members
-- - 3 Payments
-- - 6 Clinic Rooms

