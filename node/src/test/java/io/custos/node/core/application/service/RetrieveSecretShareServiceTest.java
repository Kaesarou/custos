package io.custos.node.core.application.service;

import io.custos.node.core.application.exception.SecretShareAccessDeniedException;
import io.custos.node.core.application.exception.SecretShareNotFoundException;
import io.custos.node.core.application.port.in.command.RetrieveSecretShareCommand;
import io.custos.node.core.application.port.out.*;
import io.custos.node.core.domain.PolicyValidationResult;
import io.custos.node.core.domain.model.*;
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

    private static final String SECRET_ID = "1";
    private static final String USER_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";
    private static final String USER_ADDRESS_LOWERCASE = "0x70997970c51812dc3a010c7d01b50e0d17dc79c8";
    private static final String READER_PUBLIC_KEY = "y5VMaQ_llLbDlKwKwV0au2VWPiijb125n_fvOSoS61o";
    private static final String NONCE = "test-nonce-1234";

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

        when(evmErc1155BalancePolicyValidator.supportedType())
                .thenReturn(PolicyType.EVM_ERC1155_BALANCE);

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
                SECRET_ID,
                "encrypted-share",
                policy,
                "0xPublisher",
                Instant.parse("2026-05-01T00:00:00Z")
        );

        RetrieveSecretShareCommand command = validCommand();

        ProtectedShare protectedShare = validProtectedShare();

        String payloadToSign = expectedPayloadToSign(command, protectedShare);

        when(repository.findBySecretId(SECRET_ID)).thenReturn(Optional.of(stored));
        when(evmErc1155BalancePolicyValidator.validate(policy, command.userAddress()))
                .thenReturn(PolicyValidationResult.valid());
        when(shareProtectionService.protect("encrypted-share", READER_PUBLIC_KEY))
                .thenReturn(protectedShare);
        when(nodeSignatureService.sign(payloadToSign))
                .thenReturn("node-signature");

        SecretShareDelivery result = service.retrieve(command);

        assertEquals(SECRET_ID, result.secretId());
        assertEquals(NODE_ID, result.nodeId());
        assertEquals(protectedShare, result.protectedShare());
        assertEquals("node-signature", result.nodeSignature());
        assertEquals(NOW, result.deliveredAt());

        verify(walletSignatureVerifier).verifyRetrieveSecretSignature(command);

        verify(walletNonceStore).markAsUsed(argThat(nonce ->
                nonce.userAddress().equals(USER_ADDRESS_LOWERCASE)
                        && nonce.secretId().equals(SECRET_ID)
                        && nonce.nonce().equals(NONCE)
                        && nonce.usedAt().equals(NOW)
        ));

        verify(repository).findBySecretId(SECRET_ID);
        verify(evmErc1155BalancePolicyValidator).validate(policy, command.userAddress());
        verify(shareProtectionService).protect("encrypted-share", READER_PUBLIC_KEY);
        verify(nodeSignatureService).sign(payloadToSign);
    }

    @Test
    void shouldNotRetrieveShareWhenSecretShareDoesNotExist() {
        RetrieveSecretShareCommand command = validCommand();

        when(repository.findBySecretId(SECRET_ID)).thenReturn(Optional.empty());

        assertThrows(SecretShareNotFoundException.class, () -> service.retrieve(command));

        verify(walletSignatureVerifier).verifyRetrieveSecretSignature(command);
        verify(walletNonceStore).markAsUsed(any());
        verify(repository).findBySecretId(SECRET_ID);

        verifyNoInteractions(shareProtectionService);
        verifyNoInteractions(nodeSignatureService);
    }

    @Test
    void shouldDenyAccessWhenPolicyValidationFails() {
        AccessPolicy policy = validPolicy();

        StoredSecretShare stored = new StoredSecretShare(
                SECRET_ID,
                "encrypted-share",
                policy,
                "0xPublisher",
                Instant.parse("2026-05-01T00:00:00Z")
        );

        RetrieveSecretShareCommand command = validCommand();

        when(repository.findBySecretId(SECRET_ID)).thenReturn(Optional.of(stored));
        when(evmErc1155BalancePolicyValidator.validate(policy, command.userAddress()))
                .thenReturn(PolicyValidationResult.invalid("INSUFFICIENT_BALANCE"));

        assertThrows(SecretShareAccessDeniedException.class, () -> service.retrieve(command));

        verify(walletSignatureVerifier).verifyRetrieveSecretSignature(command);
        verify(walletNonceStore).markAsUsed(any());
        verify(repository).findBySecretId(SECRET_ID);
        verify(evmErc1155BalancePolicyValidator).validate(policy, command.userAddress());

        verifyNoInteractions(shareProtectionService);
        verifyNoInteractions(nodeSignatureService);
    }

    @Test
    void shouldStopImmediatelyWhenWalletSignatureVerifierThrows() {
        RetrieveSecretShareCommand command = validCommand();

        doThrow(new RuntimeException("invalid signature"))
                .when(walletSignatureVerifier)
                .verifyRetrieveSecretSignature(command);

        assertThrows(RuntimeException.class, () -> service.retrieve(command));

        verify(walletSignatureVerifier).verifyRetrieveSecretSignature(command);
        verifyNoInteractions(walletNonceStore);
        verifyNoInteractions(repository);
        verifyNoInteractions(shareProtectionService);
        verifyNoInteractions(nodeSignatureService);
    }

    private RetrieveSecretShareCommand validCommand() {
        return new RetrieveSecretShareCommand(
                SECRET_ID,
                USER_ADDRESS,
                "0x22965675a0fc18c4f9b7ac04b6d4621ab690be18c00d85018162a0d36a0a0fd849b0a56467e8cdb6bbb178ae227458e25a3aeb3e52a0fffe277b142931f968211c",
                READER_PUBLIC_KEY,
                NONCE
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

    private ProtectedShare validProtectedShare() {
        return new ProtectedShare(
                ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM,
                "ephemeral-public-key",
                "iv",
                "ciphertext"
        );
    }

    private String expectedPayloadToSign(
            RetrieveSecretShareCommand command,
            ProtectedShare protectedShare
    ) {
        return command.secretId()
                + ":"
                + command.userAddress().toLowerCase()
                + ":"
                + protectedShare.alg()
                + ":"
                + protectedShare.ephemeralPublicKey()
                + ":"
                + protectedShare.iv()
                + ":"
                + protectedShare.ciphertext();
    }
}