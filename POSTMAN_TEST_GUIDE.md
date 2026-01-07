# Hướng Dẫn Test API với Postman

## 1. Chuẩn bị

### Khởi động ứng dụng
```bash
mvn spring-boot:run
```
Ứng dụng sẽ chạy tại: `http://localhost:8080`

### Cấu hình Postman
- Base URL: `http://localhost:8080`
- Headers: `Content-Type: application/json`

---

## 2. CRUD VaccinationCenter

### 2.1. GET /api/centers - Xem danh sách trung tâm

**Method:** `GET`  
**URL:** `http://localhost:8080/api/centers`  
**Headers:** Không cần  
**Body:** Không cần

**Response thành công (200):**
```json
[
  {
    "id": 1,
    "name": "Trung tâm Y tế Quận 1",
    "address": "123 Đường ABC",
    "phoneNumber": "0123456789",
    "email": "center1@example.com",
    "capacity": 100,
    "status": "ACTIVE",
    "createdAt": "2024-01-01T10:00:00"
  }
]
```

---

### 2.2. GET /api/centers/{id} - Chi tiết trung tâm

**Method:** `GET`  
**URL:** `http://localhost:8080/api/centers/1`  
**Headers:** Không cần  
**Body:** Không cần

**Response thành công (200):**
```json
{
  "id": 1,
  "name": "Trung tâm Y tế Quận 1",
  "address": "123 Đường ABC",
  "phoneNumber": "0123456789",
  "email": "center1@example.com",
  "capacity": 100,
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00"
}
```

**Response lỗi (404):**
```json
{
  "error": "Vaccination center not found with id: 999"
}
```

---

### 2.3. POST /api/centers - Tạo trung tâm mới

**Method:** `POST`  
**URL:** `http://localhost:8080/api/centers`  
**Headers:** 
- `Content-Type: application/json`

**Body (raw JSON):**
```json
{
  "name": "Trung tâm Y tế Quận 2",
  "address": "456 Đường XYZ",
  "phoneNumber": "0987654321",
  "email": "center2@example.com",
  "capacity": 150,
  "status": "ACTIVE"
}
```

**Response thành công (201):**
```json
{
  "id": 2,
  "name": "Trung tâm Y tế Quận 2",
  "address": "456 Đường XYZ",
  "phoneNumber": "0987654321",
  "email": "center2@example.com",
  "capacity": 150,
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00"
}
```

**Response lỗi (400) - Thiếu field bắt buộc:**
```json
{
  "error": "Center name is required"
}
```

---

### 2.4. PUT /api/centers/{id} - Cập nhật trung tâm

**Method:** `PUT`  
**URL:** `http://localhost:8080/api/centers/1`  
**Headers:** 
- `Content-Type: application/json`

**Body (raw JSON):**
```json
{
  "name": "Trung tâm Y tế Quận 1 (Đã cập nhật)",
  "address": "789 Đường Mới",
  "phoneNumber": "0111222333",
  "email": "center1-updated@example.com",
  "capacity": 200,
  "status": "ACTIVE"
}
```

**Response thành công (200):**
```json
{
  "id": 1,
  "name": "Trung tâm Y tế Quận 1 (Đã cập nhật)",
  "address": "789 Đường Mới",
  "phoneNumber": "0111222333",
  "email": "center1-updated@example.com",
  "capacity": 200,
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00"
}
```

**Lưu ý:** Có thể chỉ cập nhật một số field:
```json
{
  "name": "Tên mới",
  "capacity": 250
}
```

---

### 2.5. DELETE /api/centers/{id} - Xóa trung tâm

**Method:** `DELETE`  
**URL:** `http://localhost:8080/api/centers/1`  
**Headers:** Không cần  
**Body:** Không cần

**Response thành công (200):**
```json
{
  "message": "Vaccination center deleted successfully"
}
```

**Response lỗi (404):**
```json
{
  "error": "Vaccination center not found with id: 999"
}
```

---

## 3. CRUD CenterVaccine (Phân bổ vaccine cho trung tâm)

### 3.1. GET /api/centers/{centerId}/vaccines - Xem vaccine có tại trung tâm

**Method:** `GET`  
**URL:** `http://localhost:8080/api/centers/1/vaccines`  
**Headers:** Không cần  
**Body:** Không cần

**Response thành công (200):**
```json
[
  {
    "id": 1,
    "center": {
      "id": 1,
      "name": "Trung tâm Y tế Quận 1"
    },
    "vaccine": {
      "id": 1,
      "name": "Vaccine COVID-19",
      "code": "VAC001"
    },
    "stockQuantity": 500,
    "lastRestocked": "2024-01-01T10:00:00"
  }
]
```

---

### 3.2. POST /api/centers/{centerId}/vaccines - Thêm vaccine vào trung tâm

**Method:** `POST`  
**URL:** `http://localhost:8080/api/centers/1/vaccines`  
**Headers:** 
- `Content-Type: application/json`

**Body (raw JSON):**
```json
{
  "vaccineId": 1,
  "stockQuantity": 500
}
```

**Response thành công (201):**
```json
{
  "id": 1,
  "center": {
    "id": 1,
    "name": "Trung tâm Y tế Quận 1"
  },
  "vaccine": {
    "id": 1,
    "name": "Vaccine COVID-19",
    "code": "VAC001"
  },
  "stockQuantity": 500,
  "lastRestocked": "2024-01-01T10:00:00"
}
```

**Response lỗi (400) - Vaccine đã tồn tại:**
```json
{
  "error": "Vaccine already exists in this center"
}
```

**Response lỗi (400) - Thiếu vaccineId:**
```json
{
  "error": "Vaccine ID is required"
}
```

