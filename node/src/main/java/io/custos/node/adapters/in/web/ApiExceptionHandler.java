package io.custos.node.adapters.in.web;

import io.custos.node.core.application.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SecretShareNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(SecretShareNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            InvalidPublisherSignatureException.class,
            InvalidWalletSignatureException.class,
            SecretShareAccessDeniedException.class,
            WalletNonceAlreadyUsedException.class,
            EvmSignatureRecoveryException.class
    })
    public ResponseEntity<Map<String, Object>> handleForbidden(RuntimeException ex) {
        return response(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidReaderPublicKeyException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidReaderPublicKey(InvalidReaderPublicKeyException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ShareProtectionException.class)
    public ResponseEntity<Map<String, Object>> handleShareProtection(ShareProtectionException ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to protect secret share");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        return response(HttpStatus.BAD_REQUEST, "Invalid request payload");
    }

    @ExceptionHandler(SecretShareAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(SecretShareAlreadyExistsException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        ));
    }
}
