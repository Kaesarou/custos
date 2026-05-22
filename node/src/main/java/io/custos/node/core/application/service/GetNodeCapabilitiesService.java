package io.custos.node.core.application.service;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.in.GetNodeCapabilitiesUseCase;
import io.custos.node.core.domain.ShareProtectionAlgorithm;
import io.custos.node.core.domain.model.NodeCapabilities;
import io.custos.node.core.domain.model.NodeSignatureAlgorithm;
import io.custos.node.core.domain.model.PolicyType;

import java.util.Comparator;
import java.util.List;

public class GetNodeCapabilitiesService implements GetNodeCapabilitiesUseCase {

    private final CustosProperties custosProperties;

    public GetNodeCapabilitiesService(CustosProperties custosProperties) {
        this.custosProperties = custosProperties;
    }

    @Override
    public NodeCapabilities getNodeCapabilities() {
        return new NodeCapabilities(
                custosProperties.node().id(),
                List.of(PolicyType.EVM_ERC1155_BALANCE),
                List.of(ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM),
                NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN,
                supportedChains()
        );
    }

    private List<NodeCapabilities.SupportedChain> supportedChains() {
        if (custosProperties.chains() == null || custosProperties.chains().isEmpty()) {
            return List.of();
        }

        return custosProperties.chains()
                .keySet()
                .stream()
                .sorted(Comparator.naturalOrder())
                .map(NodeCapabilities.SupportedChain::new)
                .toList();
    }
}