package io.custos.node.adapters.in.web.dto;

import io.custos.node.core.domain.model.ProtectedShare;

public record ProtectedShareDto(
        String alg,
        String ephemeralPublicKey,
        String iv,
        String ciphertext
) {
    public static ProtectedShareDto fromDomain(ProtectedShare protectedShare) {
        return new ProtectedShareDto(
                protectedShare.alg(),
                protectedShare.ephemeralPublicKey(),
                protectedShare.iv(),
                protectedShare.ciphertext()
        );
    }
}