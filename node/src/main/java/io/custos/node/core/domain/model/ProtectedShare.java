package io.custos.node.core.domain.model;

import java.util.Objects;

public record ProtectedShare(
        String alg,
        String ephemeralPublicKey,
        String iv,
        String ciphertext
) {
    public ProtectedShare {
        Objects.requireNonNull(alg, "alg is required");
        Objects.requireNonNull(ephemeralPublicKey, "ephemeralPublicKey is required");
        Objects.requireNonNull(iv, "iv is required");
        Objects.requireNonNull(ciphertext, "ciphertext is required");

        if (alg.isBlank()) {
            throw new IllegalArgumentException("alg is required");
        }
        if (ephemeralPublicKey.isBlank()) {
            throw new IllegalArgumentException("ephemeralPublicKey is required");
        }
        if (iv.isBlank()) {
            throw new IllegalArgumentException("iv is required");
        }
        if (ciphertext.isBlank()) {
            throw new IllegalArgumentException("ciphertext is required");
        }
    }
}