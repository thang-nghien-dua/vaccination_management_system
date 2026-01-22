-- ============================================
-- INSERT APPOINTMENT SLOTS CHO NGÀY 24/01/2026
-- Trung tâm Y tế Quận 1
-- ============================================

-- Lấy ID của Trung tâm Y tế Quận 1
-- (Thay thế 1 bằng ID thực tế của trung tâm nếu khác)

-- Insert các slot từ 8:00 đến 17:00, mỗi slot 30 phút
INSERT INTO appointment_slots (center_id, date, start_time, end_time, max_capacity, current_bookings, is_available, created_at, room_id)
SELECT 
    c.id, 
    '2026-01-24', 
    '08:00:00', 
    '08:30:00', 
    10, 
    0, 
    true, 
    NOW(),
    NULL
FROM vaccination_centers c 
WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '08:30:00', '09:00:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '09:00:00', '09:30:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '09:30:00', '10:00:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '10:00:00', '10:30:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '10:30:00', '11:00:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '11:00:00', '11:30:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '11:30:00', '12:00:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '13:00:00', '13:30:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '13:30:00', '14:00:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '14:00:00', '14:30:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '14:30:00', '15:00:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '15:00:00', '15:30:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '15:30:00', '16:00:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '16:00:00', '16:30:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1'

UNION ALL

SELECT c.id, '2026-01-24', '16:30:00', '17:00:00', 10, 0, true, NOW(), NULL
FROM vaccination_centers c WHERE c.name = 'Trung tâm Y tế Quận 1';

-- ============================================
-- KIỂM TRA DỮ LIỆU ĐÃ INSERT
-- ============================================
-- Chạy câu lệnh này để xem các slot đã được tạo:
-- SELECT 
--     s.id,
--     s.date,
--     s.start_time,
--     s.end_time,
--     s.max_capacity,
--     s.current_bookings,
--     s.is_available,
--     c.name AS center_name
-- FROM appointment_slots s
-- INNER JOIN vaccination_centers c ON s.center_id = c.id
-- WHERE s.date = '2026-01-24' AND c.name = 'Trung tâm Y tế Quận 1'
-- ORDER BY s.start_time;

