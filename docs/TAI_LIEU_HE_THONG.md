# Tai lieu he thong

## 1. Tong quan

`API Test App` la ung dung desktop viet bang JavaFX de ho tro tester thuc thi, quan ly va theo doi testcase API. He thong hien tai co 2 nguon testcase:

- `Scenario co san trong code`: duoc dang ky qua `ApiScenarioRegistry`.
- `User testcase / user suite`: duoc tao trong giao dien va luu vao PostgreSQL.

Ngoai chuc nang chay test, ung dung con cung cap:

- login nguoi dung
- dashboard tong hop
- request builder de goi API thu cong
- report cho tung lan chay
- history de loc, xem va xoa ket qua da luu

## 2. Thanh phan chinh

### 2.1 Entry point va dieu huong

- `MainApplication`: khoi tao JavaFX, theme, scene, login view.
- `MainController`: dieu huong giua `Dashboard`, `Testcase`, `Request`, `Report`, `History`, `Profile`.

Ngay sau khi vao main screen, app mo dialog `Default run config` de nhap:

- `Base URL`
- `Alert mode`
- `Runner`

Gia tri nay duoc luu trong `AppRunConfig` va anh huong truc tiep den viec chay testcase.

### 2.2 Login va session

- `LoginController` xac thuc nguoi dung qua `UserRepository`.
- Session dang nhap duoc giu trong `AppSession`.
- Role tren UI hien tai chi la gia tri chon trong combobox; logic phan quyen thuc te trong code chua thay ro.

### 2.3 Chay testcase

`TestcaseController` la man hinh trung tam cua ung dung:

- hien tree `Collections` tu `ApiScenarioRegistry`
- nap testcase co san theo `ApiScenarioDefinition`
- nap them `user test suite` va `user test case` tu database
- ho tro chay `Run All` / `Run Selected`
- ho tro setup request, cleanup request, capture bien tu response
- luu ket qua chay vao `RunStorage`

App hien tai ho tro nhieu nhom scenario, trong do dang thay ro cac module:

- `Auth Module`
- `User Module`
- `Map Module`
- `Flow Module`
- `Bulk Test Module`
- `Real API`
- mot so scenario cu/phu tro con ten chua duoc chuan hoa, vi du `Collections 2`, `User2 Module2`

### 2.4 Request builder

`RequestController` cho phep:

- chon method `GET/POST/PUT/DELETE/PATCH`
- nhap URL tuyet doi hoac endpoint tuong doi
- gui raw body (`JSON`, `Text`, `XML`)
- xem status, response time, response body va response headers

Man hinh nay la cong cu goi API thu cong, khong luu lich su vao `RunStorage`.

### 2.5 Dashboard, report, history

- `DashboardController`: thong ke tong testcase da chay, tong run, pass/fail, danh sach run gan day.
- `ReportController`: chi tiet tung lan chay, gom tong hop pass/fail va bang ket qua tung testcase.
- `HistoryController`: loc ket qua theo ngay, keyword, status; co the mo report hoac xoa run.

### 2.6 Luu tru ket qua chay

`RunStorage` luu lich su run vao file JSON ngoai thu muc project:

```text
%LOCALAPPDATA%\api-test-app\runs.json
```

Ly do la tranh xung dot khoa file trong thu muc project tren Windows/OneDrive.

## 3. Du lieu va persistence

### 3.1 PostgreSQL

App dung PostgreSQL cho:

- `users`
- `roles`
- `client_machines`
- `user_test_suites`
- `user_test_cases`

Schema tham khao nam o [src/main/resources/db/database.sql](/D:/code/api-test-app/src/main/resources/db/database.sql).

### 3.2 Luu y quan trong ve file SQL

File `database.sql` hien tai khong phai migration sach de chay tu dau den cuoi. Trong file dang tron:

- sample insert
- cau `SELECT` tham khao
- `DROP TABLE`
- `CREATE TABLE`
- index goi y

Neu chay nguyen file theo thu tu hien tai, cac lenh `insert` o dau file co the fail neu bang chua ton tai. Khi setup moi truong moi, can tach hoac sap xep lai thu tu chay script.

## 4. Runtime config

### 4.1 Database

`ConnectionManager` doc cau hinh theo thu tu:

1. Java system property
2. Environment variable
3. Gia tri mac dinh trong code

Mac dinh:

- URL: `jdbc:postgresql://localhost:5432/api_test_app`
- User: `postgres`
- Password: `12345`

### 4.2 Base URL test

`AppRunConfig.DEFAULT_BASE_URL` hien tai la:

```text
http://group3.it4788.sukkaito.id.vn/api
```

Base URL co the doi trong dialog cau hinh hoac tren man hinh testcase.

## 5. Gioi han hien tai can ghi nhan

- Docs cu da mo ta sai pham vi san pham; da duoc thay the.
- `database.sql` can duoc don lai neu muon dung nhu migration/seed script chuan.
- Ten module/scenario trong `ApiScenarioRegistry` chua hoan toan dong nhat.
- Mot so controller nhu `ProfileController`, `EnvironmentsController`, `CollectionsController` moi o muc UI co ban.

## 6. Khi nao sua docs tiep

Can cap nhat lai docs neu co thay doi trong:

- schema PostgreSQL
- danh sach scenario provider
- storage format cua `RunStorage`
- luong login/phan quyen
- workflow tao `user test suite` va `user test case`
