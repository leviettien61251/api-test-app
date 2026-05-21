package com.example.apitestapp.services;

import com.example.apitestapp.services.auth.ChangePasswordScenarioProvider;
import com.example.apitestapp.services.auth.GetUserInfoScenarioProvider;
import com.example.apitestapp.services.auth.LoginScenarioProvider;
import com.example.apitestapp.services.auth.SignupScenarioProvider;
import com.example.apitestapp.services.map.*;
import com.example.apitestapp.services.user.SetUserInfoScenarioProvider;
import com.example.apitestapp.services.user.TestScenarioProvider;

import java.util.List;
import java.util.Optional;

public class ApiScenarioRegistry {

    private final List<ApiScenarioProvider> providers;

    public ApiScenarioRegistry() {
        this(List.of(
                new SignupScenarioProvider(),
                new LoginScenarioProvider(),
                new ChangePasswordScenarioProvider(),
                new GetUserInfoScenarioProvider(),
                new SetUserInfoScenarioProvider(),
                new MapTestScenarioProvider(),
                new NodeTestScenarioProvider(),
                new StepTestScenarioProvider(),
                new GetEdgesScenarioProvider(),
                new GetNodesTestScenarioProvider(),
                new GetFloorScenarioProvider(),
                new TestScenarioProvider(),
                new GetMetaScenarioProvider()

        ));
    }

    public ApiScenarioRegistry(List<ApiScenarioProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public List<ApiScenarioDefinition> getDefinitions() {
        return providers.stream()
                .map(ApiScenarioProvider::getDefinition)
                .toList();
    }

    public Optional<ApiScenarioDefinition> findByApiLabel(String apiLabel) {
        return getDefinitions().stream()
                .filter(definition -> definition.getApiLabel().equals(apiLabel))
                .findFirst();
    }
}
