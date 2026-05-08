package io.custos.node.core.domain;

public record StoreSecretShareSignatureChallenge(
        String secretId,
        String publisherAddress,
        String encryptedShareHash,
        String policyHash
) {
    public String message() {
        return """
                Custos store secret share
                secretId: %s
                publisherAddress: %s
                encryptedShareHash: %s
                policyHash: %s
                """.formatted(secretId,
                        publisherAddress.toLowerCase(),
                        encryptedShareHash,
                        policyHash)
                .stripTrailing();
    }
}
