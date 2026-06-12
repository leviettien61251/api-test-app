# Tài liệu hệ thống

## 1. Mục tiêu

`API Test App` là ứng dụng desktop JavaFX dành cho tester:

- đăng nhập và khởi tạo session làm việc
- cấu hình base URL trước khi chạy test
- nạp testcase có sẵn hoặc testcase do user tạo
- chạy testcase, setup, cleanup, assertion
- debug endpoint bằng request thủ công
- xem dashboard, report và lịch sử chạy

Hệ thống là API test client, không phải API backend.

## 2. Phạm vi chức năng

### Đang hoạt động trong luồng chính

- `Login`
- `Default run config`
- `Dashboard`
- `Testcase runner`
- `Request builder`
- `Report`
- `History`
- `Profile`
- CRUD `user_test_suite`
- CRUD `user_test_case`

### Tồn tại trong repo nhưng chưa nối vào điều hướng chính

- `Collections`
- `Environments`

## 3. Actor và hệ thống ngoài

### Actor chính

- `Tester`: sử dụng ứng dụng để chạy test, tạo testcase và xem kết quả.

### Hệ thống ngoài

- `Backend API`: dịch vụ được gọi khi chạy testcase hoặc gửi request thủ công.
- `PostgreSQL`: lưu user, role, client machine, user test suite, user test case.
- `RunStorage JSON`: file cục bộ lưu lịch sử chạy.

## 4. Module chính

### 4.1 Khởi động và điều hướng

- `MainApplication`: khởi tạo JavaFX, AtlantaFX theme, login view và main view.
- `MainController`: điều hướng, cache view, refresh view, mở report theo run ID, xử lý logout và xác nhận thoát ứng dụng.

Điều hướng chính:

- `Dashboard`
- `Testcase`
- `Request`
- `Report`
- `History`
- `Profile` qua user menu

Phím tắt:

- `Ctrl + D`: Dashboard
- `Ctrl + T`: Testcase
- `Ctrl + R`: Request
- `Ctrl + E`: Report
- `Ctrl + H`: History

### 4.2 Đăng nhập và session

- `LoginController` nhận username/email và password.
- `UserRepository.findUserByEmailAndPassword` xác thực với PostgreSQL.
- `AppSession` giữ current user và username.
- Sau khi đăng nhập, `MainController` lưu thông tin client machine qua `ClientMachineRepository`.

Code hiện không thể hiện cơ chế phân quyền riêng cho admin/tester.

### 4.3 Default run config

Hộp thoại sau khi đăng nhập cho phép cấu hình:

- `Base URL`
- `Alert mode`: `Stop on fail` hoặc `Continue`

Runner không còn là input trong hộp thoại. `AppRunConfig.getRunner()` lấy username trong session, dự phòng bằng user hệ điều hành.

### 4.4 Testcase runner

`TestcaseController` là module nghiệp vụ lớn nhất:

- nạp scenario có sẵn từ `ApiScenarioRegistry`
- nạp suite/case do user tạo từ PostgreSQL
- tạo, sửa, soft delete suite và case
- cập nhật cleanup requests của suite
- chạy `Run All` và `Run Selected`
- dừng quá trình chạy
- replace path params, append query params, apply headers
- chạy setup request, trích xuất response variables
- tự động setup auth khi body/header cần `${token}` hoặc `${authorizationHeader}`
- gọi request chính qua `ApiTestService`
- so sánh status code, payload assertions và expected response body
- chạy cleanup request
- lưu `TestRun` vào `RunStorage`

### 4.5 Scenario có sẵn

`ApiScenarioRegistry` đăng ký 30 provider:

- `Auth`: signup, login, change password, get user info
- `User`: set user info, set avatar, provider cũ `Collections 2/User2 Module2`
- `Map`: insert map/node/step, area, heatmap, path, edges, nodes, floor, meta, ward
- `Flow`: insert obstacle, density, bottleneck, heatmap, edge, edge status, edge density và các API get
- `Real API`: `GET /map/nodes`

Một số label/module trong code chưa đồng nhất; tài liệu ghi nhận trạng thái thực tế thay vì chuẩn hóa tên.

### 4.6 Request builder

`RequestController` hỗ trợ:

- method `GET`, `POST`, `PUT`, `DELETE`, `PATCH`
- URL tuyệt đối hoặc endpoint tương đối theo `AppRunConfig.baseUrl`
- bảng params đồng bộ với query string trên URL
- request headers table
- raw body `JSON`, `Text`, `XML`
- multipart form-data
- auth UI `No Auth`, `Basic Auth`, `Bearer Token`
- xem status, response time, response body và response headers
- test script đơn giản với `assert status == 200`, `duration < 500`, `body contains "..."`

Giới hạn: form-data chỉ gửi text fields, chưa có chức năng tải file lên; test script là bộ phân tích đơn giản, không phải
JavaScript/Postman sandbox.

### 4.7 Dashboard, report, history

- `DashboardController`: tổng số testcase đã chạy, số run, pass/fail, run gần đây.
- `ReportController`: thông tin run, tổng số testcase, pass/fail, pie chart, bar chart response time, bảng chi tiết.
- `HistoryController`: lọc theo ngày, lọc theo status, tìm keyword, mở report, xóa run.

## 5. Persistence

### 5.1 PostgreSQL

Bảng chính:

- `roles`
- `users`
- `client_machines`
- `user_test_suites`
- `user_test_cases`

`setup.sql` là script khởi tạo cơ sở dữ liệu mới. `database.sql` là file tham khảo cũ; các migration bổ sung nằm trong `src/main/resources/db/migrations`.

### 5.2 User testcase

`user_test_cases` hỗ trợ:

- `request_headers`
- `query_params`
- `path_params`
- `request_body`
- `setup_requests`
- `cleanup_requests`
- `payload_assertions`
- `expected_response_body`
- `expected_status_code`

`query_params` cho phép nhiều value cho một key trong model/service.

### 5.3 RunStorage

`RunStorage` lưu cục bộ:

```text
%LOCALAPPDATA%\api-test-app\runs.json
```

Nếu không có `LOCALAPPDATA`:

```text
%USERPROFILE%\.api-test-app\runs.json
```

File này là nguồn dữ liệu cho Dashboard, Report và History.

## 6. Giới hạn hiện tại

- Dùng `setup.sql` cho cơ sở dữ liệu mới; không dùng `database.sql` cũ để triển khai.
- `Collections` và `Environments` mới ở mức UI/resource, chưa thành workflow chính.
- `Profile` chủ yếu hiển thị thông tin user, chưa phải module sửa profile đầy đủ.
- `Request` chưa hỗ trợ tải file lên trong form-data.
- `TestcaseController` rất lớn, nên cần cẩn thận khi sửa vì blast radius cao.
