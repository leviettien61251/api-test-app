# Thiết lập và vận hành

## 1. Yêu cầu

- JDK 21
- Maven 3.9+ hoặc Maven wrapper `mvnw.cmd`
- PostgreSQL
- Windows là môi trường được hỗ trợ rõ nhất

## 2. Build và test

```powershell
.\mvnw.cmd clean test
```

Đóng gói và bỏ qua test:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Lần đầu dùng wrapper có thể cần mạng để tải Maven distribution và dependencies.

## 3. Chạy ứng dụng

```powershell
.\mvnw.cmd javafx:run
```

Main class được cấu hình trong `pom.xml`:

```text
com.example.apitestapp/com.example.apitestapp.MainApplication
```

## 4. Cấu hình database

`ConnectionManager` đọc theo thứ tự:

1. Java system property
2. Environment variable
3. Giá trị mặc định trong code

Mặc định:

- `jdbc:postgresql://localhost:5432/api_test_app`
- `postgres`
- `12345`

System properties:

- `app.db.url`
- `app.db.user`
- `app.db.password`

Environment variables:

- `APP_DB_URL`
- `APP_DB_USER`
- `APP_DB_PASSWORD`

Ví dụ:

```powershell
.\mvnw.cmd "-Dapp.db.url=jdbc:postgresql://localhost:5432/api_test_app" "-Dapp.db.user=postgres" "-Dapp.db.password=12345" javafx:run
```

## 5. Khởi tạo schema

Script khởi tạo cơ sở dữ liệu mới:

- `src/main/resources/db/database.sql`

Migration bổ sung:

- `src/main/resources/db/migrations/20260602_add_user_test_case_path_params.sql`
- `src/main/resources/db/migrations/20260602_add_user_test_case_response_assertions.sql`

Thứ tự thiết lập mới khuyến nghị:

1. Tạo database `api_test_app`.
2. Chạy toàn bộ `src/main/resources/db/database.sql`.
3. Đăng nhập bằng tài khoản khởi tạo trong `docs/DATABASE_SETUP.md`.
4. Chỉ chạy migration trong thư mục `migrations` khi nâng cấp schema cũ.

Hướng dẫn chi tiết bằng pgAdmin và `psql`: `docs/DATABASE_SETUP.md`.

## 6. Dữ liệu seed

`setup.sql` seed một role admin, một role tester và tài khoản cục bộ ban đầu. Thông tin đăng nhập và cách đổi mật khẩu nằm trong `docs/DATABASE_SETUP.md`.

## 7. Sau khi đăng nhập

Ứng dụng tự động:

- khởi tạo `AppSession`
- lưu client machine vào PostgreSQL
- mở Dashboard
- hiển thị hộp thoại `Default run config`

Hộp thoại cho nhập:

- `Base URL`
- `Alert mode`

Mặc định:

- `Base URL`: `http://group3.it4788.sukkaito.id.vn/api`
- `Alert mode`: `Stop on fail`

## 8. Storage run

RunStorage ghi:

```text
%LOCALAPPDATA%\api-test-app\runs.json
```

Đường dẫn dự phòng khi không có `LOCALAPPDATA`:

```text
%USERPROFILE%\.api-test-app\runs.json
```

Nếu file sai định dạng JSON, ứng dụng sẽ ghi log lỗi đọc file và tiếp tục với danh sách trong bộ nhớ.

## 9. Vận hành hằng ngày

1. Đảm bảo PostgreSQL và backend API đang chạy.
2. Chạy ứng dụng.
3. Đăng nhập.
4. Kiểm tra `Base URL`.
5. Chọn scenario hoặc user suite.
6. Chạy `Run All` hoặc `Run Selected`.
7. Xem Dashboard, Report, History.

## 10. Sự cố thường gặp

### Không kết nối được database

- Kiểm tra PostgreSQL đang chạy.
- Kiểm tra `app.db.*` hoặc `APP_DB_*`.
- Kiểm tra schema có các bảng `users`, `roles`, `client_machines`, `user_test_suites`, `user_test_cases`.

### Đăng nhập thất bại

- `LoginController` tìm user theo email và password.
- Kiểm tra user seed trong bảng `users`.
- Kiểm tra password trong DB hiện là văn bản thuần theo code hiện tại.

### Testcase lỗi kết nối backend

- Kiểm tra `Base URL`.
- Kiểm tra endpoint trong scenario/user case.
- Kiểm tra backend trả đúng status/body mong đợi.

### Request auth không như mong đợi

- Kiểm tra auth type đang chọn.
- Basic Auth/Bearer Token được đặt vào `Authorization`.
- Nếu custom header cũng là `Authorization`, giá trị auth UI sẽ ghi đè.

### Không thấy lịch sử cũ

- Kiểm tra file `%LOCALAPPDATA%\api-test-app\runs.json`.
- Kiểm tra đường dẫn dự phòng `%USERPROFILE%\.api-test-app\runs.json`.
- Kiểm tra quyền ghi file.
