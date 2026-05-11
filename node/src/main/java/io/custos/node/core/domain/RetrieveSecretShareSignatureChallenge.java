package io.custos.node.core.domain;

public record RetrieveSecretShareSignatureChallenge(
        String secretId,
        String userAddress,
        String readerPublicKey,
        String nonce
) {
    public String message() {
        return """
            Custos retrieve secret share
            secretId: %s
            userAddress: %s
            readerPublicKey: %s
            nonce: %s
            """.formatted(
                secretId,
                userAddress.toLowerCase(),
                readerPublicKey,
                nonce
        ).stripTrailing();
    }
}