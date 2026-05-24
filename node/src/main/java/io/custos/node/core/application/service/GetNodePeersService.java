package io.custos.node.core.application.service;

import io.custos.node.core.application.port.in.GetNodePeersUseCase;
import io.custos.node.core.application.port.out.NodePeerProvider;
import io.custos.node.core.domain.model.NodePeers;

public class GetNodePeersService implements GetNodePeersUseCase {

    private final NodePeerProvider nodePeerProvider;

    public GetNodePeersService(NodePeerProvider nodePeerProvider) {
        this.nodePeerProvider = nodePeerProvider;
    }

    @Override
    public NodePeers getNodePeers() {
        return nodePeerProvider.getNodePeers();
    }
}