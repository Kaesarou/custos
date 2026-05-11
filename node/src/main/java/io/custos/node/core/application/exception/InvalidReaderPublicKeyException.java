package io.custos.node.core.application.exception;

import io.custos.node.core.application.exception.errorcode.ShareProtectionErrorCode;

public class InvalidReaderPublicKeyException extends RuntimeException {

    private final ShareProtectionErrorCode code;

    public InvalidReaderPublicKeyException(ShareProtectionErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ShareProtectionErrorCode code() {
        return code;
    }
}