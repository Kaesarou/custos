package io.custos.node.core.application.exception;

import io.custos.node.core.application.exception.errorcode.WalletSignatureErrorCode;

public class InvalidPublisherSignatureException extends RuntimeException {

    private final WalletSignatureErrorCode code;

    public InvalidPublisherSignatureException(WalletSignatureErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public WalletSignatureErrorCode getCode() {
        return code;
    }
}
