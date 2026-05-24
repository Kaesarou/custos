package io.custos.node.adapters.out.security;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.domain.model.NodeIdentity;
import io.custos.node.core.domain.model.NodeSignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalNodeIdentityProviderTest {

    private static final String NODE_PRIVATE_KEY =
            "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";

    @Test
    void shouldUseNodeAddressAsRewardAddressWhenRewardAddressIsMissing() {
        CustosProperties properties = new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        NODE_PRIVATE_KEY,
                        "",
                        List.of()
                ),
                Map.of()
        );

        LocalNodeIdentityProvider provider = new LocalNodeIdentityProvider(properties);

        NodeIdentity identity = provider.getNodeIdentity();

        assertEquals("local-node-1", identity.nodeId());
        assertEquals("0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266", identity.nodeAddress().toLowerCase());
        assertEquals(identity.nodeAddress(), identity.rewardAddress());
        assertEquals(
                NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN,
                identity.signatureAlgorithm()
        );
    }

    @Test
    void shouldUseConfiguredRewardAddressWhenProvided() {
        String coldRewardAddress = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266";

        CustosProperties properties = new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        NODE_PRIVATE_KEY,
                        coldRewardAddress,
                        List.of()
                ),
                Map.of()
        );

        LocalNodeIdentityProvider provider = new LocalNodeIdentityProvider(properties);

        NodeIdentity identity = provider.getNodeIdentity();

        assertEquals(coldRewardAddress, identity.rewardAddress());
    }
}