package io.custos.node.core.domain;

import io.custos.node.core.domain.model.ProtectedShare;

import java.time.Instant;
import java.util.Objects;

public record SecretShareDeliverySignatureChallenge(
        String secretId,
        String userAddress,
        String nodeId,
        ProtectedShare protectedShare,
        Instant deliveredAt
) {
    public SecretShareDeliverySignatureChallenge {
        Objects.requireNonNull(secretId, "secretId is required");
        Objects.requireNonNull(userAddress, "userAddress is required");
        Objects.requireNonNull(nodeId, "nodeId is required");
        Objects.requireNonNull(protectedShare, "protectedShare is required");
        Objects.requireNonNull(deliveredAt, "deliveredAt is required");

        if (secretId.isBlank()) {
            throw new IllegalArgumentException("secretId is required");
        }
        if (userAddress.isBlank()) {
            throw new IllegalArgumentException("userAddress is required");
        }
        if (nodeId.isBlank()) {
            throw new IllegalArgumentException("nodeId is required");
        }
    }

    public String message() {
        return """
            Custos deliver secret share
            secretId: %s
            userAddress: %s
            nodeId: %s
            alg: %s
            ephemeralPublicKey: %s
            iv: %s
            ciphertext: %s
            deliveredAt: %s
            """.formatted(
                secretId,
                userAddress.toLowerCase(),
                nodeId,
                protectedShare.alg(),
                protectedShare.ephemeralPublicKey(),
                protectedShare.iv(),
                protectedShare.ciphertext(),
                deliveredAt
        ).stripTrailing();
    }
}