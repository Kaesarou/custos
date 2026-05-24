package io.custos.node.adapters.out.network.dto;

import java.util.List;

public record RemoteNodeCapabilitiesDto(
        String nodeId,
        List<String> supportedPolicyTypes,
        List<String> supportedShareProtectionAlgorithms,
        String signatureAlgorithm,
        List<RemoteSupportedChainDto> supportedChains
) {
    public record RemoteSupportedChainDto(long chainId) {
    }
}