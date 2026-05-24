package io.custos.node.adapters.out.config;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.out.NodePeerProvider;
import io.custos.node.core.domain.model.NodePeers;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
public class ConfiguredNodePeerProvider implements NodePeerProvider {

    private final CustosProperties custosProperties;

    public ConfiguredNodePeerProvider(CustosProperties custosProperties) {
        this.custosProperties = custosProperties;
    }

    @Override
    public NodePeers getNodePeers() {
        return new NodePeers(
                custosProperties.node().id(),
                configuredPeers()
        );
    }

    private List<NodePeers.Peer> configuredPeers() {
        if (custosProperties.node().peers() == null || custosProperties.node().peers().isEmpty()) {
            return List.of();
        }

        return custosProperties.node()
                .peers()
                .stream()
                .filter(peer -> peer != null && !peer.isBlank())
                .map(String::trim)
                .map(this::validatePeerBaseUrl)
                .distinct()
                .map(NodePeers.Peer::new)
                .toList();
    }

    private String validatePeerBaseUrl(String baseUrl) {
        try {
            URI uri = URI.create(baseUrl);

            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalStateException("Invalid peer baseUrl: " + baseUrl);
            }

            if (!uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {
                throw new IllegalStateException("Unsupported peer baseUrl scheme: " + baseUrl);
            }

            return baseUrl;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid peer baseUrl: " + baseUrl, e);
        }
    }
}