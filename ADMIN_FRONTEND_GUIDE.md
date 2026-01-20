# HƯỚNG DẪN CHỨC NĂNG CHO CÁC TRANG ADMIN123123

Tài liệu này mô tả các chức năng cần có cho 4 trang admin mới. Dựa vào đây để tạo UI/UX phù hợp.

---

## 📍 TRANG 1: QUẢN LÝ TRUNG TÂM TIÊM CHỦNG (`/admin/centers`)

### Mục đích
Quản lý các trung tâm tiêm chủng trong hệ thống, bao gồm thông tin cơ bản, vaccine có tại trung tâm, phòng khám, và giờ làm việc.

### Các chức năng cần có

#### 1. **Danh sách Trung tâm**
- Hiển thị bảng danh sách tất cả trung tâm
- Cột hiển thị: Tên, Địa chỉ, Số điện thoại, Email, Sức chứa, Trạng thái
- Có nút hành động: Xem chi tiết, Sửa, Xóa

#### 2. **Tìm kiếm & Lọc**
- Tìm kiếm theo: Tên trung tâm, Địa chỉ
- Lọc theo: Trạng thái (Hoạt động / Ngừng hoạt động)

#### 3. **CRUD Trung tâm**
- **Thêm mới:** Form với các trường:
  - Tên trung tâm (bắt buộc)
  - Địa chỉ
  - Số điện thoại
  - Email
  - Sức chứa (người/ngày)
  - Trạng thái (ACTIVE/INACTIVE)
- **Sửa:** Form tương tự, pre-fill dữ liệu hiện có
- **Xóa:** Có confirm dialog, cảnh báo không thể hoàn tác

#### 4. **Xem Chi tiết Trung tâm** (Modal/Tab)
Khi click "Xem chi tiết", hiển thị modal với các tab:

**Tab 1: Vaccine tại Trung tâm**
- Danh sách vaccine có tại trung tâm
- Hiển thị: Tên vaccine, Số lượng tồn kho, Lần nhập cuối
- Nút: Thêm vaccine, Cập nhật số lượng, Xóa vaccine

**Tab 2: Phòng khám**
- Danh sách phòng khám trong trung tâm
- Hiển thị: Số phòng, Mô tả, Trạng thái (ACTIVE/INACTIVE)
- Nút: Thêm phòng, Sửa, Xóa

**Tab 3: Giờ làm việc**
- Danh sách giờ làm việc theo ngày trong tuần
- Hiển thị: Thứ, Giờ bắt đầu, Giờ kết thúc
- Nút: Thêm giờ làm việc, Sửa, Xóa

#### 5. **Thống kê** (Optional)
- Tổng số trung tâm
- Số trung tâm đang hoạt động
- Số trung tâm ngừng hoạt động

