package io.custos.node.core.application.service;

import io.custos.node.core.application.exception.InvalidPublisherSignatureException;
import io.custos.node.core.application.exception.errorcode.WalletSignatureErrorCode;
import io.custos.node.core.application.port.in.command.StoreSecretShareCommand;
import io.custos.node.core.application.port.out.PublisherSignatureVerifier;
import io.custos.node.core.application.port.out.SecretShareRepository;
import io.custos.node.core.domain.model.AccessPolicy;
import io.custos.node.core.domain.model.PolicyType;
import io.custos.node.core.domain.model.StoredSecretShare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

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

        doNothing().when(publisherSignatureVerifier).verifyStoreSecretSignature(command);

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

        doThrow(new InvalidPublisherSignatureException(WalletSignatureErrorCode.INVALID_WALLET_SIGNATURE,
                "Invalid wallet signature")).when(publisherSignatureVerifier)
                .verifyStoreSecretSignature(command);


        assertThrows(InvalidPublisherSignatureException.class, () -> service.store(command));

        verifyNoInteractions(repository);
    }

    @Test
    void shouldVerifyPublisherSignatureBeforeSavingSecretShare() {
        SecretShareRepository repository = mock(SecretShareRepository.class);
        PublisherSignatureVerifier verifier = mock(PublisherSignatureVerifier.class);
        Clock clock = Clock.fixed(Instant.parse("2026-05-04T10:15:30Z"), ZoneOffset.UTC);

        StoreSecretShareService service = new StoreSecretShareService(clock, repository, verifier);

        StoreSecretShareCommand command = validCommand();

        service.store(command);

        InOrder inOrder = inOrder(verifier, repository);
        inOrder.verify(verifier).verifyStoreSecretSignature(command);
        inOrder.verify(repository).save(any(StoredSecretShare.class));
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