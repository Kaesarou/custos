package io.custos.node.adapters.in.web;

import io.custos.node.adapters.in.web.dto.NodeIdentityResponseDto;
import io.custos.node.core.application.port.in.GetNodeIdentityUseCase;
import io.custos.node.core.domain.model.NodeIdentity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${custos.api.base-path:/api/v1}/node")
public class NodeController {

    private final GetNodeIdentityUseCase getNodeIdentityUseCase;

    public NodeController(GetNodeIdentityUseCase getNodeIdentityUseCase) {
        this.getNodeIdentityUseCase = getNodeIdentityUseCase;
    }

    @GetMapping("/id")
    public NodeIdentityResponseDto getNodeIdentity() {
        NodeIdentity identity = getNodeIdentityUseCase.getNodeIdentity();
        return NodeIdentityResponseDto.fromDomain(identity);
    }
}