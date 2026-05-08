package io.custos.node.core.application.service;

import io.custos.node.core.application.exception.SecretShareAccessDeniedException;
import io.custos.node.core.application.exception.SecretShareNotFoundException;
import io.custos.node.core.application.port.in.command.RetrieveSecretShareCommand;
import io.custos.node.core.application.port.out.*;
import io.custos.node.core.domain.PolicyValidationResult;
import io.custos.node.core.domain.model.AccessPolicy;
import io.custos.node.core.domain.model.PolicyType;
import io.custos.node.core.domain.model.SecretShareDelivery;
import io.custos.node.core.domain.model.StoredSecretShare;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RetrieveSecretShareServiceTest {

    private static final String NODE_ID = "local-node-1";
    private static final Instant NOW = Instant.parse("2026-05-04T10:15:30Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private SecretShareRepository repository;
    private WalletSignatureVerifier walletSignatureVerifier;
    private AccessPolicyValidator evmErc1155BalancePolicyValidator;
    private PolicyValidationService policyValidationService;
    private WalletNonceStore walletNonceStore;
    private WalletNonceService walletNonceService;
    private ShareProtectionService shareProtectionService;
    private NodeSignatureService nodeSignatureService;

    private RetrieveSecretShareService service;

    @BeforeEach
    void setUp() {
        repository = mock(SecretShareRepository.class);
        walletSignatureVerifier = mock(WalletSignatureVerifier.class);
        evmErc1155BalancePolicyValidator = mock(AccessPolicyValidator.class);
        walletNonceStore = mock(WalletNonceStore.class);
        shareProtectionService = mock(ShareProtectionService.class);
        nodeSignatureService = mock(NodeSignatureService.class);
        when(evmErc1155BalancePolicyValidator.supportedType()).thenReturn(PolicyType.EVM_ERC1155_BALANCE);
        policyValidationService = new PolicyValidationService(List.of(evmErc1155BalancePolicyValidator));
        walletNonceService = new WalletNonceService(CLOCK, walletNonceStore);

        service = new RetrieveSecretShareService(
                NODE_ID,
                CLOCK,
                repository,
                walletSignatureVerifier,
                policyValidationService,
                walletNonceService,
                shareProtectionService,
                nodeSignatureService
        );
    }

    @Test
    void shouldRetrieveSecretShareWhenSignatureNoncePolicyAndProtectionAreValid() {
        AccessPolicy policy = validPolicy();

        StoredSecretShare stored = new StoredSecretShare(
                "1",
                "encrypted-share",
                policy,
                "0xPublisher",
                Instant.parse("2026-05-01T00:00:00Z")
        );

        RetrieveSecretShareCommand command = validCommand();

        when(repository.findBySecretId("1")).thenReturn(Optional.of(stored));
        when(evmErc1155BalancePolicyValidator.supportedType()).thenReturn(PolicyType.EVM_ERC1155_BALANCE);
        when(evmErc1155BalancePolicyValidator.validate(policy, command.userAddress()))
                .thenReturn(PolicyValidationResult.valid());
        when(shareProtectionService.protect("encrypted-share", "0xREADER_PUBLIC_KEY_TEST"))
                .thenReturn("protected-share");
        when(nodeSignatureService.sign("1:" + command.userAddress() + ":protected-share"))
                .thenReturn("node-signature");

        SecretShareDelivery result = service.retrieve(command);

        assertEquals("1", result.secretId());
        assertEquals(NODE_ID, result.nodeId());
        assertEquals("protected-share", result.protectedShare());
        assertEquals("node-signature", result.nodeSignature());
        assertEquals(NOW, result.deliveredAt());

        verify(walletSignatureVerifier).verifyRetrieveSecretSignature(
                command.secretId(),
                command.userAddress(),
                command.nonce(),
                command.walletSignature()
        );

        verify(walletNonceStore).markAsUsed(argThat(nonce ->
                nonce.userAddress().equals(command.userAddress().toLowerCase())
                        && nonce.secretId().equals("1")
                        && nonce.nonce().equals("test-nonce-1234")
                        && nonce.usedAt().equals(NOW)
        ));

        verify(repository).findBySecretId("1");
        verify(evmErc1155BalancePolicyValidator).validate(policy, command.userAddress());
        verify(shareProtectionService).protect("encrypted-share", "0xREADER_PUBLIC_KEY_TEST");
        verify(nodeSignatureService).sign("1:" + command.userAddress() + ":protected-share");
    }

    @Test
    void shouldNotRetrieveShareWhenSecretShareDoesNotExist() {
        RetrieveSecretShareCommand command = validCommand();

        when(repository.findBySecretId("1")).thenReturn(Optional.empty());

        assertThrows(SecretShareNotFoundException.class, () -> service.retrieve(command));

        verify(walletSignatureVerifier).verifyRetrieveSecretSignature(
                command.secretId(),
                command.userAddress(),
                command.nonce(),
                command.walletSignature()
        );

        verify(walletNonceStore).markAsUsed(any());
        verify(repository).findBySecretId("1");

        verifyNoInteractions(shareProtectionService);
        verifyNoInteractions(nodeSignatureService);
    }

    @Test
    void shouldDenyAccessWhenPolicyValidationFails() {
        AccessPolicy policy = validPolicy();

        StoredSecretShare stored = new StoredSecretShare(
                "1",
                "encrypted-share",
                policy,
                "0xPublisher",
                Instant.parse("2026-05-01T00:00:00Z")
        );

        RetrieveSecretShareCommand command = validCommand();

        when(repository.findBySecretId("1")).thenReturn(Optional.of(stored));
        when(evmErc1155BalancePolicyValidator.supportedType()).thenReturn(PolicyType.EVM_ERC1155_BALANCE);
        when(evmErc1155BalancePolicyValidator.validate(policy, command.userAddress()))
                .thenReturn(PolicyValidationResult.invalid("INSUFFICIENT_BALANCE"));

        assertThrows(SecretShareAccessDeniedException.class, () -> service.retrieve(command));

        verify(walletSignatureVerifier).verifyRetrieveSecretSignature(
                command.secretId(),
                command.userAddress(),
                command.nonce(),
                command.walletSignature()
        );

        verify(walletNonceStore).markAsUsed(any());
        verify(repository).findBySecretId("1");
        verify(evmErc1155BalancePolicyValidator).validate(policy, command.userAddress());

        verifyNoInteractions(shareProtectionService);
        verifyNoInteractions(nodeSignatureService);
    }

    @Test
    void shouldStopImmediatelyWhenWalletSignatureVerifierThrows() {
        RetrieveSecretShareCommand command = validCommand();

        doThrow(new RuntimeException("invalid signature"))
                .when(walletSignatureVerifier)
                .verifyRetrieveSecretSignature(
                        command.secretId(),
                        command.userAddress(),
                        command.nonce(),
                        command.walletSignature()
                );

        assertThrows(RuntimeException.class, () -> service.retrieve(command));

        verifyNoInteractions(walletNonceStore);
        verifyNoInteractions(repository);
        verifyNoInteractions(shareProtectionService);
        verifyNoInteractions(nodeSignatureService);
    }

    private RetrieveSecretShareCommand validCommand() {
        return new RetrieveSecretShareCommand(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "0x2eaae67211205e5ad48847d3a64251b2ec1d0b3bedee679a468127aa842aa8400c4ef9939ec8c628fbe9d240255d411532baed6e76a2c2e99d92d166cb3cfbb71c",
                "0xREADER_PUBLIC_KEY_TEST",
                "test-nonce-1234"
        );
    }

    private AccessPolicy validPolicy() {
        return new AccessPolicy(
                PolicyType.EVM_ERC1155_BALANCE,
                31337L,
                "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                "{\"tokenId\":\"1\",\"minBalance\":\"1\"}"
        );
    }
}