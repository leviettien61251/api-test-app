# Signup Test System Architecture

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    JavaFX GUI Application                       │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ TestcaseController                                       │  │
│  │ ─────────────────────────────────────────────────────── │  │
│  │ • TreeView (Collections/APIs)                           │  │
│  │ • TableView (Test Cases with ✅/❌ results)            │  │
│  │ • ListView (Execution Log)                             │  │
│  │ • Buttons (Run All, Run Selected, Stop)                │  │
│  │ • Summary (Pass/Fail counts)                           │  │
│  │                                                          │  │
│  │ handleApiSelection(apiName)                            │  │
│  │  └─> Loads SignupTestScenarios                         │  │
│  │      Populates TableView with 7 test cases             │  │
│  │                                                          │  │
│  │ runTests(all)                                           │  │
│  │  └─> Thread.start()                                    │  │
│  │      └─> For each TestCaseModel                        │  │
│  │          └─> callActualApi(tc)                         │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│                              │ Creates instances of             │
│                              ▼                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ ApiTestService                                           │  │
│  │ ─────────────────────────────────────────────────────── │  │
│  │ • OkHttpClient (HTTP communication)                     │  │
│  │ • baseUrl = "http://localhost:8080"                     │  │
│  │                                                          │  │
│  │ callSignupApi(phone, password)                          │  │
│  │  ├─> Create JSON request body                           │  │
│  │  ├─> POST to /api/v1/signup                            │  │
│  │  ├─> Execute HTTP request (10s timeout)                │  │
│  │  ├─> Parse JSON response                               │  │
│  │  └─> Return ApiResponse                                │  │
│  │                                                          │  │
│  │ ApiResponse                                             │  │
│  │  ├─ httpCode: int (200, 400, 422, etc)                │  │
│  │  ├─ isSuccess: boolean                                 │  │
│  │  ├─ responseBody: String (JSON)                        │  │
│  │  └─ statusMessage: String (OK, Bad Request, etc)       │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              ▲                                   │
│                              │ Uses                              │
└──────────────────────────────┼─────────────────────────────────┘
                               │
                               │ Uses
                               ▼
              ┌─────────────────────────────────┐
              │  SignupTestScenarios            │
              │ ─────────────────────────────── │
              │ getSignupScenarios()            │
              │  └─> Returns List<SignupTestData>
              │                                  │
              │ Scenario 1: Valid data → 1000   │
              │ Scenario 2: Duplicate → 2001    │
              │ Scenario 3: No password → 3006  │
              │ Scenario 4: Bad phone → 3007    │
              │ Scenario 5: Bad phone+dup → 3007│
              │ Test 6: Weak password → 3008    │
              │ Test 7: Valid special → 1000    │
              └─────────────────────────────────┘
                        ▲
                        │ Uses
                        ▼
              ┌─────────────────────────────────┐
              │  SignupTestData (Model)         │
              │ ─────────────────────────────── │
              │ • scenario: String              │
              │ • phone: String                 │
              │ • password: String              │
              │ • expectedCode: String          │
              │ • expectedStatus: String        │
              │ • description: String           │
              │ • @Builder (Lombok)             │
              └─────────────────────────────────┘
                        ▲
                        │
                        │ Contains (7 instances)
                        │
                ┌───────┴───────┬──────────┬──────────┬──────────┐
                │               │          │          │          │
            S1: 1000        S2: 2001  S3: 3006  S4: 3007  T6,7
```

## Data Flow Diagram

```
User Interface
      │
      ├─► Select "POST /api/v1/signup" in tree
      │
      ▼
TestcaseController.handleApiSelection()
      │
      ├─► SignupTestScenarios.getSignupScenarios()
      │       │
      │       └─► Returns List<SignupTestData> (7 items)
      │
      ▼
Load tests into TableView
      │
      ├─► TestCaseModel (with phone, password fields)
      ├─► TestCaseModel
      ├─► ... (7 total)
      │
      ▼