### API Endpoints cần dùng

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/centers` | Lấy danh sách tất cả trung tâm |
| GET | `/api/centers/{id}` | Lấy chi tiết trung tâm |
| POST | `/api/centers` | Tạo trung tâm mới |
| PUT | `/api/centers/{id}` | Cập nhật trung tâm |
| DELETE | `/api/centers/{id}` | Xóa trung tâm |
| GET | `/api/centers/{centerId}/vaccines` | Lấy vaccine tại trung tâm |
| POST | `/api/centers/{centerId}/vaccines` | Thêm vaccine vào trung tâm |
| PUT | `/api/center-vaccines/{id}` | Cập nhật số lượng vaccine |
| DELETE | `/api/center-vaccines/{id}` | Xóa vaccine khỏi trung tâm |
| GET | `/api/center-working-hours/center/{centerId}` | Lấy giờ làm việc |
| POST | `/api/center-working-hours` | Tạo giờ làm việc |
| PUT | `/api/center-working-hours/{id}` | Cập nhật giờ làm việc |
| DELETE | `/api/center-working-hours/{id}` | Xóa giờ làm việc |

**Lưu ý:** Cần tạo API cho quản lý phòng khám (xem Trang 3)

---

## 📍 TRANG 2: QUẢN LÝ PHẢN ỨNG PHỤ (`/admin/adverse-reactions`)

### Mục đích
Theo dõi và quản lý các phản ứng phụ sau tiêm vaccine, đảm bảo an toàn cho người dùng.

### Các chức năng cần có

#### 1. **Danh sách Phản ứng Phụ**
- Hiển thị bảng danh sách tất cả phản ứng
- Cột hiển thị:
  - Người dùng (tên)
  - Vaccine (tên vaccine đã tiêm)
  - Mức độ (Nhẹ/Trung bình/Nặng) - có màu sắc phân biệt
  - Triệu chứng
  - Thời gian xảy ra
  - Trạng thái (Chưa xử lý / Đã xử lý)
  - Người xử lý (nếu đã xử lý)
- Có nút hành động: Xem chi tiết, Đánh dấu đã xử lý

#### 2. **Tìm kiếm & Lọc**
- Tìm kiếm theo: Tên người dùng, Triệu chứng
- Lọc theo:
  - Mức độ (MILD / MODERATE / SEVERE)
  - Trạng thái (Chưa xử lý / Đã xử lý)
  - Vaccine (dropdown chọn vaccine)
  - Trung tâm (dropdown chọn trung tâm)
  - Khoảng thời gian (từ ngày - đến ngày)

#### 3. **Xem Chi tiết Phản ứng**
- Modal hiển thị đầy đủ thông tin:
  - Thông tin người dùng
  - Thông tin vaccine đã tiêm
  - Mức độ phản ứng
  - Triệu chứng chi tiết
  - Thời gian xảy ra
  - Trạng thái xử lý
  - Phương pháp điều trị (nếu có)
  - Ghi chú
  - Người xử lý và thời gian xử lý (nếu đã xử lý)

#### 4. **Đánh dấu Đã xử lý**
- Nút "Đánh dấu đã xử lý" cho các phản ứng chưa xử lý
- Form nhập:
  - Phương pháp điều trị (optional)
  - Ghi chú (optional)
- Sau khi xử lý, cập nhật trạng thái và hiển thị người xử lý

#### 5. **Thống kê Phản ứng**
- Card hiển thị:
  - Tổng số phản ứng
  - Số phản ứng chưa xử lý
  - Số phản ứng đã xử lý
  - Tỷ lệ phản ứng theo mức độ (biểu đồ)
  - Top 5 vaccine có nhiều phản ứng nhất
  - Top 5 trung tâm có nhiều phản ứng nhất

#### 6. **Xuất Báo cáo**
- Nút "Xuất báo cáo"
- Cho phép xuất Excel/PDF với:
  - Danh sách phản ứng (có thể filter)
  - Thống kê tổng hợp
  - Biểu đồ phân tích

### API Endpoints cần dùng

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/adverse-reactions` | **CẦN TẠO** - Lấy danh sách phản ứng (có filter) |
| GET | `/api/adverse-reactions/{id}` | **CẦN TẠO** - Lấy chi tiết phản ứng |
| POST | `/api/adverse-reactions` | Tạo phản ứng (NURSE) |
| PUT | `/api/adverse-reactions/{id}/resolve` | Đánh dấu đã xử lý |
| GET | `/api/adverse-reactions/stats` | **CẦN TẠO** - Lấy thống kê |
| GET | `/api/adverse-reactions/export` | **CẦN TẠO** - Xuất báo cáo |

**Lưu ý:** Cần thêm các GET endpoints vào `AdverseReactionController.java`

---

## 📍 TRANG 3: QUẢN LÝ PHÒNG KHÁM (`/admin/clinic-rooms`)

### Mục đích
Quản lý các phòng khám trong các trung tâm tiêm chủng, phân bổ phòng cho các lịch hẹn.

### Các chức năng cần có

#### 1. **Danh sách Phòng khám**
- Hiển thị bảng danh sách tất cả phòng khám
- Cột hiển thị:
  - Trung tâm (tên trung tâm)
  - Số phòng
  - Mô tả
  - Trạng thái (ACTIVE/INACTIVE)
  - Số lịch hẹn đã sử dụng (thống kê)
- Có nút hành động: Sửa, Xóa

#### 2. **Tìm kiếm & Lọc**
- Tìm kiếm theo: Số phòng, Mô tả
- Lọc theo:
  - Trung tâm (dropdown chọn trung tâm)
  - Trạng thái (ACTIVE/INACTIVE)

#### 3. **CRUD Phòng khám**
- **Thêm mới:** Form với các trường:
  - Trung tâm (dropdown chọn trung tâm) - bắt buộc
  - Số phòng (bắt buộc, ví dụ: P101, P102)
  - Mô tả (ví dụ: "Phòng tiêm chủng số 1")
  - Trạng thái (ACTIVE/INACTIVE) - mặc định ACTIVE
