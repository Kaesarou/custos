package io.custos.node.core.domain;

import java.util.Objects;

public record StoreSecretShareSignatureChallenge(
        String secretId,
        String publisherAddress,
        String encryptedShareHash,
        String policyHash
) {
    public StoreSecretShareSignatureChallenge {
        Objects.requireNonNull(secretId, "secretId is required");
        Objects.requireNonNull(publisherAddress, "publisherAddress is required");
        Objects.requireNonNull(encryptedShareHash, "encryptedShareHash is required");
        Objects.requireNonNull(policyHash, "policyHash is required");

        if (secretId.isBlank()) {
            throw new IllegalArgumentException("secretId is required");
        }
        if (publisherAddress.isBlank()) {
            throw new IllegalArgumentException("publisherAddress is required");
        }
        if (encryptedShareHash.isBlank()) {
            throw new IllegalArgumentException("encryptedShareHash is required");
        }
        if (policyHash.isBlank()) {
            throw new IllegalArgumentException("policyHash is required");
        }
    }

    public String message() {
        return """
                Custos store secret share
                secretId: %s
                publisherAddress: %s
                encryptedShareHash: %s
                policyHash: %s
                """.formatted(
                        secretId,
                        publisherAddress.toLowerCase(),
                        encryptedShareHash,
                        policyHash
                )
                .stripTrailing();
    }
}