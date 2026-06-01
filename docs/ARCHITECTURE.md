# Kien truc

## 1. Tong the

Ung dung di theo cau truc JavaFX desktop truyen thong:

- `views` (`.fxml`) cho giao dien
- `controllers` cho event va binding UI
- `services` cho logic test, orchestration va storage
- `repository` cho truy cap PostgreSQL
- `models` cho object nghiep vu
- `config` cho session va runtime state

## 2. Cau truc package

```text
src/main/java/com/example/apitestapp
|-- MainApplication.java
|-- MainController.java
|-- config/
|-- controllers/
|-- db/
|-- models/
|-- repository/
`-- services/
```

## 3. Luong mo man hinh

```text
MainApplication
  -> login-view.fxml
  -> LoginController
  -> MainApplication.showMainView()
  -> main-view.fxml
  -> MainController
```

`MainController` cache cac view con va goi `refresh()` neu controller implement `RefreshableView`.

## 4. Luong chay testcase

```text
TestcaseController
  -> ApiScenarioRegistry / UserTestSuiteService / UserTestCaseService
  -> ApiTestService
  -> ApiPayloadAssertionEvaluator
  -> RunStorage
```

Chi tiet:

1. Nguoi dung chon scenario hoac user suite.
2. `TestcaseController` nap `ApiTestScenario` vao `TableView`.
3. Khi run:
   - setup requests duoc goi truoc
   - response variable duoc capture theo `jsonPath`
   - token auth duoc tu sinh neu body/header dung placeholder auth
   - API chinh duoc goi bang `ApiTestService`
   - payload assertion va expected code duoc doi chieu
   - cleanup requests duoc goi sau cung
4. `TestRun` va `TestResult` duoc luu qua `RunStorage`.

## 5. Persistence

### PostgreSQL

`repository/` xu ly CRUD cho:

- `UserRepository`
- `RoleRepository`
- `ClientMachineRepository`
- `UserTestSuiteRepository`
- `UserTestCaseRepository`

### File JSON local

`RunStorage` luu lich su run vao file JSON o `%LOCALAPPDATA%`.

Dieu nay tach ket qua test khoi database va khoi workspace, nhung van cho UI doc lai nhanh.

## 6. Runtime state

- `AppSession`: nguoi dung hien tai, username, role
- `AppRunConfig`: base URL, alert mode, runner, thoi diem config
- `SelectedRunContext`: run dang duoc mo tren `Report`

## 7. Thanh phan can luu y

### Scenario providers

`ApiScenarioRegistry` dang ky cac provider cho nhieu nhom API:

- auth
- user
- map
- flow
- real API
- bulk test

Moi provider tra ve `ApiScenarioDefinition`, trong do co:

- collection name
- module name
- api label
- endpoint
- sample request body
- danh sach scenario
- cleanup requests

### Hook requests

`ApiSetupRequest` va `ApiCleanupRequest` cho phep testcase co lifecycle day du:

- tao du lieu truoc khi test
- xoa hoac rollback sau khi test
- capture bien runtime tu response setup

## 8. Diem ky thuat can cai thien

- chuan hoa ten collection/module trong cac provider
- tach `database.sql` thanh migration/seed ro rang
- bo sung phan quyen thuc te neu role `Admin/Tester` can hanh vi khac nhau
- tang test coverage cho controller/service phuc tap, dac biet `TestcaseController`
