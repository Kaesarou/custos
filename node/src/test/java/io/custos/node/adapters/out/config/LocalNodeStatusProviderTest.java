package io.custos.node.adapters.out.config;

import io.custos.node.core.application.port.out.NodeIdentityProvider;
import io.custos.node.core.domain.model.NodeIdentity;
import io.custos.node.core.domain.model.NodeStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalNodeStatusProviderTest {

    @Test
    void shouldReturnLocalNodeStatus() {
        NodeIdentityProvider nodeIdentityProvider = mock(NodeIdentityProvider.class);

        when(nodeIdentityProvider.getNodeIdentity()).thenReturn(
                new NodeIdentity(
                        "local-node-1",
                        "0x0000000000000000000000000000000000000001",
                        "0x0000000000000000000000000000000000000001",
                        "http://localhost:8080",
                        "ECDSA_SECP256K1_PERSONAL_SIGN"
                )
        );

        Instant startedAt = Instant.parse("2026-05-22T10:00:00Z");
        Clock clock = Clock.fixed(
                Instant.parse("2026-05-22T10:15:00Z"),
                ZoneOffset.UTC
        );

        LocalNodeStatusProvider provider = new LocalNodeStatusProvider(
                nodeIdentityProvider,
                clock,
                startedAt
        );

        NodeStatus result = provider.getNodeStatus();

        assertEquals("local-node-1", result.nodeId());
        assertEquals("UP", result.status());
        assertEquals(startedAt, result.startedAt());
        assertEquals(Instant.parse("2026-05-22T10:15:00Z"), result.currentTime());
        assertEquals(900, result.uptimeSeconds());
    }
}