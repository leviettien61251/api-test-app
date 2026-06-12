# API Test App

`API Test App` la ung dung desktop JavaFX dung de kiem thu API, tao testcase, chay test hang loat va xem lai ket qua
run. Repo hien tai khong phai backend service; day la test client co UI login, dashboard, testcase runner, request
builder, report, history va luu testcase do nguoi dung tao vao PostgreSQL.

## Tong quan nhanh

- Nen tang: `Java 21`, `JavaFX 21`, `Maven`, `PostgreSQL`
- HTTP client: `OkHttp`
- JSON: `Gson`
- UI theme: `AtlantaFX`
- Entry point: `com.example.apitestapp.MainApplication`
- Main class Maven JavaFX: `com.example.apitestapp/com.example.apitestapp.MainApplication`
- Man hinh dau tien: `login-view.fxml`
- Base URL mac dinh: `http://group3.it4788.sukkaito.id.vn/api`
- Lich su run local: `%LOCALAPPDATA%\api-test-app\runs.json`, fallback `%USERPROFILE%\.api-test-app\runs.json`

## Chuc nang chinh

- Dang nhap bang email/password tu PostgreSQL.
- Hien dashboard tong hop run, pass/fail va run gan day.
- Nap testcase co san tu `ApiScenarioRegistry`.
- Tao, sua, xoa `user test suite` va `user test case`.
- Chay `Run All`, `Run Selected`, dung run dang chay.
- Ho tro setup request, cleanup request va capture bien response.
- Ho tro path params, query params, headers, request body, expected status, payload assertions va expected response
  body.
- Gui request thu cong voi method, URL, params, headers, raw body, form-data, Basic Auth, Bearer Token.
- Xem report chi tiet va lich su run da luu.

## Man hinh

- `Login`
- `Dashboard`
- `Testcase`
- `Request`
- `Report`
- `History`
- `Profile`

`Collections` va `Environments` co FXML/controller trong repo, nhung chua duoc noi vao navigation chinh.

## Scenario co san

`ApiScenarioRegistry` dang ky cac nhom provider:

- `Auth`: signup, login, change password, get user info
- `User`: set user info, set avatar, mot provider cu voi ten collection/module chua dong nhat
- `Map`: insert map/node/step, area, heatmap, path, edges, nodes, floor, meta, ward
- `Flow`: insert obstacle/density/bottleneck/heatmap/edge/edge status/edge density va cac API get
- `Real API`: `GET /map/nodes`

## Build, test va run

Dung Maven wrapper tren Windows:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run
```

Neu da cai Maven:

```powershell
mvn clean test
mvn javafx:run
```

## Cau hinh database

Hướng dẫn cài đặt đầy đủ: [Thiết lập PostgreSQL](docs/DATABASE_SETUP.md).

Script khởi tạo cơ sở dữ liệu mới: `src/main/resources/db/setup.sql`.

Mac dinh:

- URL: `jdbc:postgresql://localhost:5432/api_test_app`
- User: `postgres`
- Password: `12345`

Co the ghi de bang system properties:

- `app.db.url`
- `app.db.user`
- `app.db.password`

Hoac environment variables:

- `APP_DB_URL`
- `APP_DB_USER`
- `APP_DB_PASSWORD`

## Tai lieu

- [Chi muc tai lieu](docs/DOCUMENTATION_INDEX.md)
- [Tai lieu he thong](docs/TAI_LIEU_HE_THONG.md)
- [Ma tran man hinh](docs/MA_TRAN_MAN_HINH.md)
- [Use case tong quat](docs/USECASE_TONG_QUAT.md)
- [Use case phan ra](docs/USECASE_PHAN_RA.md)
- [Thiet lap va van hanh](docs/THIET_LAP_VA_VAN_HANH.md)
- [Huong dan su dung](docs/HUONG_DAN_SU_DUNG.md)
- [Kien truc](docs/ARCHITECTURE.md)
