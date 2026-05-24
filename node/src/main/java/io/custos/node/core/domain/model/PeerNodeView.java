package io.custos.node.core.domain.model;

import java.util.Objects;

public record PeerNodeView(
        String baseUrl,
        boolean reachable,
        NodeIdentity identity,
        NodeStatus status,
        NodeCapabilities capabilities,
        String failureReason
) {
    public PeerNodeView {
        Objects.requireNonNull(baseUrl, "baseUrl is required");

        if (baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl is required");
        }

        if (reachable && identity == null) {
            throw new IllegalArgumentException("identity is required when peer is reachable");
        }

        if (reachable && status == null) {
            throw new IllegalArgumentException("status is required when peer is reachable");
        }

        if (reachable && capabilities == null) {
            throw new IllegalArgumentException("capabilities is required when peer is reachable");
        }

        if (!reachable && (failureReason == null || failureReason.isBlank())) {
            throw new IllegalArgumentException("failureReason is required when peer is unreachable");
        }
    }

    public static PeerNodeView reachable(
            String baseUrl,
            NodeIdentity identity,
            NodeStatus status,
            NodeCapabilities capabilities
    ) {
        return new PeerNodeView(
                baseUrl,
                true,
                identity,
                status,
                capabilities,
                null
        );
    }

    public static PeerNodeView unreachable(
            String baseUrl,
            String failureReason
    ) {
        return new PeerNodeView(
                baseUrl,
                false,
                null,
                null,
                null,
                failureReason
        );
    }
}