package io.custos.node.adapters.out.config;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.domain.ShareProtectionAlgorithm;
import io.custos.node.core.domain.model.NodeCapabilities;
import io.custos.node.core.domain.model.NodeSignatureAlgorithm;
import io.custos.node.core.domain.model.PolicyType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalNodeCapabilitiesProviderTest {

    @Test
    void shouldReturnLocalNodeCapabilities() {
        CustosProperties properties = new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        "0xprivate-key",
                        "",
                        List.of()
                ),
                Map.of(
                        31337L,
                        new CustosProperties.ChainConfig("http://localhost:8545")
                )
        );

        LocalNodeCapabilitiesProvider provider = new LocalNodeCapabilitiesProvider(properties);

        NodeCapabilities result = provider.getNodeCapabilities();

        assertEquals("local-node-1", result.nodeId());
        assertEquals(List.of(PolicyType.EVM_ERC1155_BALANCE), result.supportedPolicyTypes());
        assertEquals(
                List.of(ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM),
                result.supportedShareProtectionAlgorithms()
        );
        assertEquals(
                NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN,
                result.signatureAlgorithm()
        );
        assertEquals(1, result.supportedChains().size());
        assertEquals(31337L, result.supportedChains().get(0).chainId());
    }
}