package io.custos.node.core.application.service;

import io.custos.node.core.application.port.in.GetNodeStatusUseCase;
import io.custos.node.core.application.port.out.NodeStatusProvider;
import io.custos.node.core.domain.model.NodeStatus;

public class GetNodeStatusService implements GetNodeStatusUseCase {

    private final NodeStatusProvider nodeStatusProvider;

    public GetNodeStatusService(NodeStatusProvider nodeStatusProvider) {
        this.nodeStatusProvider = nodeStatusProvider;
    }

    @Override
    public NodeStatus getNodeStatus() {
        return nodeStatusProvider.getNodeStatus();
    }
}