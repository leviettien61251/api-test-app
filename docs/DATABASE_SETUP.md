# Hướng dẫn thiết lập PostgreSQL

Tài liệu này hướng dẫn khởi tạo cơ sở dữ liệu cho bản Windows đã đóng gói của `API Test App`.

## 1. Thành phần cần có

- PostgreSQL 14 trở lên. Khuyến nghị PostgreSQL 16 hoặc 17.
- Quyền tạo cơ sở dữ liệu và extension trên PostgreSQL.
- Một trong hai công cụ: pgAdmin 4 hoặc `psql`.

Bản đóng gói đã kèm Java runtime, vì vậy máy chạy ứng dụng không cần cài JDK hay Maven.

## 2. Thông tin kết nối mặc định

Nếu không cấu hình thêm, ứng dụng sử dụng:

```text
Host: localhost
Port: 5432
Database: api_test_app
User: postgres
Password: 12345
```

Bạn có thể thiết lập PostgreSQL theo đúng cấu hình trên hoặc cấu hình ứng dụng bằng biến môi trường ở bước 6.

## 3. Tạo cơ sở dữ liệu bằng pgAdmin

1. Mở pgAdmin và kết nối đến PostgreSQL server.
2. Nhấp chuột phải vào `Databases`, chọn `Create` > `Database`.
3. Đặt `Database` là `api_test_app` và chọn owner phù hợp.
4. Mở `Query Tool` của cơ sở dữ liệu vừa tạo.
5. Mở file `src/main/resources/db/setup.sql`.
6. Chạy toàn bộ script và bảo đảm không có lỗi.

Script `setup.sql` không xóa dữ liệu cũ và có thể chạy lại khi cần.

## 4. Tạo cơ sở dữ liệu bằng psql

Chạy PowerShell tại thư mục gốc của dự án:

```powershell
createdb -h localhost -p 5432 -U postgres api_test_app
psql -h localhost -p 5432 -U postgres -d api_test_app -f ".\src\main\resources\db\setup.sql"
```

Nếu `createdb` hoặc `psql` không được nhận diện, hãy dùng đường dẫn đầy đủ, ví dụ:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\createdb.exe" -h localhost -p 5432 -U postgres api_test_app
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -h localhost -p 5432 -U postgres -d api_test_app -f ".\src\main\resources\db\setup.sql"
```

## 5. Tài khoản khởi tạo

Script tạo tài khoản cục bộ sau:

```text
Email: admin@local.test
Password: ChangeMe123!
```

Ứng dụng hiện tại so sánh mật khẩu ở dạng văn bản thuần. Đây chỉ là tài khoản khởi tạo cho môi trường cục bộ; không sử dụng mật khẩu này trên cơ sở dữ liệu công khai.

Có thể đổi mật khẩu bằng SQL:

```sql
UPDATE users
SET password = 'MatKhauMoi', updated_at = NOW()
WHERE email = 'admin@local.test';
```

## 6. Chạy bản đóng gói

Nếu PostgreSQL dùng cấu hình mặc định, giải nén gói phát hành và chạy:

```powershell
.\API-Test-App.exe
```

Nếu thông tin kết nối khác mặc định, hãy đặt biến môi trường trong cùng cửa sổ PowerShell trước khi chạy:

```powershell
$env:APP_DB_URL="jdbc:postgresql://localhost:5432/api_test_app"
$env:APP_DB_USER="postgres"
$env:APP_DB_PASSWORD="mat_khau_postgres"
.\API-Test-App.exe
```

Các biến trên chỉ tồn tại trong cửa sổ PowerShell hiện tại. Để lưu cho tài khoản Windows hiện tại:

```powershell
[Environment]::SetEnvironmentVariable("APP_DB_URL", "jdbc:postgresql://localhost:5432/api_test_app", "User")
[Environment]::SetEnvironmentVariable("APP_DB_USER", "postgres", "User")
[Environment]::SetEnvironmentVariable("APP_DB_PASSWORD", "mat_khau_postgres", "User")
```

Đóng và mở lại ứng dụng sau khi thay đổi biến môi trường.

## 7. Kiểm tra cơ sở dữ liệu

Chạy các câu lệnh sau trong pgAdmin hoặc `psql`:

```sql
SELECT current_database();
SELECT name FROM roles ORDER BY id;
SELECT email, full_name, is_active FROM users ORDER BY created_at;
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN (
      'roles',
      'users',
      'client_machines',
      'user_test_suites',
      'user_test_cases'
  )
ORDER BY table_name;
```

Kết quả phải có đủ 5 bảng và tài khoản `admin@local.test`.

## 8. Cơ sở dữ liệu trên máy khác

PostgreSQL không bắt buộc nằm cùng máy với ứng dụng. Nếu dùng database server trong mạng LAN, cấu hình:

```powershell
$env:APP_DB_URL="jdbc:postgresql://192.168.1.10:5432/api_test_app"
$env:APP_DB_USER="api_test_app"
$env:APP_DB_PASSWORD="mat_khau_database"
.\API-Test-App.exe
```

PostgreSQL server phải cho phép kết nối TCP từ máy client trong `postgresql.conf`, `pg_hba.conf` và firewall. Không mở cổng `5432` trực tiếp ra Internet nếu không có VPN/TLS và giới hạn truy cập phù hợp.

## 9. Sao lưu và phục hồi

Sao lưu:

```powershell
pg_dump -h localhost -p 5432 -U postgres -Fc -d api_test_app -f api_test_app.dump
```

Phục hồi vào cơ sở dữ liệu trống:

```powershell
createdb -h localhost -p 5432 -U postgres api_test_app
pg_restore -h localhost -p 5432 -U postgres -d api_test_app --clean --if-exists api_test_app.dump
```

File dump có thể được phát hành kèm ứng dụng nếu cần mang theo dữ liệu mẫu. Máy đích vẫn cần có PostgreSQL để phục hồi và vận hành cơ sở dữ liệu.

## 10. Lỗi thường gặp

### Không thể kết nối cơ sở dữ liệu

- Kiểm tra dịch vụ PostgreSQL đang chạy.
- Kiểm tra host, port, database, user và password.
- Kiểm tra các biến `APP_DB_URL`, `APP_DB_USER`, `APP_DB_PASSWORD` trong đúng tài khoản Windows đang chạy ứng dụng.
- Kiểm tra firewall nếu cơ sở dữ liệu nằm trên máy khác.

### Không tìm thấy tài khoản

- Đăng nhập bằng email, không phải tên hiển thị.
- Kiểm tra tài khoản tồn tại trong bảng `users`.
- Mật khẩu phải khớp chính xác, bao gồm chữ hoa và ký tự đặc biệt.

### Lỗi extension uuid-ossp

Tài khoản thiết lập cơ sở dữ liệu cần có quyền tạo extension. Kết nối bằng owner hoặc superuser rồi chạy:

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### Lỗi thiếu cột

Với cơ sở dữ liệu cũ, chạy các file trong `src/main/resources/db/migrations` theo thứ tự tên file. `setup.sql` tạo đầy đủ cột cho cơ sở dữ liệu mới, nhưng `CREATE TABLE IF NOT EXISTS` không tự động bổ sung cột vào bảng đã tồn tại.

