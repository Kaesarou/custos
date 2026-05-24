package io.custos.node.adapters.out.config;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.out.NodeCapabilitiesProvider;
import io.custos.node.core.domain.ShareProtectionAlgorithm;
import io.custos.node.core.domain.model.NodeCapabilities;
import io.custos.node.core.domain.model.NodeSignatureAlgorithm;
import io.custos.node.core.domain.model.PolicyType;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class LocalNodeCapabilitiesProvider implements NodeCapabilitiesProvider {

    private final CustosProperties custosProperties;

    public LocalNodeCapabilitiesProvider(CustosProperties custosProperties) {
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