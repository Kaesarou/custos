package io.custos.node.core.domain.model;

import java.util.Objects;

public record AccessPolicy(
        PolicyType type,
        long chainId,
        String contractAddress,
        String policyData
) {
    public AccessPolicy {

        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(contractAddress, "contractAddress is required");
        Objects.requireNonNull(policyData, "policyData is required");

        if (chainId <= 0) {
            throw new IllegalArgumentException("chainId must be positive");
        }
        if (contractAddress.isBlank()) {
            throw new IllegalArgumentException("contractAddress is required");
        }
        if (policyData.isBlank()) {
            throw new IllegalArgumentException("policyData is required");
        }
    }

    public String getCanonical() {
        return String.join("|", type.name(), String.valueOf(chainId), contractAddress, policyData);
    }
}
