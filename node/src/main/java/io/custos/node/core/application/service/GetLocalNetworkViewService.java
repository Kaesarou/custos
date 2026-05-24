package io.custos.node.core.application.service;

import io.custos.node.core.application.port.in.GetLocalNetworkViewUseCase;
import io.custos.node.core.application.port.out.NodePeerProvider;
import io.custos.node.core.application.port.out.PeerClient;
import io.custos.node.core.domain.model.LocalNetworkView;
import io.custos.node.core.domain.model.NodePeers;
import io.custos.node.core.domain.model.PeerNodeView;

import java.util.List;

public class GetLocalNetworkViewService implements GetLocalNetworkViewUseCase {

    private final NodePeerProvider nodePeerProvider;
    private final PeerClient peerClient;

    public GetLocalNetworkViewService(
            NodePeerProvider nodePeerProvider,
            PeerClient peerClient
    ) {
        this.nodePeerProvider = nodePeerProvider;
        this.peerClient = peerClient;
    }

    @Override
    public LocalNetworkView getLocalNetworkView() {
        NodePeers nodePeers = nodePeerProvider.getNodePeers();

        List<PeerNodeView> peerViews = nodePeers.peers()
                .stream()
                .map(peer -> peerClient.inspectPeer(peer.baseUrl()))
                .toList();

        return new LocalNetworkView(
                nodePeers.nodeId(),
                peerViews
        );
    }
}