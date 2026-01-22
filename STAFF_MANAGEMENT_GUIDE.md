# HƯỚNG DẪN CHỨC NĂNG TRANG QUẢN LÝ NHÂN VIÊN (`/admin/staff`)

Tài liệu này mô tả các chức năng cần có cho trang quản lý nhân viên, bao gồm việc gán nhân viên vào các trung tâm tiêm chủng.

---

## 📍 TRANG: QUẢN LÝ NHÂN VIÊN (`/admin/staff`)

### Mục đích
Quản lý nhân viên trong hệ thống (Bác sĩ, Y tá, Lễ tân) và gán nhân viên vào các trung tâm tiêm chủng để đảm bảo hồ sơ được gửi đúng đến bác sĩ của trung tâm đó.

---

## 🎯 CÁC CHỨC NĂNG CHÍNH

### 1. **Danh sách Nhân viên**

#### Hiển thị bảng danh sách
- **Cột hiển thị:**
  - Mã nhân viên (Employee ID)
  - Họ và tên
  - Email
  - Số điện thoại
  - Vai trò (DOCTOR / NURSE / RECEPTIONIST) - có badge màu phân biệt
  - Trạng thái (ACTIVE / INACTIVE / LOCKED) - có badge màu
  - Trung tâm làm việc (hiển thị danh sách trung tâm, nếu có nhiều thì hiển thị "X trung tâm")
  - Ngày vào làm
  - Hành động (Xem chi tiết, Gán trung tâm, Sửa, Khóa/Mở khóa)

#### Tính năng bổ sung
- **Sắp xếp:** Có thể sắp xếp theo tên, ngày vào làm, vai trò
- **Phân trang:** Hiển thị 10-20 nhân viên/trang
- **Export:** Nút xuất Excel danh sách nhân viên

---

### 2. **Tìm kiếm & Lọc**

#### Tìm kiếm
- Tìm kiếm theo:
  - Họ và tên
  - Email
  - Mã nhân viên
  - Số điện thoại

#### Lọc
- **Theo vai trò:** DOCTOR / NURSE / RECEPTIONIST (dropdown)
- **Theo trạng thái:** ACTIVE / INACTIVE / LOCKED (dropdown)
- **Theo trung tâm:** Dropdown chọn trung tâm (hiển thị nhân viên của trung tâm đó)
- **Theo ngày vào làm:** Từ ngày - Đến ngày (date picker)

#### Kết hợp
- Có thể kết hợp nhiều bộ lọc cùng lúc
- Nút "Xóa bộ lọc" để reset về mặc định

---

### 3. **CRUD Nhân viên**

#### 3.1. **Thêm Nhân viên mới**
Form modal với các trường:

**Thông tin cơ bản:**
- Họ và tên (bắt buộc)
- Email (bắt buộc, unique)
- Số điện thoại (bắt buộc)
- Mật khẩu (bắt buộc, tối thiểu 6 ký tự)
- Ngày sinh (date picker)
- Giới tính (MALE / FEMALE / OTHER)
- Địa chỉ
- CMND/CCCD (unique nếu có)

**Thông tin nhân viên:**
- Vai trò (bắt buộc): DOCTOR / NURSE / RECEPTIONIST (dropdown)
- Mã nhân viên (bắt buộc, unique, tự động generate hoặc nhập thủ công)
- Chuyên khoa (chỉ cho DOCTOR, ví dụ: "Nhi khoa", "Y tế công cộng")
- Số chứng chỉ hành nghề (chỉ cho DOCTOR)
- Ngày vào làm (bắt buộc, date picker)
- Phòng ban (optional)

**Gán trung tâm:**
- Multi-select dropdown chọn trung tâm (có thể chọn nhiều)
- Checkbox "Trung tâm chính" (chỉ được chọn 1 trung tâm chính)
- Hiển thị danh sách trung tâm đã chọn với nút xóa

**Validation:**
- Email phải unique
- Mã nhân viên phải unique
- Phải chọn ít nhất 1 trung tâm
- Nếu là DOCTOR, bắt buộc nhập chuyên khoa và số chứng chỉ

