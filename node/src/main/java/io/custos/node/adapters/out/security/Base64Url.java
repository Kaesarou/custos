package io.custos.node.adapters.out.security;

import java.util.Base64;
import java.util.regex.Pattern;

final class Base64Url {

    private static final Pattern BASE64URL_NO_PADDING_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]+$");

    private Base64Url() {
    }

    static String encodeNoPadding(byte[] value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value);
    }

    static byte[] decodeNoPadding(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Base64url value is required");
        }

        if (!BASE64URL_NO_PADDING_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid base64url value");
        }

        return Base64.getUrlDecoder().decode(value);
    }
}