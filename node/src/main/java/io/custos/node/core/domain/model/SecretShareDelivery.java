package io.custos.node.core.domain.model;

import java.time.Instant;

public record SecretShareDelivery(
        String secretId,
        String nodeId,
        ProtectedShare protectedShare,
        String nodeSignature,
        Instant deliveredAt
) {
}
