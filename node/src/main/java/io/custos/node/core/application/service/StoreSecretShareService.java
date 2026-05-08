package io.custos.node.core.application.service;

import io.custos.node.core.application.exception.InvalidPublisherSignatureException;
import io.custos.node.core.application.port.in.StoreSecretShareUseCase;
import io.custos.node.core.application.port.in.command.StoreSecretShareCommand;
import io.custos.node.core.application.port.out.PublisherSignatureVerifier;
import io.custos.node.core.application.port.out.SecretShareRepository;
import io.custos.node.core.domain.model.StoredSecretShare;

import java.time.Clock;
import java.time.Instant;

public class StoreSecretShareService implements StoreSecretShareUseCase {

    private final Clock clock;
    private final SecretShareRepository repository;
    private final PublisherSignatureVerifier publisherSignatureVerifier;

    public StoreSecretShareService(
            Clock clock,
            SecretShareRepository repository,
            PublisherSignatureVerifier publisherSignatureVerifier
    ) {
        this.clock = clock;
        this.repository = repository;
        this.publisherSignatureVerifier = publisherSignatureVerifier;
    }

    @Override
    public void store(StoreSecretShareCommand command) {

        this.publisherSignatureVerifier.verifyStoreSecretSignature(command);

        repository.save(new StoredSecretShare(
                command.secretId(),
                command.encryptedShare(),
                command.accessPolicy(),
                command.publisherAddress(),
                Instant.now(clock)
        ));
    }
}
