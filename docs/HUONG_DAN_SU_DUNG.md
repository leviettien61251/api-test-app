# Huong dan su dung

## 1. Dang nhap

Khi mo app:

1. nhap username/email
2. nhap password
3. chon role tren combobox
4. login de vao main shell

Sau khi login thanh cong, app hien dialog `Default run config`.

## 2. Cau hinh run mac dinh

Dialog gom:

- `Base URL`
- `Alert mode`
  - `Stop on fail`
  - `Continue`
- `Runner`

Gia tri nay duoc luu vao `AppRunConfig` va anh huong truc tiep den `Testcase` va `Request`.

## 3. Dieu huong nhanh

Phim tat hien co:

- `Ctrl + D`: Dashboard
- `Ctrl + T`: Testcase
- `Ctrl + R`: Request
- `Ctrl + E`: Report
- `Ctrl + H`: History

## 4. Man hinh Testcase

Day la man hinh nghiep vu chinh.

### 4.1 Nap testcase

Nguoi dung co the nap testcase tu:

- scenario co san trong `ApiScenarioRegistry`
- `User Test Suites` trong database

Khi chon mot API/scenario, app hien:

- method
- URL dich
- headers
- body
- bang testcase

### 4.2 Chay test

Nut chinh:

- `Run All`
- `Run Selected`
- `Stop`

Trong qua trinh chay, app co the:

1. resolve URL tu `Base URL` + endpoint
2. thay `path params` vao URL
3. ap `query params`
4. chay `setup requests`
5. capture runtime variables tu response setup
6. tu dong auth setup neu request can `${token}` hoac `${authorizationHeader}`
7. goi request chinh
8. so sanh `expected status code`
9. so sanh `payload assertions`
10. so sanh `expected response body` neu co
11. chay `cleanup requests`
12. luu ket qua vao `RunStorage`

### 4.3 Y nghia du lieu user testcase

User testcase hien tai co the chua:

- `headers`
- `query params`
- `path params`
- `request body`
- `setup requests`
- `cleanup requests`
- `payload assertions`
- `expected response body`
- `expected status code`

### 4.4 Ket qua va log

- bang testcase hien `PASS`, `FAIL`, `Dang test`, `Cho`
- list log hien setup, cleanup, auth setup, loi runtime
- summary hien tong pass/fail

## 5. Quan ly user test suite va user test case

Code hien co CRUD cho:

- `UserTestSuiteService`
- `UserTestCaseService`

Workflow tong quat:

1. tao suite
2. tao testcase thuoc suite hoac thuoc API label hien tai
3. nhap params/body/assertions
4. save vao PostgreSQL
5. nap lai trong tree `User Test Suites`

## 6. Man hinh Request

Dung de debug nhanh endpoint.

### Ho tro hien tai

- chon HTTP method
- nhap endpoint tuong doi hoac URL tuyet doi
- chon body `JSON`, `Text`, `XML`
- xem status, response time, response body, response headers

### Gioi han hien tai

- UI auth co `No Auth`, `Basic Auth`, `Bearer Token`
- nhung thong tin auth nay chua duoc ap vao request khi gui

## 7. Dashboard, Report, History

### Dashboard

- tong testcase da chay
- tong so run
- tong pass/fail
- danh sach run gan day

Double-click mot dong de mo report.

### Report

- thong tin run: runner, machine, os, started time
- tong testcase, pass, fail
- pie chart pass/fail
- bar chart response time
- bang chi tiet tung testcase

### History

- loc theo ngay
- loc theo status
- tim theo keyword
- mo report
- xoa run

## 8. Profile

Man hinh `Profile` hien tai chu yeu de xem thong tin user hien tai. Khong nen xem day la module chinh sua profile day du.

## 9. Meo su dung

- doi `Base URL` truoc khi test backend khac moi truong
- dung `Request` de debug endpoint truoc khi viet testcase
- dung `payload assertions` khi khong can so sanh full response JSON
- dung `expected response body` khi can so sanh toan bo response
- mo `History` sau nhieu lan run de doi chieu ket qua hoi quy
