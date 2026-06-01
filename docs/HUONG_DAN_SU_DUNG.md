# Huong dan su dung

## 1. Dang nhap

Khi mo app, nguoi dung vao man hinh login:

- nhap username/email
- nhap password
- chon role tren combobox

Sau khi login thanh cong, app mo main view va hien dialog cau hinh run.

## 2. Cau hinh run mac dinh

Dialog `Default run config` gom:

- `Base URL`: dia chi backend de thuc thi testcase
- `Alert mode`:
  - `Stop on fail`: dung khi gap testcase fail
  - `Continue`: chay tiep den het
- `Runner`: ten nguoi chay

## 3. Man hinh Testcase

Day la man hinh quan trong nhat.

### 3.1 Chon scenario co san

Tree ben trai duoc tao tu `ApiScenarioRegistry`. Khi chon mot API:

- bang testcase se nap scenario co san
- request method, URL, headers, body mau se hien ben phai

### 3.2 Chon user test suite

Neu trong database co `user_test_suites`, app them node `User Test Suites`.

Khi chon node nay:

- app nap danh sach testcase user tu PostgreSQL
- testcase hien prefix `[User]`

### 3.3 Chay test

- `Run All`: chay toan bo testcase dang nap
- `Run Selected`: chi chay testcase duoc tick
- `Stop`: dung qua trinh chay

Trong qua trinh chay, app:

1. resolve URL tu `Base URL` va endpoint
2. thuc thi setup request neu co
3. tu dong tao auth signup/login mac dinh neu request can `${token}` hoac `${authorizationHeader}`
4. goi API chinh
5. doi chieu response code/payload
6. chay cleanup request neu co
7. luu tong hop vao `RunStorage`

### 3.4 Log va ket qua

- bang testcase hien `PASS`, `FAIL`, `Dang test`
- list log hien log setup, cleanup, auth setup va loi runtime
- summary hien tong pass/fail

## 4. Tao user test suite va user test case

Code hien tai da co service va persistence cho:

- `UserTestSuiteService`
- `UserTestCaseService`

Nguoi dung co the:

- tao test suite rieng
- gan testcase vao suite
- khai bao headers, query params, request body
- khai bao `setupRequests`, `cleanupRequests`
- dinh nghia `expectedStatusCode`

Luu y: chi tiet UI tao/sua duoc goi tu `TestcaseController`; workflow nay dang phu thuoc vao database.

## 5. Request builder

Man hinh `Request` dung cho viec goi API thu cong:

- chon HTTP method
- nhap endpoint tuong doi hoac URL tuyet doi
- chon dinh dang body
- xem status, time, body, headers

Neu URL nhap dang `/api/...`, app se noi voi `Base URL` hien tai trong `AppRunConfig`.

## 6. Dashboard, Report, History

### Dashboard

- tong so testcase da chay
- tong so run
- tong pass/fail
- danh sach run gan day

Double-click mot dong de mo report.

### Report

- thong tin run: runner, machine, os, thoi gian
- tong so testcase, pass, fail
- pie chart pass/fail
- bar chart response time
- bang chi tiet tung testcase

### History

- loc theo ngay
- loc theo status `Pass/Fail/Tat ca`
- tim theo keyword
- mo report
- xoa mot run da luu

## 7. Meo su dung

- doi `Base URL` truoc khi test backend khac moi truong
- su dung user testcase khi can thu nghiem case dac thu ma khong muon hardcode vao repo
- xem `History` sau khi run de doi chieu ket qua nhieu lan
- dung `Request` de debug nhanh truoc khi chuyen thanh testcase chinh thuc
