package io.custos.node.core.domain.model;

import java.util.List;
import java.util.Objects;

public record NodeCapabilities(
        String nodeId,
        List<PolicyType> supportedPolicyTypes,
        List<String> supportedShareProtectionAlgorithms,
        String signatureAlgorithm,
        List<SupportedChain> supportedChains
) {
    public NodeCapabilities {
        Objects.requireNonNull(nodeId, "nodeId is required");
        Objects.requireNonNull(supportedPolicyTypes, "supportedPolicyTypes is required");
        Objects.requireNonNull(supportedShareProtectionAlgorithms, "supportedShareProtectionAlgorithms is required");
        Objects.requireNonNull(signatureAlgorithm, "signatureAlgorithm is required");
        Objects.requireNonNull(supportedChains, "supportedChains is required");

        supportedPolicyTypes = List.copyOf(supportedPolicyTypes);
        supportedShareProtectionAlgorithms = List.copyOf(supportedShareProtectionAlgorithms);
        supportedChains = List.copyOf(supportedChains);

        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId is required");
        }
        if (signatureAlgorithm.isBlank()) {
            throw new IllegalArgumentException("signatureAlgorithm is required");
        }
    }

    public record SupportedChain(
            long chainId
    ) {
    }
}