package io.custos.node.core.application.port.out;

import io.custos.node.core.domain.model.NodeIdentity;

public interface NodeIdentityProvider {
    NodeIdentity getNodeIdentity();
}