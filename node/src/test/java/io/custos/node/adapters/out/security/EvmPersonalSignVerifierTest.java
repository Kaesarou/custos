package io.custos.node.adapters.out.security;

import io.custos.node.core.application.exception.InvalidWalletSignatureException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EvmPersonalSignVerifierTest {

    private final EvmPersonalSignVerifier verifier = new EvmPersonalSignVerifier();

    @Test
    void shouldAcceptValidPersonalSignSignature() {
        verifier.verifyRetrieveSecretSignature(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "test-nonce-1234",
                "0x2eaae67211205e5ad48847d3a64251b2ec1d0b3bedee679a468127aa842aa8400c4ef9939ec8c628fbe9d240255d411532baed6e76a2c2e99d92d166cb3cfbb71c"
        );
    }

    @Test
    void shouldRejectSignatureWhenUserAddressDoesNotMatchRecoveredAddress() {
        assertThrows(InvalidWalletSignatureException.class, () ->
                verifier.verifyRetrieveSecretSignature(
                        "1",
                        "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC",
                        "test-nonce-1234",
                        "0x2eaae67211205e5ad48847d3a64251b2ec1d0b3bedee679a468127aa842aa8400c4ef9939ec8c628fbe9d240255d411532baed6e76a2c2e99d92d166cb3cfbb71c"
                )
        );
    }

    @Test
    void shouldRejectSignatureWhenNonceChanges() {
        assertThrows(InvalidWalletSignatureException.class, () ->
                verifier.verifyRetrieveSecretSignature(
                        "1",
                        "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                        "another-nonce",
                        "0x2eaae67211205e5ad48847d3a64251b2ec1d0b3bedee679a468127aa842aa8400c4ef9939ec8c628fbe9d240255d411532baed6e76a2c2e99d92d166cb3cfbb71c"
                )
        );
    }

    @Test
    void shouldRejectInvalidAddress() {
        assertThrows(InvalidWalletSignatureException.class, () ->
                verifier.verifyRetrieveSecretSignature(
                        "1",
                        "invalid-address",
                        "test-nonce-1234",
                        "0x2eaae67211205e5ad48847d3a64251b2ec1d0b3bedee679a468127aa842aa8400c4ef9939ec8c628fbe9d240255d411532baed6e76a2c2e99d92d166cb3cfbb71c"
                )
        );
    }

    @Test
    void shouldRejectBlankNonce() {
        assertThrows(InvalidWalletSignatureException.class, () ->
                verifier.verifyRetrieveSecretSignature(
                        "1",
                        "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                        " ",
                        "0x2eaae67211205e5ad48847d3a64251b2ec1d0b3bedee679a468127aa842aa8400c4ef9939ec8c628fbe9d240255d411532baed6e76a2c2e99d92d166cb3cfbb71c"
                )
        );
    }
}