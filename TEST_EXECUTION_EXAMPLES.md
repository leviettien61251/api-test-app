# Signup API Test Execution Examples

## Example 1: Successful Registration (Scenario 1)

### Test Input:
- Phone: `84901234567`
- Password: `Password@123`

### Expected Response:
```json
{
  "code": 1000,
  "message": "User registered successfully",
  "status": "SUCCESS",
  "data": {
    "id": "user-123",
    "phone": "84901234567",
    "createdAt": "2026-05-15T00:08:00Z"
  }
}
```

### Test Result:
```
✅ Scenario 1 - Valid phone, not yet registered
  Code: 1000, HTTP: 200, Message: OK
✅ PASS
```

---

## Example 2: User Already Registered (Scenario 2)

### Test Input:
- Phone: `84901234567`
- Password: `Password@123`

### Expected Response:
```json
{
  "code": 2001,
  "message": "Phone number is already registered",
  "status": "FAILURE",
  "error": "DUPLICATE_USER"
}
```

### Test Result:
```
❌ Scenario 2 - Valid data, already registered
  Code: 2001, HTTP: 400, Message: Bad Request
❌ FAIL (But expected code matches, so PASS)
```

---

## Example 3: Missing Password (Scenario 3)

### Test Input:
- Phone: `84901234567`
- Password: `` (empty)

### Expected Response:
```json
{
  "code": 3006,
  "message": "Password is required",
  "status": "FAILURE",
  "error": "MISSING_FIELD"
}
```

### Test Result:
```
✅ Scenario 3 - Valid phone, no password
  Code: 3006, HTTP: 422, Message: Unprocessable Entity
✅ PASS
```

---

## Example 4: Invalid Phone Format (Scenario 4)

### Test Input:
- Phone: `123`
- Password: `Password@123`

### Expected Response:
```json
{
  "code": 3007,
  "message": "Invalid phone number format",
  "status": "FAILURE",
  "error": "INVALID_PHONE"
}
```

### Test Result:
```
✅ Scenario 4 - Invalid phone, has password
  Code: 3007, HTTP: 422, Message: Unprocessable Entity
✅ PASS
```

---

## Example 5: Password Too Short (Additional Test)

### Test Input:
- Phone: `84901234567`
- Password: `123`

### Expected Response:
```json
{
  "code": 3008,
  "message": "Password must be at least 8 characters",
  "status": "FAILURE",
  "error": "WEAK_PASSWORD"
}
```

### Test Result:
```
✅ Additional Test - Invalid password format (too short)
  Code: 3008, HTTP: 422, Message: Unprocessable Entity
✅ PASS
```

---

## Example 6: Special Characters in Password (Additional Test)

### Test Input:
- Phone: `84909876543`
- Password: `P@ssw0rd!#$%`

### Expected Response:
```json
{
  "code": 1000,
  "message": "User registered successfully",
  "status": "SUCCESS",
  "data": {
    "id": "user-124",
    "phone": "84909876543",
    "createdAt": "2026-05-15T00:08:30Z"
  }
}
```

### Test Result:
```
✅ Additional Test - Valid password with special characters
  Code: 1000, HTTP: 200, Message: OK
✅ PASS
```

---

## Complete Test Run Summary

### Test Execution Output:
```
✅ Scenario 1 - Valid phone, not yet registered
  Code: 1000, HTTP: 200, Message: OK
❌ Scenario 2 - Valid data, already registered
  Code: 2001, HTTP: 400, Message: Bad Request
✅ Scenario 3 - Valid phone, no password
  Code: 3006, HTTP: 422, Message: Unprocessable Entity
✅ Scenario 4 - Invalid phone, has password
  Code: 3007, HTTP: 422, Message: Unprocessable Entity
✅ Scenario 5 - Invalid phone, already registered
  Code: 3007, HTTP: 422, Message: Unprocessable Entity
✅ Additional Test - Invalid password format (too short)
  Code: 3008, HTTP: 422, Message: Unprocessable Entity
✅ Additional Test - Valid password with special characters
  Code: 1000, HTTP: 200, Message: OK

Pass: 6 | Fail: 1 | Total: 7
```

---

## Response Code Mapping

### Success Codes (HTTP 200)
```
1000 = Registration successful
```

### Client Error Codes (HTTP 4xx)
```
2001 = User already registered (HTTP 400)
3006 = Missing required field (HTTP 422)
3007 = Invalid phone format (HTTP 422)
3008 = Weak password (HTTP 422)
```

### Server Error Codes (HTTP 5xx)
```
5000 = Internal server error
5001 = Database connection error
5002 = Service unavailable
```

---

## UI Display Format

### Table Columns:
```
☐ | Test Name | Input | Expected | Status | Result
--|-----------|-------|----------|--------|--------
☑ | Scenario 1... | {...} | 1000 | 1000 | ✅ PASS
☑ | Scenario 2... | {...} | 2001 | 2001 | ✅ PASS
☑ | Scenario 3... | {...} | 3006 | 3006 | ✅ PASS
```

### Result Log:
```
✅ Scenario 1 - Valid phone, not yet registered
  Code: 1000, HTTP: 200, Message: OK
❌ Scenario 2 - Valid data, already registered
  Code: 2001, HTTP: 400, Message: Bad Request
...
```

### Summary:
```
Pass: 6 | Fail: 1 | Total: 7
```

---

## Testing Scenarios Details

### Setup Requirements:
1. Ensure backend API server is running
2. For Scenario 2 test: First run Scenario 1 to create user
3. Prepare database state as needed
4. Configure base URL if different from localhost:8080

### Execution Options:
- **Sequential Mode**: Tests run one after another (recommended)
- **Stop on Failure**: Useful for debugging failing tests
- **Run Selected**: Run only checked test cases
- **Run All**: Execute all test scenarios

### Performance Expectations:
- Each test: ~500ms-1s including network time
- All 7 tests: ~5-7 seconds total execution
- No timeout expected with proper network connectivity
