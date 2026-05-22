package io.custos.node.adapters.in.web;

import io.custos.node.adapters.in.web.dto.NodeCapabilitiesResponseDto;
import io.custos.node.adapters.in.web.dto.NodeIdentityResponseDto;
import io.custos.node.adapters.in.web.dto.NodeStatusResponseDto;
import io.custos.node.core.application.port.in.GetNodeCapabilitiesUseCase;
import io.custos.node.core.application.port.in.GetNodeIdentityUseCase;
import io.custos.node.core.application.port.in.GetNodeStatusUseCase;
import io.custos.node.core.domain.model.NodeCapabilities;
import io.custos.node.core.domain.model.NodeIdentity;
import io.custos.node.core.domain.model.NodeStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${custos.api.base-path:/api/v1}/node")
public class NodeController {

    private final GetNodeIdentityUseCase getNodeIdentityUseCase;
    private final GetNodeStatusUseCase getNodeStatusUseCase;
    private final GetNodeCapabilitiesUseCase getNodeCapabilitiesUseCase;

    public NodeController(
            GetNodeIdentityUseCase getNodeIdentityUseCase,
            GetNodeStatusUseCase getNodeStatusUseCase,
            GetNodeCapabilitiesUseCase getNodeCapabilitiesUseCase
    ) {
        this.getNodeIdentityUseCase = getNodeIdentityUseCase;
        this.getNodeStatusUseCase = getNodeStatusUseCase;
        this.getNodeCapabilitiesUseCase = getNodeCapabilitiesUseCase;
    }

    @GetMapping("/id")
    public NodeIdentityResponseDto getNodeIdentity() {
        NodeIdentity identity = getNodeIdentityUseCase.getNodeIdentity();
        return NodeIdentityResponseDto.fromDomain(identity);
    }

    @GetMapping("/status")
    public NodeStatusResponseDto getNodeStatus() {
        NodeStatus status = getNodeStatusUseCase.getNodeStatus();
        return NodeStatusResponseDto.fromDomain(status);
    }

    @GetMapping("/capabilities")
    public NodeCapabilitiesResponseDto getNodeCapabilities() {
        NodeCapabilities capabilities = getNodeCapabilitiesUseCase.getNodeCapabilities();
        return NodeCapabilitiesResponseDto.fromDomain(capabilities);
    }
}