---

### 3.3. PUT /api/center-vaccines/{id} - Cập nhật số lượng

**Method:** `PUT`  
**URL:** `http://localhost:8080/api/center-vaccines/1`  
**Headers:** 
- `Content-Type: application/json`

**Body (raw JSON):**
```json
{
  "stockQuantity": 750
}
```

**Hoặc cập nhật cả vaccine:**
```json
{
  "vaccineId": 2,
  "stockQuantity": 300
}
```

**Response thành công (200):**
```json
{
  "id": 1,
  "center": {
    "id": 1,
    "name": "Trung tâm Y tế Quận 1"
  },
  "vaccine": {
    "id": 1,
    "name": "Vaccine COVID-19",
    "code": "VAC001"
  },
  "stockQuantity": 750,
  "lastRestocked": "2024-01-01T11:00:00"
}
```

**Lưu ý:** `lastRestocked` sẽ tự động cập nhật khi thay đổi `stockQuantity`

---

### 3.4. DELETE /api/center-vaccines/{id} - Xóa vaccine khỏi trung tâm

**Method:** `DELETE`  
**URL:** `http://localhost:8080/api/center-vaccines/1`  
**Headers:** Không cần  
**Body:** Không cần

**Response thành công (200):**
```json
{
  "message": "Center vaccine deleted successfully"
}
```

**Response lỗi (404):**
```json
{
  "error": "Center vaccine not found with id: 999"
}
```

---

## 4. Các Trường Hợp Test Quan Trọng

### Test Validation
1. **Thiếu field bắt buộc:** Gửi request thiếu `name` hoặc `status` → Expect 400
2. **ID không tồn tại:** GET/PUT/DELETE với ID không có → Expect 404
3. **Trùng lặp:** Thêm vaccine đã có vào trung tâm → Expect 400

### Test Flow Hoàn Chỉnh
1. Tạo trung tâm mới (POST /api/centers)
2. Xem danh sách trung tâm (GET /api/centers)
3. Xem chi tiết trung tâm (GET /api/centers/{id})
4. Thêm vaccine vào trung tâm (POST /api/centers/{centerId}/vaccines)
5. Xem vaccine tại trung tâm (GET /api/centers/{centerId}/vaccines)
6. Cập nhật số lượng (PUT /api/center-vaccines/{id})
7. Cập nhật trung tâm (PUT /api/centers/{id})
8. Xóa vaccine khỏi trung tâm (DELETE /api/center-vaccines/{id})
9. Xóa trung tâm (DELETE /api/centers/{id})

---

## 5. Lưu Ý Khi Test

1. **Thứ tự test:** Nên tạo Vaccine trước khi thêm vào Center
2. **ID:** Lưu ý ID được tạo tự động, cần lấy từ response
3. **Status:** Chỉ nhận giá trị `ACTIVE` hoặc `INACTIVE`
4. **Error messages:** Tất cả lỗi đều trả về format `{"error": "message"}`

---

## 6. Collection Postman (Import vào Postman)

Tạo file JSON để import vào Postman:

```json
{
  "info": {
    "name": "Vaccination Management System API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "VaccinationCenter",
      "item": [
        {
          "name": "Get All Centers",
          "request": {
            "method": "GET",
            "url": "http://localhost:8080/api/centers"
          }
        },
        {
          "name": "Get Center By ID",
          "request": {
            "method": "GET",
            "url": "http://localhost:8080/api/centers/1"
          }
        },
        {
          "name": "Create Center",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Trung tâm Y tế Quận 2\",\n  \"address\": \"456 Đường XYZ\",\n  \"phoneNumber\": \"0987654321\",\n  \"email\": \"center2@example.com\",\n  \"capacity\": 150,\n  \"status\": \"ACTIVE\"\n}"
            },
            "url": "http://localhost:8080/api/centers"
          }
        },
        {
          "name": "Update Center",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Trung tâm Y tế Quận 1 (Updated)\",\n  \"capacity\": 200\n}"
            },
            "url": "http://localhost:8080/api/centers/1"
          }
        },
        {
          "name": "Delete Center",
          "request": {
            "method": "DELETE",
            "url": "http://localhost:8080/api/centers/1"
          }
        }
      ]
    },
    {
      "name": "CenterVaccine",
      "item": [
        {
          "name": "Get Vaccines By Center",
          "request": {
            "method": "GET",
            "url": "http://localhost:8080/api/centers/1/vaccines"
          }
        },
        {
          "name": "Add Vaccine To Center",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"vaccineId\": 1,\n  \"stockQuantity\": 500\n}"
            },
            "url": "http://localhost:8080/api/centers/1/vaccines"
          }
        },
        {
          "name": "Update Center Vaccine",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"stockQuantity\": 750\n}"
            },
            "url": "http://localhost:8080/api/center-vaccines/1"
          }
        },
        {
          "name": "Delete Center Vaccine",
          "request": {
            "method": "DELETE",
            "url": "http://localhost:8080/api/center-vaccines/1"
          }
        }
      ]
    }
  ]
}
```

Lưu file này và import vào Postman: **File → Import → Chọn file JSON**

---

## 7. Troubleshooting

### Lỗi 500 Internal Server Error
- Kiểm tra database đã kết nối chưa
- Kiểm tra log trong console để xem lỗi chi tiết

### Lỗi 404 Not Found
- Kiểm tra URL đúng chưa
- Kiểm tra ID có tồn tại trong database

### Lỗi 400 Bad Request
- Kiểm tra JSON format đúng chưa
- Kiểm tra các field bắt buộc đã có chưa
- Kiểm tra giá trị enum (status phải là ACTIVE hoặc INACTIVE)

