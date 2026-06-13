package io.custos.node.adapters.out.network.dto;

public record RemoteNodeIdentityDto(
        String nodeId,
        String nodeAddress,
        String rewardAddress,
        String publicBaseUrl,
        String signatureAlgorithm
) {
}