# Thiet lap va van hanh

## 1. Yeu cau

- JDK 21
- Maven 3.9+ hoac dung `mvnw.cmd`
- PostgreSQL
- Windows la moi truong duoc code nham toi ro nhat

## 2. Build va test

```powershell
.\mvnw.cmd clean test
```

Neu can package ma bo qua test:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Luu y:

- lan dau dung `mvnw.cmd` co the can tai Maven wrapper distribution
- neu may bi chan network/no sandbox network, wrapper co the fail truoc khi vao den compile

## 3. Chay ung dung

```powershell
.\mvnw.cmd javafx:run
```

## 4. Cau hinh database

App doc config DB tu `ConnectionManager`:

- `-Dapp.db.url=...`
- `-Dapp.db.user=...`
- `-Dapp.db.password=...`

Hoac environment variables:

- `APP_DB_URL`
- `APP_DB_USER`
- `APP_DB_PASSWORD`

Vi du:

```powershell
.\mvnw.cmd "-Dapp.db.url=jdbc:postgresql://localhost:5432/api_test_app" "-Dapp.db.user=postgres" "-Dapp.db.password=12345" javafx:run
```

## 5. Khoi tao schema

File schema tham khao:

- [database.sql](/D:/code/api-test-app/src/main/resources/db/database.sql)

Canh bao:

- file nay dang tron `insert`, `select`, `drop`, `create`, `index`
- khong nen copy-chay nguyen file ma khong kiem tra thu tu

Thu tu khuyen nghi khi setup moi:

1. Tao database `api_test_app`
2. Chay `CREATE EXTENSION IF NOT EXISTS "uuid-ossp";`
3. Chay khoi `DROP TABLE`
4. Chay khoi `CREATE TABLE`
5. Chay khoi `CREATE INDEX`
6. Sau do moi xem xet phan `INSERT`

## 6. Du lieu mau va rui ro

Trong `database.sql` dang co:

- seed role `admin`, `tester`
- seed user mau
- mot so cau `SELECT` tham khao
- mot cau `insert into client_machines ... values (?, ?, ...)` khong phai SQL script seed hop le de chay truc tiep

Tai lieu van hanh phai xem day la script tham khao pha tron, khong phai script deployment chuan.

## 7. Sau khi mo app

Ngay sau login thanh cong, app se mo dialog `Default run config` de nhap:

- `Base URL`
- `Alert mode`
- `Runner`
- machine name va OS se hien thi de tham khao

Mac dinh quan trong:

- `Base URL`: `http://group3.it4788.sukkaito.id.vn/api`
- `Alert mode`: `Stop on fail`

## 8. Storage cua ket qua test

Ket qua run luu local tai:

```text
%LOCALAPPDATA%\api-test-app\runs.json
```

Neu file nay con ton tai, run cu se hien lai trong:

- `Dashboard`
- `Report`
- `History`

## 9. Van hanh hang ngay

1. Dang nhap
2. Kiem tra `Base URL`
3. Chon scenario hoac user test suite
4. Chay `Run All` hoac `Run Selected`
5. Xem report / history

## 10. Su co thuong gap

### Khong ket noi duoc database

- kiem tra PostgreSQL dang chay
- kiem tra bien `app.db.*` hoac `APP_DB_*`
- kiem tra bang `users`, `roles`, `user_test_suites`, `user_test_cases`

### Login that bai

- `LoginController` dang tim user theo `email` va `password`
- kiem tra du lieu seed trong bang `users`

### Request/testcase loi ket noi backend

- kiem tra `Base URL`
- kiem tra backend dang chay
- kiem tra endpoint trong scenario hoac user testcase

### Request auth khong dung nhu mong doi

- UI auth o man hinh `Request` hien chua tham gia vao request headers thuc te
- neu can auth khi debug, phai sua code hoac dung endpoint/public case phu hop

### Khong thay lich su cu

- kiem tra file `%LOCALAPPDATA%\api-test-app\runs.json`
- kiem tra quyen ghi file
