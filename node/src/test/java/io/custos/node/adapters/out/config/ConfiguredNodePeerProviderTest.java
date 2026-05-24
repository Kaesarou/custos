package io.custos.node.adapters.out.config;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.domain.model.NodePeers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguredNodePeerProviderTest {

    @Test
    void shouldReturnConfiguredPeers() {
        CustosProperties properties = new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        "0xprivate-key",
                        "",
                        List.of(
                                "http://localhost:8082",
                                "http://localhost:8083"
                        )
                ),
                Map.of()
        );

        ConfiguredNodePeerProvider provider = new ConfiguredNodePeerProvider(properties);

        NodePeers result = provider.getNodePeers();

        assertEquals("local-node-1", result.nodeId());
        assertEquals(2, result.peers().size());
        assertEquals("http://localhost:8082", result.peers().get(0).baseUrl());
        assertEquals("http://localhost:8083", result.peers().get(1).baseUrl());
    }

    @Test
    void shouldReturnEmptyPeersWhenNoPeerIsConfigured() {
        CustosProperties properties = new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        "0xprivate-key",
                        "",
                        null
                ),
                Map.of()
        );

        ConfiguredNodePeerProvider provider = new ConfiguredNodePeerProvider(properties);

        NodePeers result = provider.getNodePeers();

        assertEquals("local-node-1", result.nodeId());
        assertEquals(0, result.peers().size());
    }

    @Test
    void shouldRemoveDuplicatedPeers() {
        CustosProperties properties = new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        "0xprivate-key",
                        "",
                        List.of(
                                "http://localhost:8082",
                                "http://localhost:8082",
                                "http://localhost:8083"
                        )
                ),
                Map.of()
        );

        ConfiguredNodePeerProvider provider = new ConfiguredNodePeerProvider(properties);

        NodePeers result = provider.getNodePeers();

        assertEquals(2, result.peers().size());
        assertEquals("http://localhost:8082", result.peers().get(0).baseUrl());
        assertEquals("http://localhost:8083", result.peers().get(1).baseUrl());
    }

    @Test
    void shouldRejectInvalidPeerBaseUrl() {
        CustosProperties properties = new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        "0xprivate-key",
                        "",
                        List.of("banana")
                ),
                Map.of()
        );

        ConfiguredNodePeerProvider provider = new ConfiguredNodePeerProvider(properties);

        assertThrows(IllegalStateException.class, provider::getNodePeers);
    }
}