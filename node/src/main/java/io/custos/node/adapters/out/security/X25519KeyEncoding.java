package io.custos.node.adapters.out.security;

import java.util.Arrays;

final class X25519KeyEncoding {

    /*
     * DER SubjectPublicKeyInfo prefix for X25519 public key.
     * Raw public key bytes are appended after this prefix.
     */
    private static final byte[] X25519_X509_PUBLIC_KEY_PREFIX = new byte[]{
            0x30, 0x2a,
            0x30, 0x05,
            0x06, 0x03,
            0x2b, 0x65, 0x6e,
            0x03, 0x21,
            0x00
    };

    private static final int RAW_X25519_PUBLIC_KEY_LENGTH_BYTES = 32;

    private X25519KeyEncoding() {
    }

    static byte[] x509PublicKeyFromRaw(byte[] rawPublicKey) {
        if (rawPublicKey.length != RAW_X25519_PUBLIC_KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("X25519 raw public key must be 32 bytes");
        }

        byte[] encoded = Arrays.copyOf(
                X25519_X509_PUBLIC_KEY_PREFIX,
                X25519_X509_PUBLIC_KEY_PREFIX.length + rawPublicKey.length
        );

        System.arraycopy(
                rawPublicKey,
                0,
                encoded,
                X25519_X509_PUBLIC_KEY_PREFIX.length,
                rawPublicKey.length
        );

        return encoded;
    }
}