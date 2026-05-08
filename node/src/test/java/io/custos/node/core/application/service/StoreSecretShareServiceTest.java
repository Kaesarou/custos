package io.custos.node.core.application.service;

import io.custos.node.core.application.exception.InvalidPublisherSignatureException;
import io.custos.node.core.application.port.in.command.StoreSecretShareCommand;
import io.custos.node.core.application.port.out.PublisherSignatureVerifier;
import io.custos.node.core.application.port.out.SecretShareRepository;
import io.custos.node.core.domain.model.AccessPolicy;
import io.custos.node.core.domain.model.PolicyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class StoreSecretShareServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-04T10:15:30Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private SecretShareRepository repository;
    private PublisherSignatureVerifier publisherSignatureVerifier;
    private StoreSecretShareService service;

    @BeforeEach
    void setUp() {
        repository = mock(SecretShareRepository.class);
        publisherSignatureVerifier = mock(PublisherSignatureVerifier.class);

        service = new StoreSecretShareService(
                CLOCK,
                repository,
                publisherSignatureVerifier
        );
    }

    @Test
    void shouldStoreSecretShareWhenPublisherSignatureIsValid() {
        StoreSecretShareCommand command = validCommand();

        when(publisherSignatureVerifier.isValid(command)).thenReturn(true);

        service.store(command);

        verify(repository).save(argThat(stored ->
                stored.secretId().equals("1")
                        && stored.encryptedShare().equals("encrypted-share")
                        && stored.publisherAddress().equals("0xPublisher")
                        && stored.createdAt().equals(NOW)
                        && stored.accessPolicy().type() == PolicyType.EVM_ERC1155_BALANCE
        ));
    }

    @Test
    void shouldRejectSecretShareWhenPublisherSignatureIsInvalid() {
        StoreSecretShareCommand command = validCommand();

        when(publisherSignatureVerifier.isValid(command)).thenReturn(false);

        assertThrows(InvalidPublisherSignatureException.class, () -> service.store(command));

        verifyNoInteractions(repository);
    }

    private StoreSecretShareCommand validCommand() {
        return new StoreSecretShareCommand(
                "1",
                "encrypted-share",
                new AccessPolicy(
                        PolicyType.EVM_ERC1155_BALANCE,
                        31337L,
                        "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                        "{\"tokenId\":\"1\",\"minBalance\":\"1\"}"
                ),
                "0xPublisher",
                "publisher-signature"
        );
    }
}