#### 3.2. **Sửa Nhân viên**
- Form tương tự như thêm mới, pre-fill dữ liệu hiện có
- **Không cho sửa:** Email, Mã nhân viên (hoặc chỉ admin mới sửa được)
- **Có thể sửa:** Tất cả thông tin khác, bao gồm trung tâm làm việc
- **Mật khẩu:** Có nút "Đổi mật khẩu" riêng (không hiển thị trong form sửa)

#### 3.3. **Xóa/Khóa Nhân viên**
- **Khóa tài khoản:** Nút "Khóa" cho nhân viên ACTIVE
  - Có confirm dialog: "Bạn có chắc muốn khóa tài khoản này?"
  - Sau khi khóa, nhân viên không thể đăng nhập
- **Mở khóa:** Nút "Mở khóa" cho nhân viên LOCKED
- **Xóa:** Chỉ admin mới có quyền xóa
  - Cảnh báo: "Không thể hoàn tác. Tất cả dữ liệu liên quan sẽ bị xóa."
  - Kiểm tra ràng buộc: Nếu nhân viên có appointments/screenings đang xử lý, không cho xóa

---

### 4. **Gán Nhân viên vào Trung tâm**

#### 4.1. **Gán từ danh sách**
- Nút "Gán trung tâm" trên mỗi hàng
- Modal hiển thị:
  - Thông tin nhân viên (tên, vai trò)
  - Danh sách trung tâm hiện tại (nếu có)
  - Multi-select dropdown chọn trung tâm mới
  - Checkbox "Trung tâm chính" cho mỗi trung tâm đã chọn
  - Nút "Xóa" để gỡ nhân viên khỏi trung tâm

#### 4.2. **Gán hàng loạt**
- Checkbox chọn nhiều nhân viên
- Nút "Gán trung tâm hàng loạt"
- Modal chọn trung tâm và gán cho tất cả nhân viên đã chọn

#### 4.3. **Quản lý từ trang chi tiết trung tâm**
- Trong trang quản lý trung tâm (`/admin/centers`), có tab "Nhân viên"
- Hiển thị danh sách nhân viên của trung tâm đó
- Có nút "Thêm nhân viên" để gán thêm nhân viên vào trung tâm

---

### 5. **Xem Chi tiết Nhân viên**

Modal/Tab hiển thị:

#### Tab 1: Thông tin cá nhân
- Tất cả thông tin cơ bản và nhân viên
- Lịch sử thay đổi (nếu có audit log)

#### Tab 2: Trung tâm làm việc
- Danh sách trung tâm nhân viên đang làm việc
- Hiển thị: Tên trung tâm, Trạng thái (Chính/Phụ), Ngày bắt đầu làm việc
- Nút: Thêm trung tâm, Xóa khỏi trung tâm, Đặt làm trung tâm chính

#### Tab 3: Lịch làm việc
- Hiển thị lịch làm việc của nhân viên tại các trung tâm
- Có thể xem theo tuần/tháng
- Hiển thị: Ngày, Trung tâm, Ca làm việc, Giờ bắt đầu - Kết thúc

#### Tab 4: Hiệu suất làm việc (chỉ cho DOCTOR/NURSE)
- **Cho DOCTOR:**
  - Tổng số khám sàng lọc
  - Số khám đã duyệt
  - Số khám từ chối
  - Tỷ lệ duyệt (%)
  - Biểu đồ thống kê theo thời gian
- **Cho NURSE:**
  - Tổng số tiêm đã thực hiện
  - Số phản ứng phụ đã xử lý
  - Biểu đồ thống kê theo thời gian

#### Tab 5: Lịch sử hoạt động
- Danh sách appointments/screenings/vaccination records
- Có thể filter theo trung tâm, thời gian

---

### 6. **Thống kê**

Card hiển thị ở đầu trang:

- **Tổng số nhân viên:** Tổng số nhân viên trong hệ thống
- **Theo vai trò:**
  - Số bác sĩ
  - Số y tá
  - Số lễ tân
