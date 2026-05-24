package io.custos.node.adapters.out.config;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.domain.model.NodeStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalNodeStatusProviderTest {

    @Test
    void shouldReturnLocalNodeStatus() {
        CustosProperties properties = new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        "0xprivate-key",
                        "",
                        List.of()
                ),
                Map.of()
        );

        Instant startedAt = Instant.parse("2026-05-22T10:00:00Z");
        Clock clock = Clock.fixed(
                Instant.parse("2026-05-22T10:15:00Z"),
                ZoneOffset.UTC
        );

        LocalNodeStatusProvider provider = new LocalNodeStatusProvider(
                properties,
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