- **Sửa:** Form tương tự, pre-fill dữ liệu hiện có
- **Xóa:** Có confirm dialog, cảnh báo không thể hoàn tác

#### 4. **Xem Chi tiết Phòng** (Optional)
- Modal hiển thị:
  - Thông tin phòng
  - Danh sách lịch hẹn đã sử dụng phòng này
  - Thống kê sử dụng (theo ngày/tuần/tháng)

#### 5. **Thống kê** (Optional)
- Tổng số phòng
- Số phòng đang hoạt động
- Số phòng ngừng hoạt động
- Phòng được sử dụng nhiều nhất

### API Endpoints cần dùng

**CẦN TẠO** `ClinicRoomController.java`:

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/clinic-rooms` | Lấy danh sách phòng (có filter centerId, isActive) |
| GET | `/api/clinic-rooms/{id}` | Lấy chi tiết phòng |
| POST | `/api/clinic-rooms` | Tạo phòng mới |
| PUT | `/api/clinic-rooms/{id}` | Cập nhật phòng |
| DELETE | `/api/clinic-rooms/{id}` | Xóa phòng |
| GET | `/api/clinic-rooms/{id}/appointments` | **CẦN TẠO** - Lấy lịch hẹn đã sử dụng phòng |
| GET | `/api/clinic-rooms/stats` | **CẦN TẠO** - Lấy thống kê |

---

## 📍 TRANG 4: QUẢN LÝ THÔNG BÁO (`/admin/notifications`)

### Mục đích
Quản lý và gửi thông báo đến người dùng trong hệ thống, theo dõi tỷ lệ đọc và hiệu quả thông báo.

### Các chức năng cần có

#### 1. **Danh sách Thông báo**
- Hiển thị bảng danh sách tất cả thông báo (ADMIN có thể xem tất cả)
- Cột hiển thị:
  - Người nhận (tên user)
  - Loại thông báo (APPOINTMENT_REMINDER, SYSTEM_ANNOUNCEMENT, etc.)
  - Tiêu đề
  - Nội dung (rút gọn)
  - Trạng thái gửi (SENT, PENDING, FAILED)
  - Đã đọc (Yes/No) - có icon phân biệt
  - Thời gian gửi
- Có nút hành động: Xem chi tiết, Xóa

#### 2. **Tìm kiếm & Lọc**
- Tìm kiếm theo: Tên người nhận, Tiêu đề, Nội dung
- Lọc theo:
  - Loại thông báo
  - Trạng thái gửi
  - Đã đọc / Chưa đọc
  - Khoảng thời gian gửi

#### 3. **Gửi Thông báo Đơn lẻ**
- Form gửi thông báo:
  - Người nhận (dropdown chọn user hoặc search user) - bắt buộc
  - Loại thông báo (dropdown) - bắt buộc
  - Tiêu đề (bắt buộc)
  - Nội dung (textarea, bắt buộc)
  - Liên kết với lịch hẹn (optional, nếu là APPOINTMENT_REMINDER)
- Nút "Gửi ngay" hoặc "Lên lịch gửi"

#### 4. **Gửi Thông báo Hàng loạt**
- Form gửi cho nhiều người:
  - Chọn đối tượng:
    - Tất cả người dùng
    - Theo role (CUSTOMER, DOCTOR, NURSE, etc.)
    - Theo trung tâm
    - Danh sách user cụ thể (multi-select)
  - Loại thông báo
  - Tiêu đề
  - Nội dung
  - Có thể dùng template (dropdown chọn template)
- Preview số lượng người sẽ nhận
- Nút "Gửi hàng loạt"

#### 5. **Quản lý Template**
- Danh sách template thông báo
- Có thể: Tạo mới, Sửa, Xóa template
- Template có các biến động: `{userName}`, `{appointmentDate}`, etc.

#### 6. **Thống kê Thông báo**
- Card hiển thị:
  - Tổng số thông báo đã gửi
  - Số thông báo đã đọc
  - Số thông báo chưa đọc
  - Tỷ lệ đọc (%)
  - Số thông báo theo loại (biểu đồ)
  - Thống kê theo thời gian (biểu đồ line)

#### 7. **Xem Chi tiết Thông báo**
- Modal hiển thị:
  - Thông tin đầy đủ thông báo
  - Thời gian gửi và đọc
  - Trạng thái
  - Nếu có liên kết lịch hẹn, hiển thị thông tin lịch hẹn

### API Endpoints cần dùng

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/notifications/admin/all` | **CẦN TẠO** - Lấy tất cả thông báo (ADMIN) |
| GET | `/api/notifications` | Lấy thông báo của user hiện tại |
| GET | `/api/notifications/user/{userId}` | Lấy thông báo của user cụ thể |
| POST | `/api/notifications` | Tạo thông báo đơn lẻ |
| POST | `/api/notifications/bulk` | **CẦN TẠO** - Gửi thông báo hàng loạt |
| PUT | `/api/notifications/{id}/read` | Đánh dấu đã đọc |
| PUT | `/api/notifications/mark-all-read` | Đánh dấu tất cả đã đọc |
| DELETE | `/api/notifications/{id}` | Xóa thông báo |
| GET | `/api/notifications/templates` | **CẦN TẠO** - Lấy danh sách template |
| POST | `/api/notifications/templates` | **CẦN TẠO** - Tạo template |
| PUT | `/api/notifications/templates/{id}` | **CẦN TẠO** - Cập nhật template |
| DELETE | `/api/notifications/templates/{id}` | **CẦN TẠO** - Xóa template |
| GET | `/api/notifications/stats` | **CẦN TẠO** - Lấy thống kê |