- **Theo trạng thái:**
  - Đang hoạt động (ACTIVE)
  - Đã khóa (LOCKED)
  - Ngừng hoạt động (INACTIVE)
- **Nhân viên chưa gán trung tâm:** Số nhân viên chưa được gán vào trung tâm nào (cảnh báo)
- **Phân bổ theo trung tâm:** Biểu đồ pie chart hiển thị số nhân viên mỗi trung tâm

---

### 7. **Import/Export**

#### Export
- Nút "Xuất Excel" để xuất danh sách nhân viên
- Bao gồm: Tất cả thông tin cơ bản, trung tâm làm việc
- Có thể filter trước khi xuất

#### Import
- Nút "Nhập từ Excel" để import nhiều nhân viên cùng lúc
- Template Excel mẫu có thể download
- Validation khi import:
  - Email phải unique
  - Mã nhân viên phải unique
  - Format đúng
- Preview trước khi import
- Báo cáo kết quả import (thành công/thất bại)

---

## 🔌 API ENDPOINTS CẦN TẠO

### Quản lý Nhân viên

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/admin/staff` | Lấy danh sách nhân viên (có filter: role, status, centerId, search) |
| GET | `/api/admin/staff/{id}` | Lấy chi tiết nhân viên |
| POST | `/api/admin/staff` | Tạo nhân viên mới (bao gồm gán trung tâm) |
| PUT | `/api/admin/staff/{id}` | Cập nhật thông tin nhân viên |
| DELETE | `/api/admin/staff/{id}` | Xóa nhân viên (kiểm tra ràng buộc) |
| PUT | `/api/admin/staff/{id}/status` | Khóa/Mở khóa tài khoản |
| PUT | `/api/admin/staff/{id}/password` | Đổi mật khẩu |

### Gán Nhân viên vào Trung tâm

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/admin/staff/{staffId}/centers` | Lấy danh sách trung tâm của nhân viên |
| POST | `/api/admin/staff-centers` | Gán nhân viên vào trung tâm |
| PUT | `/api/admin/staff-centers/{id}` | Cập nhật (đặt trung tâm chính) |
| DELETE | `/api/admin/staff-centers/{id}` | Gỡ nhân viên khỏi trung tâm |
| POST | `/api/admin/staff-centers/bulk` | Gán hàng loạt nhân viên vào trung tâm |
| GET | `/api/admin/centers/{centerId}/staff` | Lấy danh sách nhân viên của trung tâm |

### Thống kê

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/admin/staff/stats` | Lấy thống kê nhân viên |
| GET | `/api/admin/staff/{id}/performance` | Lấy hiệu suất làm việc của nhân viên |

---

## 📊 CẤU TRÚC DATABASE

### Bảng `staff_centers` (CẦN TẠO)

```sql
CREATE TABLE staff_centers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    center_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE, -- Trung tâm chính
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT, -- Admin gán
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (center_id) REFERENCES vaccination_centers(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_staff_center (user_id, center_id),
    INDEX idx_center_id (center_id),
    INDEX idx_user_id (user_id)
);
```

---

## 🎨 YÊU CẦU UI/UX

### Design System
- **Primary Color:** `#13ec5b` (green)
- **Text Colors:** 
  - Primary: `#111813` (dark)
  - Secondary: `#61896f` (green-gray)
- **Background:** `#f6f8f6` (light), `#102216` (dark)
- **Status Colors:**
  - ACTIVE: Green
  - INACTIVE: Gray
  - LOCKED: Red
  - DOCTOR: Blue badge
  - NURSE: Purple badge
  - RECEPTIONIST: Orange badge

### Components cần có
1. **Header:** Sticky header với title và nút "Thêm nhân viên"
2. **Filters:** Search bar + dropdown filters (role, status, center)
3. **Table:** Responsive table với:
   - Hover effects
   - Checkbox để chọn nhiều
   - Badge cho role và status
   - Action buttons với tooltip
4. **Modals:**
   - Modal thêm/sửa nhân viên
   - Modal gán trung tâm
   - Modal xem chi tiết (với tabs)
   - Modal confirm xóa/khóa
