package com.example.apitestapp.services.MapScenarios;

import com.example.apitestapp.services.ApiCleanupRequest;
import com.example.apitestapp.services.ApiScenarioDefinition;
import com.example.apitestapp.services.ApiScenarioProvider;
import com.example.apitestapp.services.ApiTestScenario;

import java.util.List;

public class GetFloorScenarioProvider implements ApiScenarioProvider {

    @Override
    public ApiScenarioDefinition getDefinition() {
        List<ApiTestScenario> scenarios = List.of(

        );

        return new ApiScenarioDefinition(
                "Collections",
                "Map Module",
                "POST /api/v1/map/",
                "/api/v1/signup",
                scenarios.get(0).getRequestBody(),
                scenarios,
                List.of(new ApiCleanupRequest(
                        "Clean signup test data",
                        "DELETE",
                        "/api/v1/signup/clean",
                        "",
                        List.of("1000", "200", "204", "201"),
                        true
                ))
        );
    }
}
