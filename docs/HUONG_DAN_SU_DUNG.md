# Huong dan su dung

## 1. Dang nhap

1. Mo app.
2. Nhap email/username.
3. Nhap password.
4. Bam login.

Sau khi login thanh cong, app mo main shell va hien dialog `Default run config`.

## 2. Cau hinh run mac dinh

Dialog gom:

- `Base URL`: URL backend duoc test.
- `Alert mode`: `Stop on fail` hoac `Continue`.
- `Machine` va `OS`: thong tin hien thi, khong phai input.

Khi bam OK, app luu vao `AppRunConfig`, reload `Testcase`/`Request` cache va chuyen sang man hinh `Testcase`.

## 3. Dieu huong nhanh

- `Ctrl + D`: Dashboard
- `Ctrl + T`: Testcase
- `Ctrl + R`: Request
- `Ctrl + E`: Report
- `Ctrl + H`: History

Profile va Logout nam trong user menu.

## 4. Man hinh Testcase

### 4.1 Nap testcase

Nguon testcase:

- scenario co san trong `ApiScenarioRegistry`
- `User Test Suites` va `User Test Cases` trong PostgreSQL

Khi chon API/suite, app hien method, endpoint, request data va bang testcase.

### 4.2 Tao user suite

Nhap cac truong chinh:

- name
- method
- endpoint
- description
- cleanup requests JSON array neu can

Suite duoc gan owner theo user dang login va luu vao `user_test_suites`.

### 4.3 Tao user testcase

User testcase co the khai bao:

- name, description
- method, endpoint
- headers
- query params va path params
- request body
- setup requests
- cleanup requests
- payload assertions
- expected response body
- expected status code

Body JSON va expected response body se duoc validate cu phap. Token runtime dang dang `${variable}` duoc chap nhan trong qua trinh validate.

### 4.4 Chay testcase

Nut chinh:

- `Run All`
- `Run Selected`
- `Stop`

Khi run, app thuc hien:

1. resolve endpoint voi `Base URL`
2. thay path params vao URL
3. them query params
4. them headers
5. chay setup requests
6. capture response variables
7. chay auth setup neu phat hien `${token}` hoac `${authorizationHeader}`
8. goi request chinh
9. so sanh expected status code
10. so sanh payload assertions
11. so sanh expected response body neu co
12. chay cleanup requests
13. luu run vao `RunStorage`

`Stop on fail` dung tiep cac testcase sau khi gap fail. `Continue` tiep tuc chay.

## 5. Man hinh Request

Dung de debug endpoint thu cong.

### 5.1 URL va params

- Nhap URL tuyet doi: `https://example.com/api/users`.
- Nhap endpoint tuong doi: `/users` hoac `users`, app ghep voi `AppRunConfig.baseUrl`.
- Query string tren URL duoc parse vao bang Params.
- Sua bang Params se dong bo lai URL.

### 5.2 Headers va auth

- Them custom header trong tab Headers.
- Chon `Basic Auth` de gui `Authorization: Basic ...`.
- Chon `Bearer Token` de gui `Authorization: Bearer ...`.
- Neu custom header va auth cung set `Authorization`, auth header se ghi de bang `builder.header`.

### 5.3 Body

- Raw body ho tro `JSON`, `Text`, `XML`.
- Form-data gui multipart text fields.
- `GET` va `DELETE` khong gui body trong luong Request hien tai.

### 5.4 Response va tests

Sau khi gui request, app hien:

- HTTP status
- response time
- response body
- response headers

Tab Tests ho tro assert don gian:

```text
assert status == 200 : "Kiem tra status";
assert duration < 500 : "Kiem tra thoi gian";
assert body contains "1000" : "Kiem tra noi dung";
```

## 6. Dashboard

Dashboard hien:

- tong testcase da chay
- tong so run
- tong pass/fail
- danh sach run gan day

Double-click run de mo report.

## 7. Report

Report hien:

- runner, machine, OS, thoi gian bat dau
- tong testcase, pass, fail
- pie chart pass/fail
- bar chart response time
- bang ket qua tung testcase

Report doc run ID tu `SelectedRunContext` khi mo tu Dashboard/History.

## 8. History

History cho phep:

- loc theo ngay
- loc theo status
- tim keyword
- mo report
- xoa run

Du lieu den tu file local `runs.json`.

## 9. Profile va logout

- `Profile` hien thong tin user hien tai.
- `Logout` clear session, reset run config va quay ve login.

## 10. Meo su dung

- Luon kiem tra `Base URL` sau login neu doi moi truong backend.
- Dung `Request` de debug endpoint truoc khi tao testcase.
- Dung `payload assertions` khi response co field dong.
- Dung `expected response body` khi can so sanh full JSON.
- Xem `History` sau nhieu lan run de doi chieu hoi quy.
