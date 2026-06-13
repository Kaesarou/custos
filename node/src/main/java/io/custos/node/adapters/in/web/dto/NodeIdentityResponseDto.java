package io.custos.node.adapters.in.web.dto;

import io.custos.node.core.domain.model.NodeIdentity;

public record NodeIdentityResponseDto(
        String nodeId,
        String nodeAddress,
        String rewardAddress,
        String publicBaseUrl,
        String signatureAlgorithm
) {
    public static NodeIdentityResponseDto fromDomain(NodeIdentity identity) {
        return new NodeIdentityResponseDto(
                identity.nodeId(),
                identity.nodeAddress(),
                identity.rewardAddress(),
                identity.publicBaseUrl(),
                identity.signatureAlgorithm()
        );
    }
}