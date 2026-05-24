package io.custos.node.core.application.port.in;

import io.custos.node.core.domain.model.NodePeers;

public interface GetNodePeersUseCase {
    NodePeers getNodePeers();
}