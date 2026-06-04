# API Test App

`API Test App` la ung dung desktop JavaFX dung de kiem thu va quan ly testcase API cho backend. He thong hien tai da vuot xa bo signup test cu; repo nay dang chua mot test client co login, dashboard, testcase runner, request builder, report, history va kha nang luu testcase do nguoi dung tao.

## Tong quan nhanh

- Nen tang: `Java 21`, `JavaFX 21`, `Maven`, `PostgreSQL`
- HTTP client: `OkHttp`
- JSON: `Gson`
- UI theme: `AtlantaFX`
- Entry point: `com.example.apitestapp.MainApplication`
- Man hinh mo dau: `login-view.fxml`
- Base URL runtime mac dinh: `http://group3.it4788.sukkaito.id.vn/api`
- Lich su run luu local: `%LOCALAPPDATA%\\api-test-app\\runs.json`

## Project nay dung de lam gi

- Chay testcase API co san duoc dang ky trong `ApiScenarioRegistry`
- Tao va quan ly `user test suite` / `user test case` trong PostgreSQL
- Goi request thu cong o man hinh `Request`
- Luu va xem lai lich su thuc thi tren `Dashboard`, `Report`, `History`
- So sanh response theo:
  - status code
  - payload assertion theo `jsonPath`
  - full expected response JSON

## Nhung module chinh trong app

- `Login`
- `Dashboard`
- `Testcase`
- `Request`
- `Report`
- `History`
- `Profile`

Ngoai ra repo con co `Collections` va `Environments`, nhung 2 man hinh nay hien chua duoc noi vao navigation chinh.

## Tinh nang quan trong

### Testcase runner

- Nap testcase tu scenario code san co
- Nap them user testcase tu database
- Ho tro `Run All`, `Run Selected`, `Stop`
- Ho tro `setup request` va `cleanup request`
- Ho tro `path params`, `query params`, `headers`, `request body`
- Ho tro `payload assertions` va `expected response body`
- Tu dong tao auth setup mac dinh neu testcase can `${token}` hoac `${authorizationHeader}`

### Request builder

- Gui `GET`, `POST`, `PUT`, `DELETE`, `PATCH`
- Ho tro body `JSON`, `Text`, `XML`
- Hien response status, time, body, headers

Luu y: UI auth (`No Auth`, `Basic Auth`, `Bearer Token`) da co, nhung code gui request hien tai chua dua thong tin auth nay vao HTTP headers.

## Chay project

Bang Maven wrapper:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run
```

Bang Maven da cai san:

```powershell
mvn clean test
mvn javafx:run
```

## Cau hinh database

Mac dinh:

- URL: `jdbc:postgresql://localhost:5432/api_test_app`
- User: `postgres`
- Password: `12345`

Co the ghi de bang:

- system properties: `app.db.url`, `app.db.user`, `app.db.password`
- environment variables: `APP_DB_URL`, `APP_DB_USER`, `APP_DB_PASSWORD`

## Tai lieu

- [Chi muc tai lieu](/D:/code/api-test-app/docs/DOCUMENTATION_INDEX.md)
- [Tai lieu he thong](/D:/code/api-test-app/docs/TAI_LIEU_HE_THONG.md)
- [Ma tran man hinh](/D:/code/api-test-app/docs/MA_TRAN_MAN_HINH.md)
- [Use case tong quat](/D:/code/api-test-app/docs/USECASE_TONG_QUAT.md)
- [Use case phan ra](/D:/code/api-test-app/docs/USECASE_PHAN_RA.md)
- [Thiet lap va van hanh](/D:/code/api-test-app/docs/THIET_LAP_VA_VAN_HANH.md)
- [Huong dan su dung](/D:/code/api-test-app/docs/HUONG_DAN_SU_DUNG.md)
- [Kien truc](/D:/code/api-test-app/docs/ARCHITECTURE.md)