User clicks "Run All"
      │
      ▼
TestcaseController.runTests(true)
      │
      ├─► Start new Thread
      │       │
      │       └─► For each TestCaseModel in testData:
      │               │
      │               ├─► callActualApi(tc)
      │               │     │
      │               │     ├─► ApiTestService.callSignupApi(phone, password)
      │               │     │     │
      │               │     │     ├─► Build JSON request
      │               │     │     ├─► POST to http://localhost:8080/api/v1/signup
      │               │     │     ├─► Wait for response (max 10s)
      │               │     │     ├─► Parse response code
      │               │     │     │
      │               │     │     └─► Return ApiResponse
      │               │     │
      │               │     ├─► Compare expectedCode vs actualCode
      │               │     │
      │               │     └─► Return boolean (PASS/FAIL)
      │               │
      │               ├─► Update UI on JavaFX thread:
      │               │   ├─ Set result (✅ PASS or ❌ FAIL)
      │               │   ├─ Set status (response code)
      │               │   └─ Log result with details
      │               │
      │               └─► Sleep 500ms
      │
      ▼
Test Suite Complete
      │
      └─► Display Summary
            ├─ Pass: X
            ├─ Fail: Y
            └─ Total: 7
```

## Request/Response Flow

```
┌─────────────────────────────────────┐
│  Test Case                          │
│  ├─ Phone: "84901234567"           │
│  ├─ Password: "Password@123"        │
│  └─ Expected Code: "1000"           │
└──────────────┬──────────────────────┘
               │
               ▼
    ┌────────────────────────┐
    │ HTTP POST Request      │
    ├────────────────────────┤
    │ URL: http://localhost: │
    │      8080/api/v1/signup│
    │                        │
    │ Headers:              │
    │ Content-Type:         │
    │ application/json      │
    │                        │
    │ Body:                 │
    │ {                      │
    │  "phone":             │
    │   "84901234567",      │
    │  "password":          │
    │   "Password@123"      │
    │ }                      │
    └────────────┬───────────┘
                 │
                 │ (Network)
                 ▼
    ┌────────────────────────┐
    │ Backend API Server     │
    │ localhost:8080         │
    └────────────┬───────────┘
                 │
                 │ Validate & Process
                 │ ├─ Check phone format
                 │ ├─ Check password strength
                 │ ├─ Query database
                 │ └─ Insert or return error
                 │
                 ▼
    ┌────────────────────────┐
    │ HTTP Response          │
    ├────────────────────────┤
    │ Status Code: 200 OK    │
    │                        │
    │ Body:                  │
    │ {                      │
    │  "code": 1000,         │
    │  "message":            │
    │   "User registered",   │
    │  "status": "SUCCESS"   │
    │ }                      │
    └────────────┬───────────┘
                 │
                 │ (Network)
                 ▼
    ┌────────────────────────┐
    │ Parse Response         │
    │ ├─ Extract code: 1000  │
    │ ├─ Get HTTP code: 200  │
    │ └─ Get message: OK     │
    └────────────┬───────────┘
                 │
                 ▼
    ┌────────────────────────┐
    │ Compare & Validate     │
    │ Expected: 1000         │
    │ Actual: 1000           │
    │ Match: ✅ YES → PASS   │
    └────────────┬───────────┘
                 │
                 ▼
    ┌────────────────────────┐
    │ Update UI              │
    │ Result: ✅ PASS        │
    │ Status: 1000           │
    │ Message: Code: 1000,   │
    │          HTTP: 200     │
    └────────────────────────┘
```

## Class Dependencies

```
TestcaseController
    │
    ├─ depends on ─► ApiTestService
    │                  │
    │                  └─ depends on ─► OkHttp3
    │                                   Gson
    │
    └─ depends on ─► SignupTestScenarios
                       │
                       └─ uses ────────► SignupTestData
                                         (Lombok @Builder)

