package io.custos.node.core.application.service;

import io.custos.node.core.application.port.out.NodeStatusProvider;
import io.custos.node.core.domain.model.NodeStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetNodeStatusServiceTest {

    @Test
    void shouldReturnNodeStatusFromProvider() {
        NodeStatusProvider provider = mock(NodeStatusProvider.class);

        NodeStatus expected = new NodeStatus(
                "local-node-1",
                "UP",
                Instant.parse("2026-05-22T10:00:00Z"),
                Instant.parse("2026-05-22T10:15:00Z"),
                900
        );

        when(provider.getNodeStatus()).thenReturn(expected);

        GetNodeStatusService service = new GetNodeStatusService(provider);

        NodeStatus result = service.getNodeStatus();

        assertEquals(expected, result);
        verify(provider).getNodeStatus();
    }
}