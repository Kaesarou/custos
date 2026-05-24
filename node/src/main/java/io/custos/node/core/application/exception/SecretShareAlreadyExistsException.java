package io.custos.node.core.application.exception;

public class SecretShareAlreadyExistsException extends RuntimeException {

    public SecretShareAlreadyExistsException(String secretId) {
        super("Secret share already exists: " + secretId);
    }
}