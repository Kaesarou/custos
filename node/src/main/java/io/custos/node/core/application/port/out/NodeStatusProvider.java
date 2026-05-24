package io.custos.node.core.application.port.out;

import io.custos.node.core.domain.model.NodeStatus;

public interface NodeStatusProvider {
    NodeStatus getNodeStatus();
}