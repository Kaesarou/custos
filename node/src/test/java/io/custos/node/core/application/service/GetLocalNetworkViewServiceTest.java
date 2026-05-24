package io.custos.node.core.application.service;

import io.custos.node.core.application.port.out.NodePeerProvider;
import io.custos.node.core.application.port.out.PeerClient;
import io.custos.node.core.domain.model.LocalNetworkView;
import io.custos.node.core.domain.model.NodePeers;
import io.custos.node.core.domain.model.PeerNodeView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetLocalNetworkViewServiceTest {

    @Test
    void shouldBuildLocalNetworkViewFromConfiguredPeers() {
        NodePeerProvider nodePeerProvider = mock(NodePeerProvider.class);
        PeerClient peerClient = mock(PeerClient.class);

        when(nodePeerProvider.getNodePeers()).thenReturn(
                new NodePeers(
                        "local-node-1",
                        List.of(
                                new NodePeers.Peer("http://localhost:8082"),
                                new NodePeers.Peer("http://localhost:8083")
                        )
                )
        );

        when(peerClient.inspectPeer("http://localhost:8082"))
                .thenReturn(PeerNodeView.unreachable("http://localhost:8082", "PEER_UNREACHABLE"));

        when(peerClient.inspectPeer("http://localhost:8083"))
                .thenReturn(PeerNodeView.unreachable("http://localhost:8083", "PEER_UNREACHABLE"));

        GetLocalNetworkViewService service = new GetLocalNetworkViewService(
                nodePeerProvider,
                peerClient
        );

        LocalNetworkView result = service.getLocalNetworkView();

        assertEquals("local-node-1", result.observerNodeId());
        assertEquals(2, result.peers().size());

        verify(peerClient).inspectPeer("http://localhost:8082");
        verify(peerClient).inspectPeer("http://localhost:8083");
    }
}