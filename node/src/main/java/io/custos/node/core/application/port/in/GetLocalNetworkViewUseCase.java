package io.custos.node.core.application.port.in;

import io.custos.node.core.domain.model.LocalNetworkView;

public interface GetLocalNetworkViewUseCase {
    LocalNetworkView getLocalNetworkView();
}