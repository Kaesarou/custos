package io.custos.node.adapters.out.security;

import io.custos.node.core.application.exception.InvalidReaderPublicKeyException;
import io.custos.node.core.application.exception.ShareProtectionException;
import io.custos.node.core.application.port.out.ShareProtectionService;
import io.custos.node.core.domain.model.ProtectedShare;
import io.custos.node.core.domain.model.ShareProtectionAlgorithm;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.XECPublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import static io.custos.node.core.application.exception.errorcode.ShareProtectionErrorCode.INVALID_READER_PUBLIC_KEY;
import static io.custos.node.core.application.exception.errorcode.ShareProtectionErrorCode.SHARE_PROTECTION_FAILED;

@Service
public class X25519AesGcmShareProtectionService implements ShareProtectionService {

    private static final int RAW_X25519_PUBLIC_KEY_LENGTH_BYTES = 32;
    private static final int AES_256_KEY_LENGTH_BYTES = 32;
    private static final int AES_GCM_IV_LENGTH_BYTES = 12;
    private static final int AES_GCM_TAG_LENGTH_BITS = 128;

    private static final String X25519 = "X25519";
    private static final String AES = "AES";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";

    private static final byte[] HKDF_SALT = "Custos share protection v1".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HKDF_INFO = "X25519-HKDF-SHA256-AES-256-GCM".getBytes(StandardCharsets.UTF_8);

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public ProtectedShare protect(String encryptedShare, String readerPublicKey) {
        try {
            byte[] readerPublicKeyRaw = decodeReaderPublicKey(readerPublicKey);

            var keyPairGenerator = KeyPairGenerator.getInstance(X25519);
            keyPairGenerator.initialize(new NamedParameterSpec(X25519));
            var ephemeralKeyPair = keyPairGenerator.generateKeyPair();

            byte[] sharedSecret = computeSharedSecret(
                    ephemeralKeyPair.getPrivate(),
                    readerPublicKeyRaw
            );

            byte[] aesKeyBytes = deriveAesKey(sharedSecret);
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES);

            byte[] iv = randomBytes(AES_GCM_IV_LENGTH_BYTES);

            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    aesKey,
                    new GCMParameterSpec(AES_GCM_TAG_LENGTH_BITS, iv)
            );

            byte[] ciphertext = cipher.doFinal(encryptedShare.getBytes(StandardCharsets.UTF_8));

            byte[] ephemeralPublicKeyRaw = rawX25519PublicKey(ephemeralKeyPair.getPublic());

            return new ProtectedShare(
                    ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM,
                    Base64Url.encodeNoPadding(ephemeralPublicKeyRaw),
                    Base64Url.encodeNoPadding(iv),
                    Base64Url.encodeNoPadding(ciphertext)
            );

        } catch (InvalidReaderPublicKeyException e) {
            throw e;
        } catch (Exception e) {
            throw new ShareProtectionException(
                    SHARE_PROTECTION_FAILED,
                    "Unable to protect secret share",
                    e
            );
        }
    }

    private byte[] decodeReaderPublicKey(String readerPublicKey) {
        try {
            byte[] decoded = Base64Url.decodeNoPadding(readerPublicKey);

            if (decoded.length != RAW_X25519_PUBLIC_KEY_LENGTH_BYTES) {
                throw new InvalidReaderPublicKeyException(
                        INVALID_READER_PUBLIC_KEY,
                        "readerPublicKey must be a base64url encoded 32-byte X25519 public key"
                );
            }

            return decoded;
        } catch (InvalidReaderPublicKeyException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidReaderPublicKeyException(
                    INVALID_READER_PUBLIC_KEY,
                    "Invalid readerPublicKey"
            );
        }
    }

    private byte[] computeSharedSecret(
            java.security.PrivateKey ephemeralPrivateKey,
            byte[] readerPublicKeyRaw
    ) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(X25519);

        /*
         * X25519 public keys need an X.509 SubjectPublicKeyInfo wrapper for Java KeyFactory.
         * The raw key is 32 bytes. This prefix is the DER header for X25519 public keys.
         */
        byte[] x509EncodedReaderPublicKey = X25519KeyEncoding.x509PublicKeyFromRaw(readerPublicKeyRaw);

        var readerPublicKey = keyFactory.generatePublic(
                new X509EncodedKeySpec(x509EncodedReaderPublicKey)
        );

        KeyAgreement keyAgreement = KeyAgreement.getInstance(X25519);
        keyAgreement.init(ephemeralPrivateKey);
        keyAgreement.doPhase(readerPublicKey, true);

        return keyAgreement.generateSecret();
    }

    private byte[] deriveAesKey(byte[] sharedSecret) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(new HKDFParameters(sharedSecret, HKDF_SALT, HKDF_INFO));

        byte[] aesKey = new byte[AES_256_KEY_LENGTH_BYTES];
        hkdf.generateBytes(aesKey, 0, aesKey.length);

        return aesKey;
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private byte[] rawX25519PublicKey(java.security.PublicKey publicKey) {
        if (publicKey instanceof XECPublicKey xecPublicKey) {
            byte[] raw = xecPublicKey.getU().toByteArray();

            /*
             * BigInteger.toByteArray() may add a sign byte or return fewer bytes.
             * X25519 raw public keys must be exactly 32 bytes little-endian-ish encoded by provider.
             *
             * If this causes trouble with your provider, use encoded X.509 form instead
             * for ephemeralPublicKey. For now, we normalize to 32 bytes.
             */
            return X25519KeyEncoding.normalizeRawPublicKey(raw);
        }

        throw new IllegalStateException("Unsupported X25519 public key type");
    }
}