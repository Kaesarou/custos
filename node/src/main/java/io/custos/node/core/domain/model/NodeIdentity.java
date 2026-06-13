package io.custos.node.core.domain.model;

import java.util.Objects;

public record NodeIdentity(
        String nodeId,
        String nodeAddress,
        String rewardAddress,
        String publicBaseUrl,
        String signatureAlgorithm
) {
    public NodeIdentity {
        Objects.requireNonNull(nodeId, "nodeId is required");
        Objects.requireNonNull(nodeAddress, "nodeAddress is required");
        Objects.requireNonNull(rewardAddress, "rewardAddress is required");
        Objects.requireNonNull(publicBaseUrl, "publicBaseUrl is required");
        Objects.requireNonNull(signatureAlgorithm, "signatureAlgorithm is required");

        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId is required");
        }
        if (nodeAddress.isBlank()) {
            throw new IllegalArgumentException("nodeAddress is required");
        }
        if (rewardAddress.isBlank()) {
            throw new IllegalArgumentException("rewardAddress is required");
        }
        if (publicBaseUrl.isBlank()) {
            throw new IllegalArgumentException("publicBaseUrl is required");
        }
        if (signatureAlgorithm.isBlank()) {
            throw new IllegalArgumentException("signatureAlgorithm is required");
        }
    }
}