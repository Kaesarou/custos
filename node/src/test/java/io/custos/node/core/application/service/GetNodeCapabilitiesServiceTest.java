package io.custos.node.core.application.service;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.domain.ShareProtectionAlgorithm;
import io.custos.node.core.domain.model.NodeCapabilities;
import io.custos.node.core.domain.model.NodeSignatureAlgorithm;
import io.custos.node.core.domain.model.PolicyType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetNodeCapabilitiesServiceTest {

    @Test
    void shouldReturnNodeCapabilities() {
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

        GetNodeCapabilitiesService service = new GetNodeCapabilitiesService(properties);

        NodeCapabilities capabilities = service.getNodeCapabilities();

        assertEquals("local-node-1", capabilities.nodeId());
        assertEquals(
                List.of(PolicyType.EVM_ERC1155_BALANCE),
                capabilities.supportedPolicyTypes()
        );
        assertEquals(
                List.of(ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM),
                capabilities.supportedShareProtectionAlgorithms()
        );
        assertEquals(
                NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN,
                capabilities.signatureAlgorithm()
        );
        assertEquals(1, capabilities.supportedChains().size());
        assertEquals(31337L, capabilities.supportedChains().getFirst().chainId());
    }
}