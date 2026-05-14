# Signup API Test Cases - Quick Reference

## Test Scenarios at a Glance

```
SCENARIO 1 ✅ SUCCESS
Phone: 84901234567 | Password: Password@123 | Code: 1000
Description: Valid data, not yet registered

SCENARIO 2 ❌ FAILURE  
Phone: 84901234567 | Password: Password@123 | Code: 2001
Description: Valid data, already registered

SCENARIO 3 ❌ FAILURE
Phone: 84901234567 | Password: (empty) | Code: 3006
Description: Valid phone, no password

SCENARIO 4 ❌ FAILURE
Phone: 123 | Password: Password@123 | Code: 3007
Description: Invalid phone, has password

SCENARIO 5 ❌ FAILURE
Phone: invalid | Password: Password@123 | Code: 3007
Description: Invalid phone, already registered

TEST 6 ❌ FAILURE
Phone: 84901234567 | Password: 123 | Code: 3008
Description: Invalid password format (too short)

TEST 7 ✅ SUCCESS
Phone: 84909876543 | Password: P@ssw0rd!#$% | Code: 1000
Description: Valid password with special characters
```

## Response Codes

| Code | Meaning            | HTTP | Scenario      |
|------|--------------------|------|---------------|
| 1000 | Success            | 200  | Scenario 1, 7 |
| 2001 | Already registered | 400  | Scenario 2    |
| 3006 | Missing password   | 422  | Scenario 3    |
| 3007 | Invalid phone      | 422  | Scenario 4, 5 |
| 3008 | Weak password      | 422  | Scenario 6    |

## Classes & Files

```
ApiTestService.java
├─ callSignupApi(phone, password) → ApiResponse
└─ ApiResponse
   ├─ getHttpCode()
   ├─ getResponseCode()
   └─ getStatusMessage()

SignupTestScenarios.java
└─ getSignupScenarios() → List<SignupTestData>

SignupTestData.java
├─ scenario (String)
├─ phone (String)
├─ password (String)
├─ expectedCode (String)
├─ expectedStatus (String)
└─ description (String)

TestcaseController.java (Modified)
├─ handleApiSelection(apiName)
├─ runTests(all)
└─ callActualApi(testCase)
```

## Quick Test Flow

1. **Launch App** → Click TestcaseController view
2. **Select API** → Collections → Auth Module → POST /api/v1/signup
3. **Choose Mode** → Sequential recommended
4. **Select Tests** → Check boxes or Run All
5. **Execute** → Click "Run All" button
6. **Review Results** → ✅ PASS or ❌ FAIL in table & log

## Key Methods

### ApiTestService

```java
ApiTestService service = new ApiTestService();
ApiResponse response = service.callSignupApi("84901234567", "Password@123");

// Use response:
int httpCode = response.getHttpCode();        // 200, 400, 422
String code = response.getResponseCode();     // "1000", "2001", etc
String msg = response.getStatusMessage();     // "OK", "Bad Request", etc
boolean ok = response.isSuccess();            // true/false
String body = response.getResponseBody();     // JSON string
```

### SignupTestScenarios

```java
List<SignupTestData> tests = SignupTestScenarios.getSignupScenarios();
for(
SignupTestData test :tests){
        System.out.

println(test.getScenario());      // "Scenario 1"
        System.out.

println(test.getPhone());         // "84901234567"
        System.out.

println(test.getPassword());      // "Password@123"
        System.out.

println(test.getExpectedCode());  // "1000"
        System.out.

println(test.getDescription());   // "..."
        }
```

## Configuration

| Setting         | Default               | Note                  |
|-----------------|-----------------------|-----------------------|
| Base URL        | http://localhost:8080 | Can change in service |
| Endpoint        | /api/v1/signup        | Fixed path            |
| Method          | POST                  | Fixed HTTP method     |
| Connect Timeout | 10 seconds            | Network timeout       |
| Read Timeout    | 10 seconds            | Response timeout      |
| Write Timeout   | 10 seconds            | Request timeout       |

## Test Execution Modes

- **Sequential**: One test at a time (default)
- **Parallel**: All tests at once (available in dropdown)
- **Stop on Fail**: Halts on first failure (option available)
- **Run All**: Execute all test cases
- **Run Selected**: Execute only checked cases

## Expected Output

### Individual Test:

```
✅ Scenario 1 - Valid phone, not yet registered
  Code: 1000, HTTP: 200, Message: OK
```

### Summary:

```
Pass: 6 | Fail: 1 | Total: 7
```

## Dependencies Added

```xml

<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

## Testing Tips

✓ Run Scenario 1 before Scenario 2 (creates test user)
✓ Use Sequential mode for predictable results
✓ Check Result Log for detailed error messages
✓ Verify backend is running before testing
✓ Invalid credentials intentionally included for negative testing

## Troubleshooting

| Problem             | Solution                  |
|---------------------|---------------------------|
| Connection refused  | Start backend API server  |
| Timeout errors      | Check network/firewall    |
| Wrong response code | Verify API implementation |
| JSON parsing error  | Check API response format |
| No tests loading    | Select API in tree view   |

## Files Created

1. `SignupTestData.java` - Model class
2. `SignupTestScenarios.java` - Test data provider
3. `ApiTestService.java` - HTTP client service
4. `TEST_CASES_DOCUMENTATION.md` - Full documentation
5. `IMPLEMENTATION_SUMMARY.md` - Implementation details
6. `TEST_EXECUTION_EXAMPLES.md` - Example outputs
7. `QUICK_REFERENCE.md` - This file

## Next Steps

1. Start backend API server on localhost:8080
2. Launch the application
3. Navigate to POST /api/v1/signup in Collections
4. Click "Run All" to execute all 7 test scenarios
5. Review results and response codes
6. Check logs for detailed debugging information
