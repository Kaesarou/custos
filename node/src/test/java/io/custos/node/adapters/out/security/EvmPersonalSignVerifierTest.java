package io.custos.node.adapters.out.security;

import io.custos.node.core.application.exception.InvalidWalletSignatureException;
import io.custos.node.core.application.port.in.command.RetrieveSecretShareCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EvmPersonalSignVerifierTest {

    private final EvmPersonalSignVerifier verifier = new EvmPersonalSignVerifier(new EvmPersonalSignAddressRecoverer());

    @Test
    void shouldAcceptValidPersonalSignSignature() {
        verifier.verifyRetrieveSecretSignature(
                new RetrieveSecretShareCommand(
                        "1",
                        "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                        "0x22965675a0fc18c4f9b7ac04b6d4621ab690be18c00d85018162a0d36a0a0fd849b0a56467e8cdb6bbb178ae227458e25a3aeb3e52a0fffe277b142931f968211c",
                        "y5VMaQ_llLbDlKwKwV0au2VWPiijb125n_fvOSoS61o",
                        "test-nonce-1234"
                )
        );
    }

    @Test
    void shouldRejectSignatureWhenUserAddressDoesNotMatchRecoveredAddress() {
        assertThrows(InvalidWalletSignatureException.class, () ->
                verifier.verifyRetrieveSecretSignature(
                        new RetrieveSecretShareCommand(
                                "1",
                                "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC",
                                "0x22965675a0fc18c4f9b7ac04b6d4621ab690be18c00d85018162a0d36a0a0fd849b0a56467e8cdb6bbb178ae227458e25a3aeb3e52a0fffe277b142931f968211c",
                                "y5VMaQ_llLbDlKwKwV0au2VWPiijb125n_fvOSoS61o",
                                "test-nonce-1234"
                        )
                )
        );
    }

    @Test
    void shouldRejectSignatureWhenNonceChanges() {
        assertThrows(InvalidWalletSignatureException.class, () ->
                verifier.verifyRetrieveSecretSignature(
                        new RetrieveSecretShareCommand(
                                "1",
                                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                                "0x22965675a0fc18c4f9b7ac04b6d4621ab690be18c00d85018162a0d36a0a0fd849b0a56467e8cdb6bbb178ae227458e25a3aeb3e52a0fffe277b142931f968211c",
                                "y5VMaQ_llLbDlKwKwV0au2VWPiijb125n_fvOSoS61o",
                                "another-nonce"
                        ))
        );
    }

    @Test
    void shouldRejectInvalidAddress() {
        assertThrows(InvalidWalletSignatureException.class, () ->
                verifier.verifyRetrieveSecretSignature(
                        new RetrieveSecretShareCommand(
                                "1",
                                "invalid-address",
                                "0x22965675a0fc18c4f9b7ac04b6d4621ab690be18c00d85018162a0d36a0a0fd849b0a56467e8cdb6bbb178ae227458e25a3aeb3e52a0fffe277b142931f968211c",
                                "y5VMaQ_llLbDlKwKwV0au2VWPiijb125n_fvOSoS61o",
                                "test-nonce-1234"
                        ))
        );
    }

    @Test
    void shouldRejectBlankNonce() {
        assertThrows(InvalidWalletSignatureException.class, () ->
                verifier.verifyRetrieveSecretSignature(
                        new RetrieveSecretShareCommand(
                                "1",
                                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                                "0x22965675a0fc18c4f9b7ac04b6d4621ab690be18c00d85018162a0d36a0a0fd849b0a56467e8cdb6bbb178ae227458e25a3aeb3e52a0fffe277b142931f968211c",
                                "y5VMaQ_llLbDlKwKwV0au2VWPiijb125n_fvOSoS61o",
                                " "
                        ))
        );
    }
}