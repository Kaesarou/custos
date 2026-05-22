package io.custos.node.core.application.service;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.in.GetNodeStatusUseCase;
import io.custos.node.core.domain.model.NodeStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class GetNodeStatusService implements GetNodeStatusUseCase {

    private final CustosProperties custosProperties;
    private final Clock clock;
    private final Instant startedAt;

    public GetNodeStatusService(
            CustosProperties custosProperties,
            Clock clock,
            Instant startedAt
    ) {
        this.custosProperties = custosProperties;
        this.clock = clock;
        this.startedAt = startedAt;
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