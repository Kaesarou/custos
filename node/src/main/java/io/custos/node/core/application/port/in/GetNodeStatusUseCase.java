package io.custos.node.core.application.port.in;

import io.custos.node.core.domain.model.NodeStatus;

public interface GetNodeStatusUseCase {
    NodeStatus getNodeStatus();
}