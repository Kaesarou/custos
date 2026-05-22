package io.custos.node.adapters.in.web.dto;

import io.custos.node.core.domain.model.NodeStatus;

public record NodeStatusResponseDto(
        String nodeId,
        String status,
        String startedAt,
        String currentTime,
        long uptimeSeconds
) {
    public static NodeStatusResponseDto fromDomain(NodeStatus status) {
        return new NodeStatusResponseDto(
                status.nodeId(),
                status.status(),
                status.startedAt().toString(),
                status.currentTime().toString(),
                status.uptimeSeconds()
        );
    }
}