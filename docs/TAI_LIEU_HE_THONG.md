# Tai lieu he thong

## 1. Muc tieu

`API Test App` la ung dung desktop JavaFX cho tester:

- dang nhap va khoi tao session lam viec
- cau hinh base URL truoc khi chay test
- nap testcase co san hoac testcase do user tao
- chay testcase, setup, cleanup, assertion
- debug endpoint bang request thu cong
- xem dashboard, report va lich su run

He thong la API test client, khong phai API backend.

## 2. Pham vi chuc nang

### Dang hoat dong trong luong chinh

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

### Ton tai trong repo nhung chua noi vao navigation chinh

- `Collections`
- `Environments`

## 3. Actor va he thong ngoai

### Actor chinh

- `Tester`: su dung app de chay test, tao testcase va xem ket qua.

### He thong ngoai

- `Backend API`: dich vu duoc goi khi run testcase hoac gui request thu cong.
- `PostgreSQL`: luu user, role, client machine, user test suite, user test case.
- `RunStorage JSON`: file local luu lich su run.

## 4. Module chinh

### 4.1 Khoi dong va dieu huong

- `MainApplication`: khoi tao JavaFX, AtlantaFX theme, login view va main view.
- `MainController`: dieu huong, cache view, refresh view, mo report theo run ID, phan hoi logout va xac nhan thoat app.

Navigation chinh:

- `Dashboard`
- `Testcase`
- `Request`
- `Report`
- `History`
- `Profile` qua user menu

Phim tat:

- `Ctrl + D`: Dashboard
- `Ctrl + T`: Testcase
- `Ctrl + R`: Request
- `Ctrl + E`: Report
- `Ctrl + H`: History

### 4.2 Login va session

- `LoginController` nhan username/email va password.
- `UserRepository.findUserByEmailAndPassword` xac thuc voi PostgreSQL.
- `AppSession` giu current user va username.
- Sau login, `MainController` luu thong tin client machine qua `ClientMachineRepository`.

Code hien khong the hien authorization rieng cho admin/tester.

### 4.3 Default run config

Dialog sau login cho phep cau hinh:

- `Base URL`
- `Alert mode`: `Stop on fail` hoac `Continue`

Runner khong con la input trong dialog. `AppRunConfig.getRunner()` lay username trong session, fallback ve user he dieu
hanh.

### 4.4 Testcase runner

`TestcaseController` la module nghiep vu lon nhat:

- nap scenario co san tu `ApiScenarioRegistry`
- nap suite/case do user tao tu PostgreSQL
- tao, sua, soft delete suite va case
- cap nhat cleanup requests cua suite
- chay `Run All` va `Run Selected`
- dung qua trinh run
- replace path params, append query params, apply headers
- chay setup request, capture response variables
- tu dong setup auth khi body/header can `${token}` hoac `${authorizationHeader}`
- goi request chinh qua `ApiTestService`
- so sanh status code, payload assertions va expected response body
- chay cleanup request
- luu `TestRun` vao `RunStorage`

### 4.5 Scenario co san

`ApiScenarioRegistry` dang ky 30 provider:

- `Auth`: signup, login, change password, get user info
- `User`: set user info, set avatar, provider cu `Collections 2/User2 Module2`
- `Map`: insert map/node/step, area, heatmap, path, edges, nodes, floor, meta, ward
- `Flow`: insert obstacle, density, bottleneck, heatmap, edge, edge status, edge density va cac API get
- `Real API`: `GET /map/nodes`

Mot so label/module trong code chua dong nhat; tai lieu ghi nhan trang thai that thay vi chuan hoa ten.

### 4.6 Request builder

`RequestController` ho tro:

- method `GET`, `POST`, `PUT`, `DELETE`, `PATCH`
- URL tuyet doi hoac endpoint tuong doi theo `AppRunConfig.baseUrl`
- params table dong bo voi query string tren URL
- request headers table
- raw body `JSON`, `Text`, `XML`
- multipart form-data
- auth UI `No Auth`, `Basic Auth`, `Bearer Token`
- xem status, response time, response body va response headers
- test script don gian voi `assert status == 200`, `duration < 500`, `body contains "..."`

Gioi han: form-data chi gui text fields, chua co upload file; test script la bo parse don gian, khong phai
JavaScript/Postman sandbox.

### 4.7 Dashboard, report, history

- `DashboardController`: tong so testcase da chay, so run, pass/fail, run gan day.
- `ReportController`: thong tin run, tong testcase, pass/fail, pie chart, bar chart response time, bang chi tiet.
- `HistoryController`: loc theo ngay, loc theo status, tim keyword, mo report, xoa run.

## 5. Persistence

### 5.1 PostgreSQL

Bang chinh:

- `roles`
- `users`
- `client_machines`
- `user_test_suites`
- `user_test_cases`

`database.sql` la file tham khao. Cac migration bo sung nam trong `src/main/resources/db/migrations`.

### 5.2 User testcase

`user_test_cases` ho tro:

- `request_headers`
- `query_params`
- `path_params`
- `request_body`
- `setup_requests`
- `cleanup_requests`
- `payload_assertions`
- `expected_response_body`
- `expected_status_code`

`query_params` cho phep nhieu value cho mot key trong model/service.

### 5.3 RunStorage

`RunStorage` luu local:

```text
%LOCALAPPDATA%\api-test-app\runs.json
```

Neu `LOCALAPPDATA` khong co:

```text
%USERPROFILE%\.api-test-app\runs.json
```

File nay la nguon du lieu cho Dashboard, Report va History.

## 6. Gioi han hien tai

- `database.sql` chua phai migration/deployment script sach.
- Seed SQL trong `database.sql` co phan can sua thu tu va cu phap truoc khi chay truc tiep.
- `Collections` va `Environments` moi o muc UI/resource, chua thanh workflow chinh.
- `Profile` chu yeu hien thi thong tin user, chua phai module sua profile day du.
- `Request` chua ho tro file upload trong form-data.
- `TestcaseController` rat lon, nen can can than khi sua vi blast radius cao.
