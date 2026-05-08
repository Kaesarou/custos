package io.custos.node.core.domain;

public record RetrieveSecretShareSignatureChallenge(
        String secretId,
        String userAddress,
        String nonce
) {
    public String message() {
        return """
            Custos retrieve secret share
            secretId: %s
            userAddress: %s
            nonce: %s
            """.formatted(secretId, userAddress.toLowerCase(), nonce).stripTrailing();
    }
}