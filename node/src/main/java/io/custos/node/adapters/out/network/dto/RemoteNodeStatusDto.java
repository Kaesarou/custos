package io.custos.node.adapters.out.network.dto;

public record RemoteNodeStatusDto(
        String nodeId,
        String status,
        String startedAt,
        String currentTime,
        long uptimeSeconds
) {
}