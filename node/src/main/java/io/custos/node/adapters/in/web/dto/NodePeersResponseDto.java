package io.custos.node.adapters.in.web.dto;

import io.custos.node.core.domain.model.NodePeers;

import java.util.List;

public record NodePeersResponseDto(
        String nodeId,
        List<PeerDto> peers
) {
    public static NodePeersResponseDto fromDomain(NodePeers nodePeers) {
        return new NodePeersResponseDto(
                nodePeers.nodeId(),
                nodePeers.peers()
                        .stream()
                        .map(PeerDto::fromDomain)
                        .toList()
        );
    }

    public record PeerDto(
            String baseUrl
    ) {
        public static PeerDto fromDomain(NodePeers.Peer peer) {
            return new PeerDto(peer.baseUrl());
        }
    }
}