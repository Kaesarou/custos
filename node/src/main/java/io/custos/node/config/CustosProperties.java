package io.custos.node.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "custos")
public record CustosProperties(
        NodeConfig node,
        Map<Long, ChainConfig> chains
) {
    public record NodeConfig(
            String id,
            String privateKey,
            String rewardAddress
    ) {
    }

    public record ChainConfig(
            String rpcUrl
    ) {
    }
}