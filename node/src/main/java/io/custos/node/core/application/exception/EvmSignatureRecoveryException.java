package io.custos.node.core.application.exception;

public class EvmSignatureRecoveryException extends RuntimeException {

    public EvmSignatureRecoveryException(String message) {
        super(message);
    }

    public EvmSignatureRecoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}