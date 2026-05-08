package io.custos.node.core.application.port.out;

import io.custos.node.core.application.port.in.command.StoreSecretShareCommand;

public interface PublisherSignatureVerifier {
    void verifyStoreSecretSignature(StoreSecretShareCommand command);
}
