package io.custos.node.core.domain.model;

import java.util.List;
import java.util.Objects;

public record NodePeers(
        String nodeId,
        List<Peer> peers
) {
    public NodePeers {
        Objects.requireNonNull(nodeId, "nodeId is required");
        Objects.requireNonNull(peers, "peers is required");

        peers = List.copyOf(peers);

        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId is required");
        }
    }

    public record Peer(
            String baseUrl
    ) {
        public Peer {
            Objects.requireNonNull(baseUrl, "baseUrl is required");

            if (baseUrl.isBlank()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
        }
    }
}