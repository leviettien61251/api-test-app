# API Test App

Ứng dụng JavaFX để kiểm thử REST API cho hệ thống dẫn đường trong bệnh viện. Người dùng chính là tester.

## Tài liệu chính

- [Tài liệu hệ thống](docs/TAI_LIEU_HE_THONG.md)
- [Chỉ mục tài liệu](docs/DOCUMENTATION_INDEX.md)

## Công nghệ chính

- Java 21
- JavaFX
- PostgreSQL
- OkHttp
- Gson
- Lombok

## Cấu hình database

Ứng dụng đọc cấu hình DB từ `src/main/java/com/example/apitestapp/db/ConnectionManager.java`.

Giá trị có thể ghi đè bằng:

- `app.db.url` hoặc `APP_DB_URL`
- `app.db.user` hoặc `APP_DB_USER`
- `app.db.password` hoặc `APP_DB_PASSWORD`
