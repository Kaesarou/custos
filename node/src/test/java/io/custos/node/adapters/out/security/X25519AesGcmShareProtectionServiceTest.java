package io.custos.node.adapters.out.security;

import io.custos.node.core.application.exception.InvalidReaderPublicKeyException;
import io.custos.node.core.domain.ShareProtectionAlgorithm;
import io.custos.node.core.domain.model.ProtectedShare;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.junit.jupiter.api.Test;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class X25519AesGcmShareProtectionServiceTest {

    private static final String X25519 = "X25519";
    private static final String AES = "AES";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int RAW_X25519_PUBLIC_KEY_LENGTH_BYTES = 32;
    private static final int AES_256_KEY_LENGTH_BYTES = 32;
    private static final int AES_GCM_TAG_LENGTH_BITS = 128;

    private static final byte[] HKDF_SALT =
            "Custos share protection v1".getBytes(StandardCharsets.UTF_8);

    private static final byte[] HKDF_INFO =
            "X25519-HKDF-SHA256-AES-256-GCM".getBytes(StandardCharsets.UTF_8);

    private final X25519AesGcmShareProtectionService service =
            new X25519AesGcmShareProtectionService();

    @Test
    void shouldReturnStructuredProtectedShare() throws Exception {
        ReaderKeyPairForTest readerKeyPair = generateReaderKeyPairForTest();

        ProtectedShare result = service.protect(
                "encrypted-share",
                readerKeyPair.publicKeyBase64Url()
        );

        assertEquals(
                ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM,
                result.alg()
        );
        assertNotNull(result.ephemeralPublicKey());
        assertNotNull(result.iv());
        assertNotNull(result.ciphertext());

        assertFalse(result.ephemeralPublicKey().isBlank());
        assertFalse(result.iv().isBlank());
        assertFalse(result.ciphertext().isBlank());
    }

    @Test
    void shouldEncryptAndDecryptShareWithMatchingReaderPrivateKey() throws Exception {
        ReaderKeyPairForTest readerKeyPair = generateReaderKeyPairForTest();

        ProtectedShare protectedShare = service.protect(
                "encrypted-share",
                readerKeyPair.publicKeyBase64Url()
        );

        String decryptedShare = decrypt(
                protectedShare,
                readerKeyPair.privateKey()
        );

        assertEquals("encrypted-share", decryptedShare);
    }

    @Test
    void shouldFailToDecryptWithAnotherReaderPrivateKey() throws Exception {
        ReaderKeyPairForTest legitimateReaderKeyPair = generateReaderKeyPairForTest();
        ReaderKeyPairForTest anotherReaderKeyPair = generateReaderKeyPairForTest();

        ProtectedShare protectedShare = service.protect(
                "encrypted-share",
                legitimateReaderKeyPair.publicKeyBase64Url()
        );

        assertThrows(
                AEADBadTagException.class,
                () -> decrypt(protectedShare, anotherReaderKeyPair.privateKey())
        );
    }

    @Test
    void shouldProduceDifferentProtectedSharesForSameInput() throws Exception {
        ReaderKeyPairForTest readerKeyPair = generateReaderKeyPairForTest();

        ProtectedShare first = service.protect(
                "encrypted-share",
                readerKeyPair.publicKeyBase64Url()
        );

        ProtectedShare second = service.protect(
                "encrypted-share",
                readerKeyPair.publicKeyBase64Url()
        );

        assertEquals(first.alg(), second.alg());

        assertNotEquals(first.ephemeralPublicKey(), second.ephemeralPublicKey());
        assertNotEquals(first.iv(), second.iv());
        assertNotEquals(first.ciphertext(), second.ciphertext());
    }

    @Test
    void shouldRejectBlankReaderPublicKey() {
        assertThrows(
                InvalidReaderPublicKeyException.class,
                () -> service.protect("encrypted-share", " ")
        );
    }

    @Test
    void shouldRejectNonBase64UrlReaderPublicKey() {
        assertThrows(
                InvalidReaderPublicKeyException.class,
                () -> service.protect("encrypted-share", "not+base64/url==")
        );
    }

    @Test
    void shouldRejectReaderPublicKeyWithInvalidLength() {
        String invalidReaderPublicKey = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(new byte[31]);

        assertThrows(
                InvalidReaderPublicKeyException.class,
                () -> service.protect("encrypted-share", invalidReaderPublicKey)
        );
    }

    @Test
    void shouldFailToDecryptWhenCiphertextIsTampered() throws Exception {
        ReaderKeyPairForTest readerKeyPair = generateReaderKeyPairForTest();

        ProtectedShare protectedShare = service.protect(
                "encrypted-share",
                readerKeyPair.publicKeyBase64Url()
        );

        byte[] ciphertext = Base64.getUrlDecoder().decode(protectedShare.ciphertext());
        ciphertext[0] = (byte) (ciphertext[0] ^ 1);

        ProtectedShare tamperedProtectedShare = new ProtectedShare(
                protectedShare.alg(),
                protectedShare.ephemeralPublicKey(),
                protectedShare.iv(),
                Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext)
        );

        assertThrows(
                AEADBadTagException.class,
                () -> decrypt(tamperedProtectedShare, readerKeyPair.privateKey())
        );
    }

    @Test
    void shouldReturnExpectedEncodedFieldSizes() throws Exception {
        ReaderKeyPairForTest readerKeyPair = generateReaderKeyPairForTest();

        ProtectedShare protectedShare = service.protect(
                "encrypted-share",
                readerKeyPair.publicKeyBase64Url()
        );

        assertEquals(32, Base64.getUrlDecoder().decode(protectedShare.ephemeralPublicKey()).length);
        assertEquals(12, Base64.getUrlDecoder().decode(protectedShare.iv()).length);

        byte[] ciphertext = Base64.getUrlDecoder().decode(protectedShare.ciphertext());

        assertTrue(ciphertext.length > "encrypted-share".getBytes(StandardCharsets.UTF_8).length);
    }

    private String decrypt(
            ProtectedShare protectedShare,
            PrivateKey readerPrivateKey
    ) throws Exception {
        byte[] ephemeralPublicKeyRaw = Base64.getUrlDecoder()
                .decode(protectedShare.ephemeralPublicKey());

        byte[] ephemeralPublicKeyX509 =
                X25519KeyEncoding.x509PublicKeyFromRaw(ephemeralPublicKeyRaw);

        var ephemeralPublicKey = KeyFactory.getInstance(X25519)
                .generatePublic(new X509EncodedKeySpec(ephemeralPublicKeyX509));

        KeyAgreement keyAgreement = KeyAgreement.getInstance(X25519);
        keyAgreement.init(readerPrivateKey);
        keyAgreement.doPhase(ephemeralPublicKey, true);

        byte[] sharedSecret = keyAgreement.generateSecret();

        byte[] aesKeyBytes = deriveAesKey(sharedSecret);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES);

        byte[] iv = Base64.getUrlDecoder().decode(protectedShare.iv());
        byte[] ciphertext = Base64.getUrlDecoder().decode(protectedShare.ciphertext());

        Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
        cipher.init(
                Cipher.DECRYPT_MODE,
                aesKey,
                new GCMParameterSpec(AES_GCM_TAG_LENGTH_BITS, iv)
        );

        byte[] plaintext = cipher.doFinal(ciphertext);

        return new String(plaintext, StandardCharsets.UTF_8);
    }

    private byte[] deriveAesKey(byte[] sharedSecret) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(sharedSecret, HKDF_SALT, HKDF_INFO));

        byte[] aesKey = new byte[AES_256_KEY_LENGTH_BYTES];
        hkdf.generateBytes(aesKey, 0, aesKey.length);

        return aesKey;
    }

    private ReaderKeyPairForTest generateReaderKeyPairForTest() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(X25519);
        keyPairGenerator.initialize(new NamedParameterSpec(X25519));

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        byte[] x509EncodedPublicKey = keyPair.getPublic().getEncoded();

        byte[] rawPublicKey = Arrays.copyOfRange(
                x509EncodedPublicKey,
                x509EncodedPublicKey.length - RAW_X25519_PUBLIC_KEY_LENGTH_BYTES,
                x509EncodedPublicKey.length
        );

        String publicKeyBase64Url = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawPublicKey);

        return new ReaderKeyPairForTest(
                keyPair.getPrivate(),
                publicKeyBase64Url
        );
    }

    private record ReaderKeyPairForTest(
            PrivateKey privateKey,
            String publicKeyBase64Url
    ) {
    }
}