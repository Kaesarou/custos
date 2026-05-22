package io.custos.node.core.domain;

import io.custos.node.core.domain.model.ProtectedShare;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SecretShareDeliverySignatureChallengeTest {

    @Test
    void shouldBuildStableDeliverySignatureMessage() {
        ProtectedShare protectedShare = protectedShare();

        String message = new SecretShareDeliverySignatureChallenge(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "local-node-1",
                protectedShare,
                Instant.parse("2026-05-04T10:15:30Z")
        ).message();

        assertEquals("""
                Custos deliver secret share
                secretId: 1
                userAddress: 0x70997970c51812dc3a010c7d01b50e0d17dc79c8
                nodeId: local-node-1
                alg: X25519-HKDF-SHA256-AES-256-GCM
                ephemeralPublicKey: ephemeral-public-key
                iv: iv
                ciphertext: ciphertext
                deliveredAt: 2026-05-04T10:15:30Z""", message);
    }

    @Test
    void shouldProduceDifferentMessageWhenProtectedShareChanges() {
        ProtectedShare originalProtectedShare = protectedShare();

        ProtectedShare tamperedProtectedShare = new ProtectedShare(
                originalProtectedShare.alg(),
                originalProtectedShare.ephemeralPublicKey(),
                originalProtectedShare.iv(),
                "tampered-ciphertext"
        );

        String originalMessage = new SecretShareDeliverySignatureChallenge(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "local-node-1",
                originalProtectedShare,
                Instant.parse("2026-05-04T10:15:30Z")
        ).message();

        String tamperedMessage = new SecretShareDeliverySignatureChallenge(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "local-node-1",
                tamperedProtectedShare,
                Instant.parse("2026-05-04T10:15:30Z")
        ).message();

        assertNotEquals(originalMessage, tamperedMessage);
    }

    @Test
    void shouldProduceDifferentMessageWhenDeliveredAtChanges() {
        ProtectedShare protectedShare = protectedShare();

        String firstMessage = new SecretShareDeliverySignatureChallenge(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "local-node-1",
                protectedShare,
                Instant.parse("2026-05-04T10:15:30Z")
        ).message();

        String secondMessage = new SecretShareDeliverySignatureChallenge(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "local-node-1",
                protectedShare,
                Instant.parse("2026-05-04T10:16:30Z")
        ).message();

        assertNotEquals(firstMessage, secondMessage);
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        ProtectedShare protectedShare = protectedShare();
        Instant deliveredAt = Instant.parse("2026-05-04T10:15:30Z");

        assertThrows(NullPointerException.class, () ->
                new SecretShareDeliverySignatureChallenge(
                        null,
                        "0xUser",
                        "local-node-1",
                        protectedShare,
                        deliveredAt
                )
        );

        assertThrows(NullPointerException.class, () ->
                new SecretShareDeliverySignatureChallenge(
                        "1",
                        null,
                        "local-node-1",
                        protectedShare,
                        deliveredAt
                )
        );

        assertThrows(NullPointerException.class, () ->
                new SecretShareDeliverySignatureChallenge(
                        "1",
                        "0xUser",
                        null,
                        protectedShare,
                        deliveredAt
                )
        );

        assertThrows(NullPointerException.class, () ->
                new SecretShareDeliverySignatureChallenge(
                        "1",
                        "0xUser",
                        "local-node-1",
                        null,
                        deliveredAt
                )
        );

        assertThrows(NullPointerException.class, () ->
                new SecretShareDeliverySignatureChallenge(
                        "1",
                        "0xUser",
                        "local-node-1",
                        protectedShare,
                        null
                )
        );
    }

    private ProtectedShare protectedShare() {
        return new ProtectedShare(
                ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM,
                "ephemeral-public-key",
                "iv",
                "ciphertext"
        );
    }
}