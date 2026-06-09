package com.example.apitestapp.services;

import com.example.apitestapp.models.dto.ApiScenarioDefinition;
import com.example.apitestapp.services.auth.ChangePasswordScenarioProvider;
import com.example.apitestapp.services.auth.GetUserInfoScenarioProvider;
import com.example.apitestapp.services.auth.LoginScenarioProvider;
import com.example.apitestapp.services.auth.SignupScenarioProvider;
import com.example.apitestapp.services.flow.*;
import com.example.apitestapp.services.map.*;
import com.example.apitestapp.services.realapitest.GetMapNodeFromRealApiScenarioProvider;
import com.example.apitestapp.services.user.SetAvatarScenarioProvider;
import com.example.apitestapp.services.user.SetUserInfoScenarioProvider;

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
                new SetAvatarScenarioProvider(),
                new MapTestScenarioProvider(),
                new NodeTestScenarioProvider(),
                new StepTestScenarioProvider(),
                new PostAreaTestScenarioProvider(),
                new PostHeatmapTestScenarioProvider(),
                new PostPathTestScenarioProvider(),
                new GetEdgesScenarioProvider(),
                new GetNodesTestScenarioProvider(),
                new GetFloorScenarioProvider(),
                new GetMetaScenarioProvider(),
                new WardTestScenarioProvider(),
                new FlowAlertScenarioProvider(),
                new FlowDensityScenarioProvider(),
                new FlowBottleneckScenarioProvider(),
                new FlowHeatmapScenarioProvider(),
                new InsertEdgeScenarioProvider(),
                new InsertEdgeStatusScenarioProvider(),
                new InsertEdgeDensityScenarioProvider(),
                new GetAlertsScenarioProvider(),
                new GetDensityScenarioProvider(),
                new GetHeatmapScenarioProvider(),
                new GetBottlenecksScenarioProvider(),
                new GetEdgeStatusScenarioProvider(),
                new GetMapNodeFromRealApiScenarioProvider()


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
