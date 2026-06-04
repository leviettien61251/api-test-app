# Tai lieu he thong

## 1. Muc tieu he thong

`API Test App` la ung dung desktop JavaFX phuc vu tester trong viec:

- dang nhap va giu session lam viec
- nap testcase API tu code hoac tu database
- chay testcase hang loat hoac chon loc
- debug endpoint bang request thu cong
- luu, xem, loc va xoa lich su thuc thi

He thong nay la mot test client cho backend, khong phai backend service.

## 2. Pham vi chuc nang

### Co trong code hien tai

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

### Co mat trong repo nhung chua tham gia luong chinh day du

- `Collections`
- `Environments`

Chi tiet o [MA_TRAN_MAN_HINH.md](/D:/code/api-test-app/docs/MA_TRAN_MAN_HINH.md).

## 3. Nguoi dung va he thong ngoai

### Actor chinh

- `Tester`

### He thong ngoai

- `Backend API`: dich vu duoc test
- `PostgreSQL`: luu user, role, may client, user test suite, user test case
- `RunStorage JSON`: luu local lich su run

Use case muc cao nam tai [USECASE_TONG_QUAT.md](/D:/code/api-test-app/docs/USECASE_TONG_QUAT.md).

## 4. Thanh phan chinh

### 4.1 Khoi tao va dieu huong

- `MainApplication`: khoi tao JavaFX, stylesheet, login view
- `MainController`: dieu huong giua cac man hinh chinh, cache view, dang ky phim tat va dialog xac nhan thoat

Main menu hien tai noi den:

- `Dashboard`
- `Testcase`
- `Request`
- `Report`
- `History`
- `Profile`

Phim tat da duoc cai trong `MainController`:

- `Ctrl + D`: Dashboard
- `Ctrl + T`: Testcase
- `Ctrl + R`: Request
- `Ctrl + E`: Report
- `Ctrl + H`: History

### 4.2 Login va session

- `LoginController` tim user qua `UserRepository`
- session runtime duoc giu trong `AppSession`
- thong tin may client duoc luu qua `ClientMachineRepository` ngay sau khi vao main view

Luu y:

- role trong UI hien la gia tri chon tren combobox
- docs khong nen coi do la co che phan quyen hoan chinh, vi code chua the hien logic authorization tach biet

### 4.3 Testcase runner

`TestcaseController` la module trung tam cua app. Chuc nang hien tai gom:

- nap scenario co san tu `ApiScenarioRegistry`
- nap `user test suite` va `user test case` tu database
- chay `Run All` / `Run Selected`
- dung qua trinh chay
- save base URL
- CRUD suite/testcase cho user
- quan ly setup/cleanup hooks
- validate payload qua `ApiPayloadAssertionEvaluator`

Nguon testcase hien tai gom 2 nhom:

1. `Scenario code san`
2. `User testcase` luu trong PostgreSQL

### 4.4 Nhom scenario co san

`ApiScenarioRegistry` dang ky cac provider thuoc cac nhom:

- `Auth`
- `User`
- `Map`
- `Flow`
- `Real API`

Ngoai ra repo con mot so ten module cu/chua chuan hoa, vi du `Collections 2`, `User2 Module2`. Docs can ghi nhan do day la trang thai that cua code, khong nen lam dep bang cach bo qua.

### 4.5 Request builder

`RequestController` cho phep:

- chon method `GET`, `POST`, `PUT`, `DELETE`, `PATCH`
- nhap URL tuyet doi hoac endpoint tuong doi
- gui raw body `JSON`, `Text`, `XML`
- xem status, response time, body va headers

Trang thai hien tai:

- UI chon `No Auth`, `Basic Auth`, `Bearer Token` da co
- nhung logic HTTP call hien tai moi chi set `Accept: */*`
- thong tin auth tu UI chua duoc ap vao request

### 4.6 Dashboard, report, history

- `DashboardController`
  - tong so testcase da chay
  - tong so run
  - tong pass/fail
  - danh sach run gan day

- `ReportController`
  - thong tin run
  - tong testcase, pass, fail
  - pie chart pass/fail
  - bar chart response time
  - bang ket qua chi tiet

- `HistoryController`
  - loc theo ngay
  - loc theo status
  - tim theo keyword
  - mo report
  - xoa run

## 5. Du lieu va persistence

### 5.1 PostgreSQL

Schema hien tai su dung it nhat cac bang:

- `roles`
- `users`
- `client_machines`
- `user_test_suites`
- `user_test_cases`

File tham khao: [database.sql](/D:/code/api-test-app/src/main/resources/db/database.sql)

### 5.2 Cac truong quan trong cua `user_test_cases`

Theo schema va model/repository hien tai, user testcase ho tro:

- `request_headers`
- `query_params`
- `path_params`
- `request_body`
- `setup_requests`
- `cleanup_requests`
- `payload_assertions`
- `expected_response_body`
- `expected_status_code`

Dieu nay co nghia app khong chi test status code, ma con co the:

- thay the path params vao endpoint
- so sanh payload theo `jsonPath`
- so sanh toan bo response JSON

### 5.3 Run storage local

`RunStorage` luu lich su run vao:

```text
%LOCALAPPDATA%\api-test-app\runs.json
```

Muc dich:

- tach lich su run khoi workspace
- tranh xung dot khoa file tren Windows/OneDrive
- cho `Dashboard`, `Report`, `History` doc lai du lieu nhanh

## 6. Runtime config

### 6.1 Cau hinh database

`ConnectionManager` doc cau hinh theo thu tu:

1. Java system property
2. Environment variable
3. Gia tri mac dinh trong code

Mac dinh:

- URL: `jdbc:postgresql://localhost:5432/api_test_app`
- User: `postgres`
- Password: `12345`

### 6.2 Cau hinh run

`AppRunConfig` quan ly:

- `baseUrl`
- `alertMode`
- `runner`
- `configuredAt`

Gia tri mac dinh dang quan trong nhat:

- `DEFAULT_BASE_URL = http://group3.it4788.sukkaito.id.vn/api`
- `DEFAULT_ALERT_MODE = Stop on fail`

Luu y:

- hang `DEFAULT_RUN_MODE` van con trong code
- nhung luong chon `run mode` da bi loai bo khoi dialog cau hinh
- khi luu ket qua run, app phan biet `Run All` va `Run Selected` o cap ket qua thuc thi, khong phai o dialog config

## 7. Gioi han ky thuat can ghi nhan

- `database.sql` chua sach, khong phai migration tuyen tinh
- ten collection/module trong scenario provider chua dong nhat
- auth UI trong `Request` chua duoc noi voi HTTP headers thuc te
- `Collections` va `Environments` chua noi vao navigation chinh
- `Profile` hien nghien ve hien thi thong tin hon la module chinh sua profile day du

## 8. Khi nao can cap nhat docs tiep

Can cap nhat docs neu co thay doi trong:

- schema PostgreSQL
- danh sach scenario provider
- co che auth/request builder
- format `runs.json`
- workflow tao/sua user testcase
- navigation chinh cua `MainController`
