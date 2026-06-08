# Thiet lap va van hanh

## 1. Yeu cau

- JDK 21
- Maven 3.9+ hoac Maven wrapper `mvnw.cmd`
- PostgreSQL
- Windows la moi truong duoc code nham toi ro nhat

## 2. Build va test

```powershell
.\mvnw.cmd clean test
```

Package bo qua test:

```powershell
.\mvnw.cmd clean package -DskipTests
```

Lan dau dung wrapper co the can network de tai Maven distribution va dependencies.

## 3. Chay ung dung

```powershell
.\mvnw.cmd javafx:run
```

Main class cau hinh trong `pom.xml`:

```text
com.example.apitestapp/com.example.apitestapp.MainApplication
```

## 4. Cau hinh database

`ConnectionManager` doc theo thu tu:

1. Java system property
2. Environment variable
3. Gia tri mac dinh trong code

Mac dinh:

- `jdbc:postgresql://localhost:5432/api_test_app`
- `postgres`
- `12345`

System properties:

- `app.db.url`
- `app.db.user`
- `app.db.password`

Environment variables:

- `APP_DB_URL`
- `APP_DB_USER`
- `APP_DB_PASSWORD`

Vi du:

```powershell
.\mvnw.cmd "-Dapp.db.url=jdbc:postgresql://localhost:5432/api_test_app" "-Dapp.db.user=postgres" "-Dapp.db.password=12345" javafx:run
```

## 5. Khoi tao schema

File tham khao:

- `src/main/resources/db/database.sql`

Migration bo sung:

- `src/main/resources/db/migrations/20260602_add_user_test_case_path_params.sql`
- `src/main/resources/db/migrations/20260602_add_user_test_case_response_assertions.sql`

Canh bao: `database.sql` dang tron insert, select, drop, create va index. Khong nen chay nguyen file ma khong sua truoc.

Thu tu setup moi khuyen nghi:

1. Tao database `api_test_app`.
2. Chay `CREATE EXTENSION IF NOT EXISTS "uuid-ossp";`.
3. Chay khoi `DROP TABLE` neu can reset.
4. Chay khoi `CREATE TABLE`.
5. Chay khoi `CREATE INDEX`.
6. Chay migration trong thu muc `migrations` neu schema dang cu.
7. Them seed role/user hop le.

## 6. Du lieu seed

`database.sql` co seed role va user mau, nhung phan dau file hien co dau hieu khong phai SQL seed sach:

- co cau insert users bi tach boi dau `;` som
- co cau `insert into client_machines ... values (?, ?, ...)` chi phu hop prepared statement, khong phai script chay truc tiep
- co cac cau `SELECT` debug

Nen tach rieng file seed hop le neu can deployment on dinh.

## 7. Sau khi login

App tu dong:

- khoi tao `AppSession`
- luu client machine vao PostgreSQL
- mo Dashboard
- hien dialog `Default run config`

Dialog cho nhap:

- `Base URL`
- `Alert mode`

Mac dinh:

- `Base URL`: `http://group3.it4788.sukkaito.id.vn/api`
- `Alert mode`: `Stop on fail`

## 8. Storage run

RunStorage ghi:

```text
%LOCALAPPDATA%\api-test-app\runs.json
```

Fallback khi khong co `LOCALAPPDATA`:

```text
%USERPROFILE%\.api-test-app\runs.json
```

Neu file bi loi format JSON, app se log loi doc file va tiep tuc voi danh sach trong memory.

## 9. Van hanh hang ngay

1. Dam bao PostgreSQL va backend API dang chay.
2. Chay app.
3. Dang nhap.
4. Kiem tra `Base URL`.
5. Chon scenario hoac user suite.
6. Chay `Run All` hoac `Run Selected`.
7. Xem Dashboard, Report, History.

## 10. Su co thuong gap

### Khong ket noi duoc database

- Kiem tra PostgreSQL dang chay.
- Kiem tra `app.db.*` hoac `APP_DB_*`.
- Kiem tra schema co cac bang `users`, `roles`, `client_machines`, `user_test_suites`, `user_test_cases`.

### Login that bai

- `LoginController` tim user theo email va password.
- Kiem tra user seed trong bang `users`.
- Kiem tra password trong DB hien la plain text theo code hien tai.

### Testcase loi ket noi backend

- Kiem tra `Base URL`.
- Kiem tra endpoint trong scenario/user case.
- Kiem tra backend tra dung status/body mong doi.

### Request auth khong nhu mong doi

- Kiem tra auth type dang chon.
- Basic Auth/Bearer Token duoc set vao `Authorization`.
- Neu custom header cung la `Authorization`, gia tri auth UI se ghi de.

### Khong thay lich su cu

- Kiem tra file `%LOCALAPPDATA%\api-test-app\runs.json`.
- Kiem tra fallback `%USERPROFILE%\.api-test-app\runs.json`.
- Kiem tra quyen ghi file.
