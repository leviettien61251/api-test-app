# Hướng dẫn sử dụng

## 1. Đăng nhập

1. Mở ứng dụng.
2. Nhập email/username.
3. Nhập password.
4. Bấm đăng nhập.

Sau khi đăng nhập thành công, ứng dụng mở main shell và hiển thị hộp thoại `Default run config`.

## 2. Cấu hình chạy mặc định

Hộp thoại gồm:

- `Base URL`: URL backend được kiểm thử.
- `Alert mode`: `Stop on fail` hoặc `Continue`.
- `Machine` và `OS`: thông tin hiển thị, không phải dữ liệu nhập.

Khi bấm OK, ứng dụng lưu vào `AppRunConfig`, tải lại cache `Testcase`/`Request` và chuyển sang màn hình `Testcase`.

## 3. Điều hướng nhanh

- `Ctrl + D`: Dashboard
- `Ctrl + T`: Testcase
- `Ctrl + R`: Request
- `Ctrl + E`: Report
- `Ctrl + H`: History

Profile và Logout nằm trong user menu.

## 4. Màn hình Testcase

### 4.1 Nạp testcase

Nguồn testcase:

- scenario có sẵn trong `ApiScenarioRegistry`
- `User Test Suites` và `User Test Cases` trong PostgreSQL

Khi chọn API/suite, ứng dụng hiển thị method, endpoint, request data và bảng testcase.

### 4.2 Tạo user suite

Nhập các trường chính:

- name
- method
- endpoint
- description
- cleanup requests JSON array nếu cần

Suite được gán owner theo user đang đăng nhập và lưu vào `user_test_suites`.

### 4.3 Tạo user testcase

User testcase có thể khai báo:

- name, description
- method, endpoint
- headers
- query params và path params
- request body
- setup requests
- cleanup requests
- payload assertions
- expected response body
- expected status code

Body JSON và expected response body sẽ được kiểm tra cú pháp. Token runtime dạng `${variable}` được chấp nhận trong
quá trình kiểm tra.

### 4.4 Chạy testcase

Nút chính:

- `Run All`
- `Run Selected`
- `Stop`

Khi chạy, ứng dụng thực hiện:

1. ghép endpoint với `Base URL`
2. thay path params vào URL
3. thêm query params
4. thêm headers
5. chạy setup requests
6. trích xuất response variables
7. chạy auth setup nếu phát hiện `${token}` hoặc `${authorizationHeader}`
8. gọi request chính
9. so sánh expected status code
10. so sánh payload assertions
11. so sánh expected response body nếu có
12. chạy cleanup requests
13. lưu run vào `RunStorage`

`Stop on fail` dừng các testcase tiếp theo sau khi gặp lỗi. `Continue` tiếp tục chạy.

## 5. Màn hình Request

Dùng để debug endpoint thủ công.

### 5.1 URL và params

- Nhập URL tuyệt đối: `https://example.com/api/users`.
- Nhập endpoint tương đối: `/users` hoặc `users`, ứng dụng ghép với `AppRunConfig.baseUrl`.
- Query string trên URL được phân tích vào bảng Params.
- Sửa bảng Params sẽ đồng bộ lại URL.

### 5.2 Headers và auth

- Thêm custom header trong tab Headers.
- Chọn `Basic Auth` để gửi `Authorization: Basic ...`.
- Chọn `Bearer Token` để gửi `Authorization: Bearer ...`.
- Nếu custom header và auth cùng đặt `Authorization`, auth header sẽ ghi đè bằng `builder.header`.

### 5.3 Body

- Raw body hỗ trợ `JSON`, `Text`, `XML`.
- Form-data gửi multipart text fields.
- `GET` và `DELETE` không gửi body trong luồng Request hiện tại.

### 5.4 Response và tests

Sau khi gửi request, ứng dụng hiển thị:

- HTTP status
- response time
- response body
- response headers

Tab Tests hỗ trợ assert đơn giản:

```text
assert status == 200 : "Kiểm tra status";
assert duration < 500 : "Kiểm tra thời gian";
assert body contains "1000" : "Kiểm tra nội dung";
```

## 6. Dashboard

Dashboard hiển thị:

- tổng số testcase đã chạy
- tổng số run
- tổng số pass/fail
- danh sách run gần đây

Nhấp đúp vào run để mở report.

## 7. Report

Report hiển thị:

- runner, machine, OS, thời gian bắt đầu
- tổng số testcase, pass, fail
- pie chart pass/fail
- bar chart response time
- bảng kết quả từng testcase

Report đọc run ID từ `SelectedRunContext` khi mở từ Dashboard/History.

## 8. History

History cho phép:

- lọc theo ngày
- lọc theo status
- tìm keyword
- mở report
- xóa run

Dữ liệu đến từ file cục bộ `runs.json`.

## 9. Profile và đăng xuất

- `Profile` hiển thị thông tin user hiện tại.
- `Logout` xóa session, reset run config và quay về màn hình đăng nhập.

## 10. Mẹo sử dụng

- Luôn kiểm tra `Base URL` sau khi đăng nhập nếu đổi môi trường backend.
- Dùng `Request` để debug endpoint trước khi tạo testcase.
- Dùng `payload assertions` khi response có field động.
- Dùng `expected response body` khi cần so sánh toàn bộ JSON.
- Xem `History` sau nhiều lần chạy để đối chiếu hồi quy.
