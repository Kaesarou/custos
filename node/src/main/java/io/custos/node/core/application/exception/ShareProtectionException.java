package io.custos.node.core.application.exception;

import io.custos.node.core.application.exception.errorcode.ShareProtectionErrorCode;

public class ShareProtectionException extends RuntimeException {

    private final ShareProtectionErrorCode code;

    public ShareProtectionException(ShareProtectionErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ShareProtectionErrorCode code() {
        return code;
    }
}