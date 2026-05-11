package io.custos.node.adapters.out.security;

import io.custos.node.core.domain.model.ProtectedShare;
import io.custos.node.core.domain.model.ShareProtectionAlgorithm;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class X25519AesGcmShareProtectionServiceTest {

    @Test
    void shouldReturnStructuredProtectedShare() throws Exception {
        var service = new X25519AesGcmShareProtectionService();

        String readerPublicKey = generateReaderPublicKeyForTest();

        ProtectedShare result = service.protect("encrypted-share", readerPublicKey);

        assertEquals(ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM, result.alg());
        assertNotNull(result.ephemeralPublicKey());
        assertNotNull(result.iv());
        assertNotNull(result.ciphertext());
    }

    private String generateReaderPublicKeyForTest() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("X25519");
        keyPairGenerator.initialize(new NamedParameterSpec("X25519"));

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        byte[] x509EncodedPublicKey = keyPair.getPublic().getEncoded();

        byte[] rawPublicKey = Arrays.copyOfRange(
                x509EncodedPublicKey,
                x509EncodedPublicKey.length - 32,
                x509EncodedPublicKey.length
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawPublicKey);
    }
}
