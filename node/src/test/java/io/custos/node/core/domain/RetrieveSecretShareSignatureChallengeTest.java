package io.custos.node.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetrieveSecretShareSignatureChallengeTest {

    @Test
    void shouldBuildStableRetrieveSecretShareMessage() {
        String message = new RetrieveSecretShareSignatureChallenge(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "test-nonce-1234"
        ).message();

        assertEquals("""
                Custos retrieve secret share
                secretId: 1
                userAddress: 0x70997970C51812dc3A010C7d01b50e0d17dc79C8
                nonce: test-nonce-1234""", message);
    }
}