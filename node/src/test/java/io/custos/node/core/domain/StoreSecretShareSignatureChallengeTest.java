package io.custos.node.core.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoreSecretShareSignatureChallengeTest {

    @Test
    void shouldBuildStableStoreSecretShareMessageWithLowercasePublisherAddress() {
        String message = new StoreSecretShareSignatureChallenge(
                "1",
                "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC",
                "0xencryptedsharehash",
                "0xpolicyhash"
        ).message();

        assertEquals("""
                Custos store secret share
                secretId: 1
                publisherAddress: 0x3c44cdddb6a900fa2b585dd299e03d12fa4293bc
                encryptedShareHash: 0xencryptedsharehash
                policyHash: 0xpolicyhash""", message);
    }
}