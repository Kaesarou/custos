package io.custos.node.core.application.service;

import io.custos.node.core.application.port.in.GetNodeCapabilitiesUseCase;
import io.custos.node.core.application.port.out.NodeCapabilitiesProvider;
import io.custos.node.core.domain.model.NodeCapabilities;

public class GetNodeCapabilitiesService implements GetNodeCapabilitiesUseCase {

    private final NodeCapabilitiesProvider nodeCapabilitiesProvider;

    public GetNodeCapabilitiesService(NodeCapabilitiesProvider nodeCapabilitiesProvider) {
        this.nodeCapabilitiesProvider = nodeCapabilitiesProvider;
    }

    @Override
    public NodeCapabilities getNodeCapabilities() {
        return nodeCapabilitiesProvider.getNodeCapabilities();
    }
}