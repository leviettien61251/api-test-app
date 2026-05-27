test# Signup API Test Cases Documentation

## Overview

This document describes the test cases implemented for the signup API endpoint: `http://localhost:8080/api/v1/signup`

## Test Scenarios

### Scenario 1: Valid data, not yet registered → SUCCESS

- **Phone**: 84901234567
- **Password**: Password@123
- **Expected Response Code**: 1000
- **Expected Status**: SUCCESS
- **Description**: Valid phone number and strong password for a new user

### Scenario 2: Valid data, already registered → FAILURE

- **Phone**: 84901234567
- **Password**: Password@123
- **Expected Response Code**: 2001
- **Expected Status**: FAILURE
- **Description**: Same credentials as Scenario 1, but user already exists in system

### Scenario 3: Valid phone, no password → FAILURE

- **Phone**: 84901234567
- **Password**: (empty)
- **Expected Response Code**: 3006
- **Expected Status**: FAILURE
- **Description**: Missing password field is required

### Scenario 4: Invalid phone, has password → FAILURE

- **Phone**: 123
- **Password**: Password@123
- **Expected Response Code**: 3007
- **Expected Status**: FAILURE
- **Description**: Invalid phone format (too short/invalid)

### Scenario 5: Invalid phone, has password, already registered → FAILURE

- **Phone**: invalid
- **Password**: Password@123
- **Expected Response Code**: 3007
- **Expected Status**: FAILURE
- **Description**: Invalid phone format even if user might be registered

### Additional Test: Invalid password format (too short)

- **Phone**: 84901234567
- **Password**: 123
- **Expected Response Code**: 3008
- **Expected Status**: FAILURE
- **Description**: Password is too short and doesn't meet security requirements

### Additional Test: Valid password with special characters

- **Phone**: 84909876543
- **Password**: P@ssw0rd!#$%
- **Expected Response Code**: 1000
- **Expected Status**: SUCCESS
- **Description**: Valid password with multiple special characters should work

## Expected Response Codes

- **1000**: SUCCESS - User successfully registered
- **2001**: FAILURE - User already registered with this phone number
- **3006**: FAILURE - Missing required field (password)
- **3007**: FAILURE - Invalid phone number format
- **3008**: FAILURE - Invalid password format (too short or doesn't meet requirements)

## How to Run Tests

### Using the GUI

1. Launch the application
2. Navigate to the "Auth Module" → "POST /api/v1/signup" in the Collections tree
3. Select test cases to run (or click "Run All")
4. Click "Run All" or "Run Selected" button
5. Monitor results in the Result Log

### Test Configuration

- **Base URL**: http://localhost:8080
- **Endpoint**: /api/v1/signup
- **HTTP Method**: POST
- **Content-Type**: application/json

### Request Body Format

```json
{
  "phone": "84901234567",
  "password": "Password@123"
}
```

## Implementation Classes

### SignupTestScenarios.java

Service class that provides all test scenarios in a structured format.

- Method: `getSignupScenarios()` returns `List<SignupTestData>`

### SignupTestData.java

Model class representing a single test scenario with:

- scenario: Test scenario name
- phone: Phone number to test
- password: Password to test
- expectedCode: Expected response code from API
- expectedStatus: Expected status (SUCCESS/FAILURE)
- description: Human-readable description

### ApiTestService.java

Service class that handles actual HTTP calls to the signup API.

- Method: `callSignupApi(String phone, String password)` returns `ApiResponse`
- Handles connection, JSON serialization, and response parsing

### TestcaseController.java (Updated)

JavaFX controller that manages the test UI and execution.

- Loads test scenarios when user selects "POST /api/v1/signup"
- Executes tests sequentially or in parallel
- Displays results with pass/fail indicators
- Shows response codes and HTTP status messages

## Testing Tips

1. **Pre-requisites**:
    - Target API server must be running on localhost:8080
    - Database must be prepared with/without test data as needed

2. **For Scenario 2** (Already registered):
    - First run Scenario 1 to register the user
    - Then run Scenario 2 to test duplicate registration

3. **Execution Modes**:
    - Sequential: Tests run one at a time with 500ms delay
    - Can stop on first failure for faster feedback

4. **Result Interpretation**:
    - ✅ PASS: Response code matches expected code
    - ❌ FAIL: Response code doesn't match expected
    - Details shown in Result Log with HTTP codes and messages

## Error Handling

The test service includes error handling for:

- Network connection failures
- JSON parsing errors
- Missing response body
- Timeout scenarios (10-second timeout per request)

All errors are logged in the Result Log panel.
