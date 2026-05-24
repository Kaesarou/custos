package io.custos.node.adapters.out.config;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.out.NodePeerProvider;
import io.custos.node.core.domain.model.NodePeers;
import org.springframework.stereotype.Component;

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
                .distinct()
                .map(NodePeers.Peer::new)
                .toList();
    }
}