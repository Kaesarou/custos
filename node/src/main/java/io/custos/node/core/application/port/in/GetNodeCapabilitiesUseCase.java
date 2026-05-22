package io.custos.node.core.application.port.in;

import io.custos.node.core.domain.model.NodeCapabilities;

public interface GetNodeCapabilitiesUseCase {
    NodeCapabilities getNodeCapabilities();
}