package io.custos.node.core.application.port.in.command;

import io.custos.node.core.application.exception.InvalidWalletSignatureException;
import io.custos.node.core.domain.model.AccessPolicy;

import java.util.Objects;

import static io.custos.node.core.application.exception.errorcode.WalletSignatureErrorCode.MISSING_WALLET_SIGNATURE;

public record StoreSecretShareCommand(
        String secretId,
        String encryptedShare,
        AccessPolicy accessPolicy,
        String publisherAddress,
        String publisherSignature
) {
    public StoreSecretShareCommand {
        Objects.requireNonNull(secretId, "secretId is required");
        Objects.requireNonNull(encryptedShare, "encryptedShare is required");
        Objects.requireNonNull(accessPolicy, "accessPolicy is required");
        Objects.requireNonNull(publisherAddress, "publisherAddress is required");
        Objects.requireNonNull(publisherSignature, "publisherSignature is required");

        if (secretId.isBlank()) {
            throw new IllegalArgumentException("secretId is required");
        }
        if (encryptedShare.isBlank()) {
            throw new IllegalArgumentException("encryptedShare is required");
        }
        if (publisherAddress.isBlank()) {
            throw new IllegalArgumentException("publisherAddress is required");
        }
        if (publisherSignature.isBlank()) {
            throw new InvalidWalletSignatureException(
                    MISSING_WALLET_SIGNATURE,
                    "Wallet signature is required"
            );
        }
    }
}