5. **Status Chips:** Badge hiển thị trạng thái và vai trò
6. **Action Buttons:** Icon buttons với tooltip
7. **Loading States:** Skeleton loaders hoặc spinners
8. **Empty States:** Message khi không có dữ liệu
9. **Error Messages:** Toast notifications hoặc inline errors
10. **Success Messages:** Toast notifications

### Responsive Design
- **Mobile:** Stack layout, hamburger menu
- **Tablet:** 2-column layout
- **Desktop:** Full layout với sidebar

### Dark Mode
- Hỗ trợ dark mode
- Toggle switch trong header hoặc settings

---

## ✅ CHECKLIST KHI TẠO UI

- [ ] Tạo file HTML template với cấu trúc cơ bản
- [ ] Thêm route vào `AdminController.java`
- [ ] Thêm link vào `admin-sidebar.html`
- [ ] Tạo Entity `StaffCenter.java`
- [ ] Tạo Repository `StaffCenterRepository.java`
- [ ] Tạo Service `StaffCenterService.java`
- [ ] Tạo Controller `StaffCenterController.java` hoặc thêm vào `AdminRestController.java`
- [ ] Implement các chức năng CRUD nhân viên
- [ ] Implement chức năng gán nhân viên vào trung tâm
- [ ] Implement search và filter
- [ ] Implement modals
- [ ] Implement thống kê
- [ ] Test tất cả chức năng
- [ ] Kiểm tra responsive design
- [ ] Kiểm tra dark mode
- [ ] Thêm error handling
- [ ] Thêm loading states
- [ ] Thêm empty states
- [ ] Thêm success/error messages
- [ ] Tạo migration SQL cho bảng `staff_centers`
- [ ] Tạo script SQL để gán nhân viên hiện có vào trung tâm

---

## 📝 LƯU Ý QUAN TRỌNG

1. **Security:** Đảm bảo các API endpoints có kiểm tra quyền ADMIN
2. **Validation:** 
   - Validate input ở cả client và server
   - Email và mã nhân viên phải unique
   - Phải chọn ít nhất 1 trung tâm khi tạo nhân viên
3. **Business Logic:**
   - Một nhân viên có thể làm việc ở nhiều trung tâm
   - Chỉ có 1 trung tâm chính (is_primary = true)
   - Khi gán nhân viên vào trung tâm, tự động tạo notification cho nhân viên đó
   - Khi user đăng ký và chọn trung tâm, hồ sơ sẽ được gửi đến các doctor của trung tâm đó
4. **Error Handling:** Luôn xử lý lỗi và hiển thị thông báo rõ ràng
5. **Performance:** Nếu dữ liệu nhiều, cần pagination hoặc virtual scrolling
6. **Accessibility:** Đảm bảo UI accessible (keyboard navigation, screen readers)
7. **Consistency:** Giữ consistency với các trang admin hiện có (`manager_staff.html`, `manager_user.html`, etc.)

---

## 🔄 WORKFLOW GÁN NHÂN VIÊN VÀO TRUNG TÂM

1. **Admin tạo nhân viên mới:**
   - Điền thông tin cơ bản
   - Chọn vai trò (DOCTOR/NURSE/RECEPTIONIST)
   - Chọn trung tâm làm việc (có thể chọn nhiều)
   - Đặt trung tâm chính
   - Lưu → Tạo user + StaffInfo + StaffCenter records

2. **Admin gán nhân viên hiện có vào trung tâm:**
   - Chọn nhân viên từ danh sách
   - Click "Gán trung tâm"
   - Chọn trung tâm (có thể chọn nhiều)
   - Đặt trung tâm chính
   - Lưu → Tạo StaffCenter records

3. **Khi user đăng ký và chọn trung tâm:**
   - User chọn trung tâm trong form đăng ký
   - Tạo appointment với center_id
   - Hệ thống tự động tìm các doctor của trung tâm đó
   - Gửi notification đến các doctor đó

---

**Chúc bạn tạo UI thành công! 🎨**

