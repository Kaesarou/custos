package io.custos.node.adapters.out.security;

import io.custos.node.core.domain.RetrieveSecretShareSignatureChallenge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EvmPersonalSignAddressRecovererTest {
    @Test
    void shouldRecoverSignerAddressFromValidPersonalSignSignature() {
        var recoverer = new EvmPersonalSignAddressRecoverer();

        String message = new RetrieveSecretShareSignatureChallenge(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "test-nonce-1234"
        ).message();

        String recoveredAddress = recoverer.recoverAddress(
                message,
                "0x2eaae67211205e5ad48847d3a64251b2ec1d0b3bedee679a468127aa842aa8400c4ef9939ec8c628fbe9d240255d411532baed6e76a2c2e99d92d166cb3cfbb71c"
        );

        Assertions.assertEquals(
                "0x70997970c51812dc3a010c7d01b50e0d17dc79c8",
                recoveredAddress.toLowerCase()
        );
    }
}