# API Test App

`API Test App` la ung dung desktop JavaFX dung de chay va quan ly testcase API cho he thong backend. Repo hien tai khong con chi phuc vu bo signup test cu; no da mo rong thanh mot test client co login, dashboard, request builder, report, history va kha nang luu testcase/test suite do nguoi dung tu tao.

## Muc dich

- Chay testcase API theo bo scenario co san trong code.
- Tao them `user test suite` va `user test case` luu trong PostgreSQL.
- Goi request thu cong trong man hinh `Request`.
- Luu lich su thuc thi ra file JSON cuc bo de xem lai tren `Dashboard`, `Report`, `History`.

## Cong nghe chinh

- Java 21
- JavaFX 21
- Maven
- PostgreSQL
- OkHttp
- Gson
- Lombok
- AtlantaFX

## Diem can biet ngay

- Entry point UI: `com.example.apitestapp.MainApplication`
- Man hinh mo dau: `login-view.fxml`
- Default base URL runtime: `http://group3.it4788.sukkaito.id.vn/api`
- Cac test run duoc luu ngoai project tai `%LOCALAPPDATA%\api-test-app\runs.json`
- Cau hinh database co the ghi de bang:
  - system properties: `app.db.url`, `app.db.user`, `app.db.password`
  - environment variables: `APP_DB_URL`, `APP_DB_USER`, `APP_DB_PASSWORD`

## Chay ung dung

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run
```

Neu dung Maven da cai san:

```powershell
mvn clean test
mvn javafx:run
```

## Database mac dinh

Mac dinh app ket noi vao:

- URL: `jdbc:postgresql://localhost:5432/api_test_app`
- User: `postgres`
- Password: `12345`

Chi tiet thiet lap va canh bao ve file SQL nam trong [docs/THIET_LAP_VA_VAN_HANH.md](/D:/code/api-test-app/docs/THIET_LAP_VA_VAN_HANH.md) va [docs/TAI_LIEU_HE_THONG.md](/D:/code/api-test-app/docs/TAI_LIEU_HE_THONG.md).

## Tai lieu chinh

- [Chi muc tai lieu](/D:/code/api-test-app/docs/DOCUMENTATION_INDEX.md)
- [Tai lieu he thong](/D:/code/api-test-app/docs/TAI_LIEU_HE_THONG.md)
- [Use case tong quat](/D:/code/api-test-app/docs/USECASE_TONG_QUAT.md)
- [Thiet lap va van hanh](/D:/code/api-test-app/docs/THIET_LAP_VA_VAN_HANH.md)
- [Huong dan su dung](/D:/code/api-test-app/docs/HUONG_DAN_SU_DUNG.md)
- [Kien truc](/D:/code/api-test-app/docs/ARCHITECTURE.md)
