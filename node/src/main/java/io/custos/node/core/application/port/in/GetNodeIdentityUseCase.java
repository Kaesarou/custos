package io.custos.node.core.application.port.in;

import io.custos.node.core.domain.model.NodeIdentity;

public interface GetNodeIdentityUseCase {
    NodeIdentity getNodeIdentity();
}