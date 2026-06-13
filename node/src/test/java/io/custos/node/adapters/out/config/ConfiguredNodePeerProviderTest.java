package io.custos.node.adapters.out.config;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.out.NodeIdentityProvider;
import io.custos.node.core.domain.model.NodeIdentity;
import io.custos.node.core.domain.model.NodePeers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfiguredNodePeerProviderTest {

    @Test
    void shouldReturnConfiguredPeers() {
        CustosProperties properties = propertiesWithPeers(
                List.of(
                        "http://localhost:8082",
                        "http://localhost:8083"
                )
        );

        ConfiguredNodePeerProvider provider = new ConfiguredNodePeerProvider(
                properties,
                nodeIdentityProvider()
        );

        NodePeers result = provider.getNodePeers();

        assertEquals("local-node-1", result.nodeId());
        assertEquals(2, result.peers().size());
        assertEquals("http://localhost:8082", result.peers().get(0).baseUrl());
        assertEquals("http://localhost:8083", result.peers().get(1).baseUrl());
    }

    @Test
    void shouldReturnEmptyPeersWhenNoPeerIsConfigured() {
        CustosProperties properties = propertiesWithPeers(null);

        ConfiguredNodePeerProvider provider = new ConfiguredNodePeerProvider(
                properties,
                nodeIdentityProvider()
        );

        NodePeers result = provider.getNodePeers();

        assertEquals("local-node-1", result.nodeId());
        assertEquals(0, result.peers().size());
    }

    @Test
    void shouldRemoveDuplicatedPeers() {
        CustosProperties properties = propertiesWithPeers(
                List.of(
                        "http://localhost:8082",
                        "http://localhost:8082",
                        "http://localhost:8083"
                )
        );

        ConfiguredNodePeerProvider provider = new ConfiguredNodePeerProvider(
                properties,
                nodeIdentityProvider()
        );

        NodePeers result = provider.getNodePeers();

        assertEquals(2, result.peers().size());
        assertEquals("http://localhost:8082", result.peers().get(0).baseUrl());
        assertEquals("http://localhost:8083", result.peers().get(1).baseUrl());
    }

    @Test
    void shouldRejectInvalidPeerBaseUrl() {
        CustosProperties properties = propertiesWithPeers(List.of("banana"));

        ConfiguredNodePeerProvider provider = new ConfiguredNodePeerProvider(
                properties,
                nodeIdentityProvider()
        );

        assertThrows(IllegalStateException.class, provider::getNodePeers);
    }

    private CustosProperties propertiesWithPeers(List<String> peers) {
        return new CustosProperties(
                new CustosProperties.NodeConfig(
                        "",
                        "0xprivate-key",
                        "",
                        "http://localhost:8080",
                        peers
                ),
                Map.of()
        );
    }

    private NodeIdentityProvider nodeIdentityProvider() {
        NodeIdentityProvider nodeIdentityProvider = mock(NodeIdentityProvider.class);

        when(nodeIdentityProvider.getNodeIdentity()).thenReturn(
                new NodeIdentity(
                        "local-node-1",
                        "0x0000000000000000000000000000000000000001",
                        "0x0000000000000000000000000000000000000001",
                        "http://localhost:8080",
                        "ECDSA_SECP256K1_PERSONAL_SIGN"
                )
        );

        return nodeIdentityProvider;
    }
}