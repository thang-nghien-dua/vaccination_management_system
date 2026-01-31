# 💉 VacciCare - Hệ Thống Quản Lý Tiêm Chủng

<div align="center">

**Hệ thống quản lý tiêm chủng toàn diện** với giao diện hiện đại, hỗ trợ đầy đủ các chức năng từ đặt lịch, sàng lọc, tiêm chủng đến quản lý hồ sơ và báo cáo.

[Tính năng](#-tính-năng-chính) • [Cài đặt](#-cài-đặt-và-chạy-dự-án) • [Tài khoản test](#-tài-khoản-demo) • [Công nghệ](#-công-nghệ-sử-dụng) • [Cấu trúc dự án](#-cấu-trúc-dự-án)

</div>

---

## 📋 Mục Lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng chính](#-tính-năng-chính)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt và chạy dự án](#-cài-đặt-và-chạy-dự-án)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Tài khoản Demo](#-tài-khoản-demo)
- [API Endpoints](#-api-endpoints)
- [Thanh toán VNPay](#-tích-hợp-thanh-toán-vnpay)

---

## 🌟 Giới Thiệu

**VacciCare** là hệ thống quản lý tiêm chủng được xây dựng trên nền tảng Spring Boot, cung cấp giải pháp chuyển đổi số toàn diện cho các trung tâm tiêm chủng. Hệ thống giúp tối ưu hóa quy trình từ khâu đăng ký, khám sàng lọc đến tiêm chủng và theo dõi sau tiêm.

### 🎯 Mục tiêu dự án
- **Số hóa quy trình**: Loại bỏ giấy tờ, quản lý mọi thứ trên nền tảng số.
- **An toàn tiêm chủng**: Kiểm tra tương thích vaccine và lịch sử tiêm của người dùng.
- **Tiện lợi cho khách hàng**: Đặt lịch online, xem lịch sử tiêm và nhận thông báo nhắc lịch.
- **Quản lý thông minh**: Báo cáo thống kê trực quan cho người quản lý.

---

## ✨ Tính Năng Chính

### 👤 **Khách hàng (Customer)**
- **Đăng nhập đa phương thức**: Hỗ trợ Email/Mật khẩu truyền thống và Đăng nhập nhanh qua Google/Facebook.
- **Đặt lịch tiêm thông minh**: Chọn vaccine theo yêu cầu, chọn trung tâm và khung giờ tiêm phù hợp.
- **Quản lý hộ gia đình**: Thêm và quản lý thông tin tiêm chủng cho người thân (con cái, cha mẹ).
- **Thanh toán trực tuyến**: Tích hợp cổng thanh toán VNPay thuận tiện và an toàn.
- **Sổ tiêm chủng điện tử**: Theo dõi toàn bộ lịch sử các mũi đã tiêm và thời gian tiêm mũi kế tiếp.

### 👨‍⚕️ **Bác sĩ (Doctor)**
- **Khám sàng lọc**: Ghi nhận tình trạng sức khỏe bệnh nhân trước khi tiêm.
- **Chỉ định tiêm chủng**: Quyết định bệnh nhân đủ điều kiện tiêm hoặc hoãn tiêm dựa trên kết quả khám.
- **Lịch sử sàng lọc**: Truy xuất thông tin các lần khám trước đó của bệnh nhân.

### 💉 **Y tá (Nurse)**
- **Quản lý tiêm chủng**: Ghi nhận chi tiết quá trình tiêm (vị trí tiêm, liều lượng, số lô vaccine).
- **Kiểm kho vaccine**: Theo dõi số lượng vaccine thực tế tại trung tâm.
- **Theo dõi sau tiêm**: Ghi nhận các phản ứng phụ (nếu có) sau khi tiêm.

### 🏥 **Lễ tân (Receptionist)**
- **Tiếp đón khách hàng**: Xác nhận lịch hẹn qua mã QR hoặc mã đặt chỗ.
- **Tiếp nhận khách vãng lai**: Đăng ký và tạo hồ sơ cho khách hàng chưa đặt lịch trước.
- **Thu ngân**: Xử lý thanh toán tại quầy bằng tiền mặt hoặc chuyển khoản.

### 🛡️ **Quản trị viên (Admin)**
- **Dashboard thống kê**: Biểu đồ trực quan về doanh thu, số lượng mũi tiêm và tình hình hoạt động.
- **Quản lý danh mục**: Vaccine, Loại bệnh, Trung tâm tiêm chủng, Khung giờ (Slots).
- **Cấu hình hệ thống**: Quản lý ưu đãi (Promotions), Giờ làm việc của các trung tâm.
- **Quản trị nhân sự**: CRUD tài khoản nhân viên và phân công công tác.

---

## 🛠 Công Nghệ Sử Dụng

### Backend (Xử lý máy chủ)
- **Ngôn ngữ**: Java 17
- **Framework**: Spring Boot 4.0.1
- **Bảo mật**: Spring Security (Quản lý phiên đăng nhập và phân quyền roles)
- **Kết nối DB**: Spring Data JPA (Sử dụng Hibernate)
- **Đăng nhập MXH**: OAuth2 Client (Google & Facebook)
- **Gửi Email**: Spring Mail (Thông báo đặt lịch thành công, mã OTP)

### Frontend (Giao diện người dùng)
- **Template Engine**: Thymeleaf (Hiển thị dữ liệu động từ server)
- **Styling**: Tailwind CSS (Thiết kế giao diện hiện đại, responsive)
- **Icons**: Material Symbols (Bộ icon chuyên dụng của Google)
- **Logic Client**: JavaScript Vanilla (Xử lý các tương tác mượt mà không cần load lại trang)

### Database & Tools (Dữ liệu & Công cụ)
- **Hệ quản trị DB**: MySQL 8.0
- **Tạo QR Code**: Thư viện ZXing
- **Thanh toán**: Cổng thanh toán VNPay
- **Quản lý dự án**: Maven

---

## 💻 Yêu Cầu Hệ Thống

- **Java JDK**: Phiên bản 17 hoặc mới hơn.
- **MySQL Server**: Phiên bản 8.0 trở lên.
- **Trình duyệt**: Chrome, Firefox, Edge phiên bản mới nhất.

---

## 🚀 Hướng Dẫn Cài Đặt

### 1. Chuẩn bị Cơ sở dữ liệu
```sql
CREATE DATABASE vaccination_management_system;
```
Import dữ liệu từ file `data.sql` để có cấu hình ban đầu và các tài khoản demo.

### 2. Cấu hình ứng dụng
Mở file `src/main/resources/application.properties` và cập nhật thông tin MySQL:
```properties
spring.datasource.username=root
spring.datasource.password=Mật_khẩu_MySQL_của_bạn
```

### 3. Chạy ứng dụng
Sử dụng terminal tại thư mục gốc của dự án:
```bash
# Sử dụng Maven Wrapper
.\mvnw spring-boot:run
```

Sau khi ứng dụng khởi chạy thành công, truy cập: `http://localhost:8080`

---

## � Tài Khoản Demo (Mật khẩu: 12345678)

| Vai trò | Email đăng nhập |
|---------|-----------------|
| **Quản trị viên** | `admin@vaccicare.com` |
| **Bác sĩ** | `doctor1@vaccicare.com` |
| **Y tá** | `nurse1@vaccicare.com` |
| **Lễ tân** | `receptionist1@vaccicare.com` |
| **Khách hàng** | `user1@test.com` |

---

## 🔌 API Endpoints Chính

- **Auth**: `/login`, `/register`, `/forgot-password`
- **Profiles**: `/api/users/profile`
- **Appointments**: `/api/appointments` (Đặt lịch, hủy lịch)
- **Vaccines**: `/api/vaccines` (Tra cứu danh mục vaccine)
- **Payments**: `/api/payment/vnpay-return`

---

## � Thông Tin Khác

Dự án được thực hiện bởi sinh viên trường **Đại học Giao thông Vận tải TP.HCM (UTH)** nhằm mục đích cung cấp giải pháp y tế số hiện đại.

<div align="center">

Made with ❤️ by UTH Students

</div>
