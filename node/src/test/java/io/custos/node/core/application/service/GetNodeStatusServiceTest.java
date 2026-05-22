package io.custos.node.core.application.service;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.domain.model.NodeStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetNodeStatusServiceTest {

    @Test
    void shouldReturnNodeStatus() {
        CustosProperties properties = new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        "0xprivate-key",
                        ""
                ),
                Map.of()
        );

        Instant startedAt = Instant.parse("2026-05-22T10:00:00Z");
        Clock clock = Clock.fixed(
                Instant.parse("2026-05-22T10:15:00Z"),
                ZoneOffset.UTC
        );

        GetNodeStatusService service = new GetNodeStatusService(
                properties,
                clock,
                startedAt
        );

        NodeStatus status = service.getNodeStatus();

        assertEquals("local-node-1", status.nodeId());
        assertEquals("UP", status.status());
        assertEquals(Instant.parse("2026-05-22T10:00:00Z"), status.startedAt());
        assertEquals(Instant.parse("2026-05-22T10:15:00Z"), status.currentTime());
        assertEquals(900, status.uptimeSeconds());
    }
}