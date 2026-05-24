package io.custos.node.core.domain;

import java.util.Objects;

public record RetrieveSecretShareSignatureChallenge(
        String secretId,
        String userAddress,
        String readerPublicKey,
        String nonce
) {
    public RetrieveSecretShareSignatureChallenge {
        Objects.requireNonNull(secretId, "secretId is required");
        Objects.requireNonNull(userAddress, "userAddress is required");
        Objects.requireNonNull(readerPublicKey, "readerPublicKey is required");
        Objects.requireNonNull(nonce, "nonce is required");

        if (secretId.isBlank()) {
            throw new IllegalArgumentException("secretId is required");
        }
        if (userAddress.isBlank()) {
            throw new IllegalArgumentException("userAddress is required");
        }
        if (readerPublicKey.isBlank()) {
            throw new IllegalArgumentException("readerPublicKey is required");
        }
        if (nonce.isBlank()) {
            throw new IllegalArgumentException("nonce is required");
        }
    }

    public String message() {
        return """
            Custos retrieve secret share
            secretId: %s
            userAddress: %s
            readerPublicKey: %s
            nonce: %s
            """.formatted(
                secretId,
                userAddress.toLowerCase(),
                readerPublicKey,
                nonce
        ).stripTrailing();
    }
}