package io.custos.node.adapters.in.web;

import io.custos.node.adapters.in.web.dto.NetworkViewResponseDto;
import io.custos.node.core.application.port.in.GetLocalNetworkViewUseCase;
import io.custos.node.core.domain.model.LocalNetworkView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${custos.api.base-path:/api/v1}/network")
public class NetworkController {

    private final GetLocalNetworkViewUseCase getLocalNetworkViewUseCase;

    public NetworkController(GetLocalNetworkViewUseCase getLocalNetworkViewUseCase) {
        this.getLocalNetworkViewUseCase = getLocalNetworkViewUseCase;
    }

    @GetMapping("/view")
    public NetworkViewResponseDto getLocalNetworkView() {
        LocalNetworkView view = getLocalNetworkViewUseCase.getLocalNetworkView();
        return NetworkViewResponseDto.fromDomain(view);
    }
}