# Chỉ mục tài liệu

Bộ tài liệu này được cập nhật theo code hiện tại của `API Test App`.

## Thứ tự đọc khuyến nghị

1. [README.md](../README.md)
2. [TAI_LIEU_HE_THONG.md](TAI_LIEU_HE_THONG.md)
3. [MA_TRAN_MAN_HINH.md](MA_TRAN_MAN_HINH.md)
4. [HUONG_DAN_SU_DUNG.md](HUONG_DAN_SU_DUNG.md)
5. [THIET_LAP_VA_VAN_HANH.md](THIET_LAP_VA_VAN_HANH.md)
6. [DATABASE_SETUP.md](DATABASE_SETUP.md)
7. [ARCHITECTURE.md](ARCHITECTURE.md)

## Vai trò từng tài liệu

- `README.md`: tổng quan nhanh, stack, lệnh build/run và liên kết tài liệu.
- `TAI_LIEU_HE_THONG.md`: phạm vi hệ thống, actor, module, persistence, runtime config và giới hạn.
- `MA_TRAN_MAN_HINH.md`: FXML/controller, trạng thái điều hướng và ghi chú từng màn hình.
- `USECASE_TONG_QUAT.md`: actor, hệ thống ngoài và use case mức cao.
- `USECASE_PHAN_RA.md`: phân rã use case theo module UI/nghiệp vụ.
- `HUONG_DAN_SU_DUNG.md`: quy trình thao tác cho tester.
- `THIET_LAP_VA_VAN_HANH.md`: yêu cầu môi trường, DB, lưu trữ và xử lý sự cố.
- `DATABASE_SETUP.md`: hướng dẫn cài PostgreSQL, tạo schema, seed tài khoản, cấu hình kết nối và sao lưu/phục hồi.
- `ARCHITECTURE.md`: package layout, luồng khởi động, luồng chạy testcase và service/repository.

## Điểm cần lưu ý khi đọc

- Dùng `src/main/resources/db/setup.sql` để khởi tạo cơ sở dữ liệu mới. `database.sql` chỉ là file tham khảo cũ.
- Thư mục `src/main/resources/db/migrations` có các migration bổ sung cho `path_params`, `payload_assertions`,
  `expected_response_body`.
- `Request` hiện đã áp dụng custom headers, Basic Auth và Bearer Token vào HTTP request.
- `Request` có form-data UI và multipart form-data cho method không phải `GET`/`DELETE`.
- `Collections` và `Environments` tồn tại trong repo nhưng chưa nằm trong điều hướng chính.
- `Default run config` hiện chỉ cho nhập `Base URL` và `Alert mode`; runner được lấy từ session username hoặc system user.
