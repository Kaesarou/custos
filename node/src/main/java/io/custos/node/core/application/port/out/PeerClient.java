package io.custos.node.core.application.port.out;

import io.custos.node.core.domain.model.PeerNodeView;

public interface PeerClient {
    PeerNodeView inspectPeer(String baseUrl);
}