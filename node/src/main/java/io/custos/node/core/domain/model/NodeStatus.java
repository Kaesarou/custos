package io.custos.node.core.domain.model;

import java.time.Instant;
import java.util.Objects;

public record NodeStatus(
        String nodeId,
        String status,
        Instant startedAt,
        Instant currentTime,
        long uptimeSeconds
) {
    public NodeStatus {
        Objects.requireNonNull(nodeId, "nodeId is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(startedAt, "startedAt is required");
        Objects.requireNonNull(currentTime, "currentTime is required");

        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId is required");
        }
        if (status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        if (uptimeSeconds < 0) {
            throw new IllegalArgumentException("uptimeSeconds must be positive");
        }
    }
}