Module Imports:
  • javafx.* (UI components)
  • okhttp3.* (HTTP client)
  • com.google.gson.* (JSON processing)
  • java.util.* (Collections)
```

## Testing Scenarios Hierarchy

```
SignupTestScenarios (7 Test Cases)
│
├─ Scenario 1: SUCCESS PATH ✅
│  ├─ Phone: 84901234567 (Valid)
│  ├─ Password: Password@123 (Valid)
│  └─ Expected: 1000 (Success)
│
├─ Scenario 2: DUPLICATE USER ❌
│  ├─ Phone: 84901234567 (Same as S1)
│  ├─ Password: Password@123
│  └─ Expected: 2001 (Already registered)
│
├─ Scenario 3: MISSING FIELD ❌
│  ├─ Phone: 84901234567 (Valid)
│  ├─ Password: (Empty)
│  └─ Expected: 3006 (Missing field)
│
├─ Scenario 4: INVALID PHONE ❌
│  ├─ Phone: 123 (Too short)
│  ├─ Password: Password@123 (Valid)
│  └─ Expected: 3007 (Invalid phone)
│
├─ Scenario 5: INVALID PHONE + DUPLICATE ❌
│  ├─ Phone: invalid (Invalid format)
│  ├─ Password: Password@123 (Valid)
│  └─ Expected: 3007 (Invalid phone)
│
├─ Test 6: WEAK PASSWORD ❌
│  ├─ Phone: 84901234567 (Valid)
│  ├─ Password: 123 (Too short)
│  └─ Expected: 3008 (Weak password)
│
└─ Test 7: SPECIAL CHARS ✅
   ├─ Phone: 84909876543 (Valid, different)
   ├─ Password: P@ssw0rd!#$% (Valid with special chars)
   └─ Expected: 1000 (Success)
```

## Execution Flow (Sequence Diagram)

```
User              Controller         Service           API
  │                  │                 │                │
  ├─ Click API ─────►│                 │                │
  │                  │                 │                │
  │                  ├─ Load scenarios─►│                │
  │                  │                 │                │
  │◄─ Display tests ─┤                 │                │
  │                  │                 │                │
  ├─ Click Run ─────►│                 │                │
  │                  │                 │                │
  │                  ├─ Thread.start()─┤                │
  │                  │                 │                │
  │                  ├─ For each test ─┤                │
  │                  │                 │                │
  │                  │  ├─ callApi() ──────► POST req ─►│
  │                  │  │                    │           │
  │                  │  │                    │ Process   │
  │                  │  │                    │ Validate  │
  │                  │  │                    │           │
  │                  │  │◄─ Response ───────◄│
  │                  │  │                    │
  │                  │  ├─ Compare codes    │
  │                  │  │                    │
  │                  │  └─ Return PASS/FAIL │
  │                  │                      │
  │◄─ Update UI ─────┤ (every 500ms)        │
  │                  │                      │
  │                  └─ Summary when done   │
  │                                          │
  ├─ View results ──────────────────────────┤
  │                                          │
```

---

## Technology Stack

```
Framework Layer:
  └─ JavaFX (UI Controls, Layouts, Bindings)

Business Logic Layer:
  ├─ TestcaseController (Orchestration)
  ├─ ApiTestService (HTTP Communication)
  └─ SignupTestScenarios (Test Data)

Data Layer:
  ├─ SignupTestData (Model)
  └─ TestCaseModel (UI Model)

External Libraries:
  ├─ OkHttp3 (HTTP Client)
  ├─ Gson (JSON Processing)
  └─ Lombok (Code Generation)

Network Layer:
  └─ HTTP/JSON API @ http://localhost:8080
```

This architecture ensures:

- ✅ Separation of concerns
- ✅ Easy testing and mocking
- ✅ Extensible design
- ✅ UI responsiveness (threading)
- ✅ Error handling
- ✅ Real API integration
