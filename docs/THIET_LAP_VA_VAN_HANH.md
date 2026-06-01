# Thiet lap va van hanh

## 1. Yeu cau

- JDK 21
- Maven 3.9+ hoac dung wrapper `mvnw.cmd`
- PostgreSQL
- Windows la moi truong duoc code nham toi ro nhat

## 2. Build

```powershell
.\mvnw.cmd clean test
```

Neu muon bo qua test:

```powershell
.\mvnw.cmd clean package -DskipTests
```

## 3. Chay ung dung

```powershell
.\mvnw.cmd javafx:run
```

## 4. Cau hinh database

App doc config database tu `ConnectionManager`:

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

File schema hien nam o:

- [database.sql](/D:/code/api-test-app/src/main/resources/db/database.sql)

Canh bao:

- file nay dang gom ca `insert`, `select`, `drop`, `create`
- khong nen copy-chay nguyen file ma khong kiem tra thu tu

Khuyen nghi setup:

1. Tao database `api_test_app`.
2. Chay `CREATE EXTENSION IF NOT EXISTS "uuid-ossp";`
3. Chay khoi `DROP TABLE` va `CREATE TABLE`.
4. Chay cac `CREATE INDEX`.
5. Neu can account mau, moi chay phan `INSERT` sau khi bang da ton tai.

## 6. Tai khoan mau

Trong `database.sql` dang co du lieu mau cho:

- admin
- tester

Nhung du lieu insert dau file hien bi lap va chua duoc don lai. Hay xem lai truoc khi dung o moi truong chia se.

## 7. Storage cua ket qua test

Ket qua run duoc luu tai:

```text
%LOCALAPPDATA%\api-test-app\runs.json
```

Muoi test run cu van hien tren `Dashboard`, `Report`, `History` neu file nay con ton tai.

## 8. Van hanh hang ngay

1. Dang nhap vao app.
2. Nhap `Base URL`, `Alert mode`, `Runner` trong dialog cau hinh.
3. Chon scenario trong `Testcase`.
4. Chay test va xem ket qua.
5. Dung `History`/`Report` de xem lai cac run da luu.

## 9. Su co thuong gap

### Khong ket noi duoc database

- kiem tra PostgreSQL dang chay
- kiem tra `app.db.*` hoac `APP_DB_*`
- kiem tra schema da tao du bang `users`, `roles`, `user_test_suites`, `user_test_cases`

### Login khong thanh cong

- kiem tra bang `users`
- kiem tra email/password seed
- luu y `LoginController` dang tim user theo email va password

### Request test tra ve loi ket noi

- kiem tra `Base URL`
- kiem tra backend dang chay
- kiem tra endpoint trong scenario hoac user testcase

### Khong thay lich su cu

- kiem tra file `%LOCALAPPDATA%\api-test-app\runs.json`
- kiem tra quyen ghi file tren may
