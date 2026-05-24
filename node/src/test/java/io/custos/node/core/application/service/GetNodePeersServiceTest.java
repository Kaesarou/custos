package io.custos.node.core.application.service;

import io.custos.node.core.application.port.out.NodePeerProvider;
import io.custos.node.core.domain.model.NodePeers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetNodePeersServiceTest {

    @Test
    void shouldReturnNodePeersFromProvider() {
        NodePeerProvider nodePeerProvider = mock(NodePeerProvider.class);

        NodePeers expectedPeers = new NodePeers(
                "local-node-1",
                List.of(
                        new NodePeers.Peer("http://localhost:8082"),
                        new NodePeers.Peer("http://localhost:8083")
                )
        );

        when(nodePeerProvider.getNodePeers()).thenReturn(expectedPeers);

        GetNodePeersService service = new GetNodePeersService(nodePeerProvider);

        NodePeers result = service.getNodePeers();

        assertEquals(expectedPeers, result);
        verify(nodePeerProvider).getNodePeers();
    }
}