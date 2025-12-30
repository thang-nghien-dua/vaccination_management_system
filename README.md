# Hệ Thống Quản Lý Tiêm Chủng Trẻ Em

## Mô tả
Hệ thống quản lý lịch tiêm chủng cho trẻ em được xây dựng bằng Spring Boot, giúp theo dõi và quản lý lịch tiêm vaccine cho trẻ em một cách hiệu quả.

## Công nghệ sử dụng
- **Spring Boot 4.0.1**
- **Java 17**
- **Spring Data JPA**
- **Thymeleaf** (Server-side rendering)
- **MySQL Database**
- **Maven** (Build tool)

## Cấu trúc dự án
```
src/main/java/com/example/vaccinationmanagement_system/
├── controller/      # Controllers xử lý HTTP requests
├── service/         # Business logic layer
├── repository/      # Data access layer
├── entity/          # JPA entities
├── dto/             # Data Transfer Objects
├── config/          # Configuration classes
└── exception/       # Exception handling
```

## Cài đặt và chạy

### Yêu cầu
- JDK 17 hoặc cao hơn
- Maven 3.6+
- MySQL 8.0+

### Cấu hình database
Cập nhật thông tin kết nối MySQL trong `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vaccination_management_system
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Chạy ứng dụng
```bash
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

## Tính năng
- ✅ Quản lý thông tin trẻ em
- ✅ Quản lý thông tin vaccine
- ✅ Quản lý lịch tiêm chủng
- ✅ Theo dõi lịch sử tiêm chủng

## Tác giả
Thang Nghien Dua

## License
MIT License

