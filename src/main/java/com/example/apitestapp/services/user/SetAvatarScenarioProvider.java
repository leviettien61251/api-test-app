package com.example.apitestapp.services.user;

import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiCleanupRequest;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiSetupRequest;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.List;

public class SetAvatarScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(
                new ApiTestScenario(
                        "Scenario 1",
                        "Cập nhật avatar thành công khi user đã signup và login",
                        createAuthSetupRequests("0980000001"),
                        createSetAvatarRequestBody("https://example.com/avatar1.jpg", "0980000001"),
                        "1000",
                        "SUCCESS"
                ),
                new ApiTestScenario(
                        "Scenario 2",
                        "Thiếu avatarUrl",
                        createAuthSetupRequests("0980000002"),
                        """
                                {
                                  "phoneNumber": "0980000002"
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 3",
                        "avatarUrl null",
                        createAuthSetupRequests("0980000003"),
                        createSetAvatarRequestBody(null, "0980000003"),
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 4",
                        "avatarUrl để trống",
                        createAuthSetupRequests("0980000004"),
                        createSetAvatarRequestBody("", "0980000004"),
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 5",
                        "avatarUrl không hợp lệ",
                        createAuthSetupRequests("0980000005"),
                        createSetAvatarRequestBody("not-a-url", "0980000005"),
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 6",
                        "Thiếu phoneNumber",
                        createAuthSetupRequests("0980000006"),
                        """
                                {
                                  "avatarUrl": "https://example.com/avatar6.jpg"
                                }
                                """,
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 7",
                        "phoneNumber null",
                        createAuthSetupRequests("0980000007"),
                        createSetAvatarRequestBody("https://example.com/avatar7.jpg", null),
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 8",
                        "phoneNumber để trống",
                        createAuthSetupRequests("0980000008"),
                        createSetAvatarRequestBody("https://example.com/avatar8.jpg", ""),
                        "2001",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 9",
                        "phoneNumber không hợp lệ",
                        createAuthSetupRequests("0980000009"),
                        createSetAvatarRequestBody("https://example.com/avatar9.jpg", "123"),
                        "2003",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 10",
                        "User đã signup nhưng chưa login",
                        List.of(createSignupSetupRequest("0980000010")),
                        createSetAvatarRequestBody("https://example.com/avatar10.jpg", "0980000010"),
                        "3009",
                        "FAILURE"
                ),
                new ApiTestScenario(
                        "Scenario 11",
                        "User không tồn tại",
                        createSetAvatarRequestBody("https://example.com/avatar11.jpg", "0980000011"),
                        "3007",
                        "FAILURE"
                )
        );

        return ApiScenarioDefinition.builder()
                .collectionName("Collections")
                .moduleName("User Module")
                .apiLabel("POST /api/v1/set-avatar")
                .endpoint("/api/v1/set-avatar")
                .sampleRequestBody(scenarios.get(0).getRequestBody())
                .scenarios(scenarios)
                .cleanupRequests(List.of(
                        new ApiCleanupRequest(
                                "Clean user-test test data",
                                "DELETE",
                                "/api/v1/user-test/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                false
                        ),
                        new ApiCleanupRequest(
                                "Clean set-avatar test data",
                                "DELETE",
                                "/api/v1/set-avatar/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                false
                        ),
                        new ApiCleanupRequest(
                                "Clean login test data",
                                "DELETE",
                                "/api/v1/login/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        ),
                        new ApiCleanupRequest(
                                "Clean signup test data",
                                "DELETE",
                                "/api/v1/signup/clean",
                                "",
                                List.of("1000", "200", "204", "201"),
                                true
                        )
                ))
                .build();
    }

    private static List<ApiSetupRequest> createAuthSetupRequests(String phoneNumber) {
        return List.of(
                createSignupSetupRequest(phoneNumber),
                new ApiSetupRequest(
                        "Thêm dữ liệu mồi Login",
                        "POST",
                        "api/v1/login",
                        createAuthRequestBody(phoneNumber),
                        List.of("1000"),
                        true
                )
        );
    }

    private static ApiSetupRequest createSignupSetupRequest(String phoneNumber) {
        return new ApiSetupRequest(
                "Thêm dữ liệu mồi Signup",
                "POST",
                "api/v1/signup",
                createAuthRequestBody(phoneNumber),
                List.of("1000"),
                true
        );
    }

    private static String createAuthRequestBody(String phoneNumber) {
        return """
                {
                  "phoneNumber": "%s",
                  "password": "111111"
                }
                """.formatted(phoneNumber);
    }

    private static String createSetAvatarRequestBody(String avatarUrl, String phoneNumber) {
        return """
                {
                  "avatarUrl": %s,
                  "phoneNumber": %s
                }
                """.formatted(toJsonString(avatarUrl), toJsonString(phoneNumber));
    }

    private static String toJsonString(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value + "\"";
    }
}