---

## 🎨 YÊU CẦU UI/UX CHUNG

### Design System
- **Primary Color:** `#13ec5b` (green)
- **Text Colors:** 
  - Primary: `#111813` (dark)
  - Secondary: `#61896f` (green-gray)
- **Background:** `#f6f8f6` (light), `#102216` (dark)
- **Status Colors:**
  - Active/Success: Green
  - Warning: Yellow/Orange
  - Error/Danger: Red
  - Info: Blue

### Components cần có
1. **Header:** Sticky header với title và notification icon
2. **Filters:** Search bar + dropdown filters
3. **Table:** Responsive table với hover effects
4. **Modals:** Modal cho CRUD và chi tiết
5. **Status Chips:** Badge hiển thị trạng thái
6. **Action Buttons:** Icon buttons với tooltip
7. **Loading States:** Skeleton loaders hoặc spinners
8. **Empty States:** Message khi không có dữ liệu
9. **Error Messages:** Toast notifications hoặc inline errors
10. **Success Messages:** Toast notifications

### Responsive Design
- Mobile: Stack layout, hamburger menu
- Tablet: 2-column layout
- Desktop: Full layout với sidebar

### Dark Mode
- Hỗ trợ dark mode
- Toggle switch trong header hoặc settings

---

## ✅ CHECKLIST KHI TẠO UI

- [ ] Tạo file HTML template với cấu trúc cơ bản
- [ ] Thêm route vào `AdminController.java`
- [ ] Thêm link vào `admin-sidebar.html`
- [ ] Tạo/kiểm tra API endpoints
- [ ] Implement các chức năng CRUD
- [ ] Implement search và filter
- [ ] Implement modals
- [ ] Implement thống kê (nếu có)
- [ ] Test tất cả chức năng
- [ ] Kiểm tra responsive design
- [ ] Kiểm tra dark mode
- [ ] Thêm error handling
- [ ] Thêm loading states
- [ ] Thêm empty states
- [ ] Thêm success/error messages

---

## 📝 LƯU Ý QUAN TRỌNG

1. **Security:** Đảm bảo các API endpoints có kiểm tra quyền ADMIN
2. **Validation:** Validate input ở cả client và server
3. **Error Handling:** Luôn xử lý lỗi và hiển thị thông báo rõ ràng
4. **Performance:** Nếu dữ liệu nhiều, cần pagination hoặc virtual scrolling
5. **Accessibility:** Đảm bảo UI accessible (keyboard navigation, screen readers)
6. **Consistency:** Giữ consistency với các trang admin hiện có (`manager_vaccin.html`, `manager_user.html`, etc.)

---

**Chúc bạn tạo UI thành công! 🎨**
PENDING - Chờ xác nhận
CONFIRMED - Đã xác nhận
CHECKED_IN - Đã check-in tại quầy lễ tân
SCREENING - Đang khám sàng lọc
APPROVED - Đủ điều kiện tiêm
REJECTED - Từ chối
INJECTING - Đang tiêm vaccine
MONITORING - Đang theo dõi sau tiêm
COMPLETED - Hoàn thành
CANCELLED - Đã hủy
RESCHEDULED - Đã đổi lịch