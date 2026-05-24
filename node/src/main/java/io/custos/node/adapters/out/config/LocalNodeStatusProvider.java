package io.custos.node.adapters.out.config;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.out.NodeStatusProvider;
import io.custos.node.core.domain.model.NodeStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class LocalNodeStatusProvider implements NodeStatusProvider {

    private final CustosProperties custosProperties;
    private final Clock clock;
    private final Instant startedAt;

    public LocalNodeStatusProvider(
            CustosProperties custosProperties,
            Clock clock,
            Instant nodeStartedAt
    ) {
        this.custosProperties = custosProperties;
        this.clock = clock;
        this.startedAt = nodeStartedAt;
    }

    @Override
    public NodeStatus getNodeStatus() {
        Instant currentTime = Instant.now(clock);

        return new NodeStatus(
                custosProperties.node().id(),
                "UP",
                startedAt,
                currentTime,
                Duration.between(startedAt, currentTime).toSeconds()
        );
    }
}