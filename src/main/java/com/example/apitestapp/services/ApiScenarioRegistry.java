package com.example.apitestapp.services;

import com.example.apitestapp.services.AuthScenarios.ChangePasswordScenarioProvider;
import com.example.apitestapp.services.AuthScenarios.LoginScenarioProvider;
import com.example.apitestapp.services.AuthScenarios.SignupScenarioProvider;

import java.util.List;
import java.util.Optional;

public class ApiScenarioRegistry {

    private final List<ApiScenarioProvider> providers;

    public ApiScenarioRegistry() {
        this(List.of(
                new SignupScenarioProvider(),
                new LoginScenarioProvider(),
                new ChangePasswordScenarioProvider()
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
