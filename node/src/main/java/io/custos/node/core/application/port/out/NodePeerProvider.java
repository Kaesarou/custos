package io.custos.node.core.application.port.out;

import io.custos.node.core.domain.model.NodePeers;

public interface NodePeerProvider {
    NodePeers getNodePeers();
}