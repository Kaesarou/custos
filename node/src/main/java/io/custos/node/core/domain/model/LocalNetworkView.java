package io.custos.node.core.domain.model;

import java.util.List;
import java.util.Objects;

public record LocalNetworkView(
        String observerNodeId,
        List<PeerNodeView> peers
) {
    public LocalNetworkView {
        Objects.requireNonNull(observerNodeId, "observerNodeId is required");
        Objects.requireNonNull(peers, "peers is required");

        peers = List.copyOf(peers);

        if (observerNodeId.isBlank()) {
            throw new IllegalArgumentException("observerNodeId is required");
        }
    }
}