 package io.custos.node.adapters.in.web.dto;

 import io.custos.node.core.domain.model.*;

 import java.util.List;

public record NetworkViewResponseDto(
        String observerNodeId,
        List<PeerNodeViewDto> peers
) {
    public static NetworkViewResponseDto fromDomain(LocalNetworkView view) {
        return new NetworkViewResponseDto(
                view.observerNodeId(),
                view.peers()
                        .stream()
                        .map(PeerNodeViewDto::fromDomain)
                        .toList()
        );
    }

    public record PeerNodeViewDto(
            String baseUrl,
            boolean reachable,
            NodeIdentityDto identity,
            NodeStatusDto status,
            NodeCapabilitiesDto capabilities,
            String failureReason
    ) {
        public static PeerNodeViewDto fromDomain(PeerNodeView peer) {
            return new PeerNodeViewDto(
                    peer.baseUrl(),
                    peer.reachable(),
                    peer.identity() == null ? null : NodeIdentityDto.fromDomain(peer.identity()),
                    peer.status() == null ? null : NodeStatusDto.fromDomain(peer.status()),
                    peer.capabilities() == null ? null : NodeCapabilitiesDto.fromDomain(peer.capabilities()),
                    peer.failureReason()
            );
        }
    }

    public record NodeIdentityDto(
            String nodeId,
            String nodeAddress,
            String rewardAddress,
            String publicBaseUrl,
            String signatureAlgorithm
    ) {
        public static NodeIdentityDto fromDomain(NodeIdentity identity) {
            return new NodeIdentityDto(
                    identity.nodeId(),
                    identity.nodeAddress(),
                    identity.rewardAddress(),
                    identity.publicBaseUrl(),
                    identity.signatureAlgorithm()
            );
        }
    }

    public record NodeStatusDto(
            String nodeId,
            String status,
            String startedAt,
            String currentTime,
            long uptimeSeconds
    ) {
        public static NodeStatusDto fromDomain(NodeStatus status) {
            return new NodeStatusDto(
                    status.nodeId(),
                    status.status(),
                    status.startedAt().toString(),
                    status.currentTime().toString(),
                    status.uptimeSeconds()
            );
        }
    }

    public record NodeCapabilitiesDto(
            String nodeId,
            List<String> supportedPolicyTypes,
            List<String> supportedShareProtectionAlgorithms,
            String signatureAlgorithm,
            List<SupportedChainDto> supportedChains
    ) {
        public static NodeCapabilitiesDto fromDomain(NodeCapabilities capabilities) {
            return new NodeCapabilitiesDto(
                    capabilities.nodeId(),
                    capabilities.supportedPolicyTypes()
                            .stream()
                            .map(Enum::name)
                            .toList(),
                    capabilities.supportedShareProtectionAlgorithms(),
                    capabilities.signatureAlgorithm(),
                    capabilities.supportedChains()
                            .stream()
                            .map(SupportedChainDto::fromDomain)
                            .toList()
            );
        }
    }

    public record SupportedChainDto(long chainId) {
        public static SupportedChainDto fromDomain(NodeCapabilities.SupportedChain chain) {
            return new SupportedChainDto(chain.chainId());
        }
    }
}