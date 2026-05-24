package io.custos.node.core.application.port.out;

import io.custos.node.core.domain.model.NodeCapabilities;

public interface NodeCapabilitiesProvider {
    NodeCapabilities getNodeCapabilities();
}