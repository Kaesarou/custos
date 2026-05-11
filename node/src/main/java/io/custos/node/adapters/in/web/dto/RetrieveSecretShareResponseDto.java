package io.custos.node.adapters.in.web.dto;

public record RetrieveSecretShareResponseDto(
        String secretId,
        String nodeId,
        ProtectedShareDto protectedShare,
        String nodeSignature,
        String deliveredAt
) {
}
