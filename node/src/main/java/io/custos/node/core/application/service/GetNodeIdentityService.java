package io.custos.node.core.application.service;

import io.custos.node.core.application.port.in.GetNodeIdentityUseCase;
import io.custos.node.core.application.port.out.NodeIdentityProvider;
import io.custos.node.core.domain.model.NodeIdentity;

public class GetNodeIdentityService implements GetNodeIdentityUseCase {

    private final NodeIdentityProvider nodeIdentityProvider;

    public GetNodeIdentityService(NodeIdentityProvider nodeIdentityProvider) {
        this.nodeIdentityProvider = nodeIdentityProvider;
    }

    @Override
    public NodeIdentity getNodeIdentity() {
        return nodeIdentityProvider.getNodeIdentity();
    }
}