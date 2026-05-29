package com.example.apitestapp.services;

import com.example.apitestapp.services.auth.ChangePasswordScenarioProvider;
import com.example.apitestapp.services.auth.GetUserInfoScenarioProvider;
import com.example.apitestapp.services.auth.LoginScenarioProvider;
import com.example.apitestapp.services.auth.SignupScenarioProvider;
import com.example.apitestapp.services.flow.FlowAlertScenarioProvider;
import com.example.apitestapp.services.flow.FlowBottleneckScenarioProvider;
import com.example.apitestapp.services.flow.FlowDensityScenarioProvider;
import com.example.apitestapp.services.flow.FlowHeatmapScenarioProvider;
import com.example.apitestapp.services.flow.GetAlertsScenarioProvider;
import com.example.apitestapp.services.flow.GetBottlenecksScenarioProvider;
import com.example.apitestapp.services.flow.GetDensityScenarioProvider;
import com.example.apitestapp.services.flow.GetEdgeStatusScenarioProvider;
import com.example.apitestapp.services.flow.GetHeatmapScenarioProvider;
import com.example.apitestapp.services.flow.InsertEdgeDensityScenarioProvider;
import com.example.apitestapp.services.flow.InsertEdgeScenarioProvider;
import com.example.apitestapp.services.flow.InsertEdgeStatusScenarioProvider;
import com.example.apitestapp.services.map.*;
import com.example.apitestapp.services.realapitest.GetMapNodeFromRealApiScenarioProvider;
import com.example.apitestapp.services.user.SetAvatarScenarioProvider;
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
                new SetAvatarScenarioProvider(),
                new MapTestScenarioProvider(),
                new NodeTestScenarioProvider(),
                new StepTestScenarioProvider(),
                new GetEdgesScenarioProvider(),
                new GetNodesTestScenarioProvider(),
                new GetFloorScenarioProvider(),
                new TestScenarioProvider(),
                new GetMetaScenarioProvider(),
                new WardTestScenarioProvider(),
                new BulkTest1ScenarioProvider(),
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
