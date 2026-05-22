package io.custos.node.adapters.in.web.dto;

import io.custos.node.core.domain.model.NodeCapabilities;

import java.util.List;

public record NodeCapabilitiesResponseDto(
        String nodeId,
        List<String> supportedPolicyTypes,
        List<String> supportedShareProtectionAlgorithms,
        String signatureAlgorithm,
        List<SupportedChainDto> supportedChains
) {
    public static NodeCapabilitiesResponseDto fromDomain(NodeCapabilities capabilities) {
        return new NodeCapabilitiesResponseDto(
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

    public record SupportedChainDto(
            long chainId
    ) {
        public static SupportedChainDto fromDomain(NodeCapabilities.SupportedChain chain) {
            return new SupportedChainDto(chain.chainId());
        }
    }
}