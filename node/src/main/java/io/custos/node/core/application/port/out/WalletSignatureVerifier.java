package io.custos.node.core.application.port.out;

import io.custos.node.core.application.port.in.command.RetrieveSecretShareCommand;

public interface WalletSignatureVerifier {
    void verifyRetrieveSecretSignature(RetrieveSecretShareCommand retrieveSecretShareCommand);
}
