package io.custos.node.adapters.out.config;

import io.custos.node.core.application.port.out.NodeIdentityProvider;
import io.custos.node.core.application.port.out.NodeStatusProvider;
import io.custos.node.core.domain.model.NodeStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class LocalNodeStatusProvider implements NodeStatusProvider {

    private final NodeIdentityProvider nodeIdentityProvider;
    private final Clock clock;
    private final Instant startedAt;

    public LocalNodeStatusProvider(
            NodeIdentityProvider nodeIdentityProvider,
            Clock clock,
            Instant nodeStartedAt
    ) {
        this.nodeIdentityProvider = nodeIdentityProvider;
        this.clock = clock;
        this.startedAt = nodeStartedAt;
    }

    @Override
    public NodeStatus getNodeStatus() {
        Instant currentTime = Instant.now(clock);

        return new NodeStatus(
                nodeIdentityProvider.getNodeIdentity().nodeId(),
                "UP",
                startedAt,
                currentTime,
                Duration.between(startedAt, currentTime).toSeconds()
        );
    }
}