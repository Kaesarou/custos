package io.custos.node.core.application.service;

import io.custos.node.core.application.exception.SecretShareAccessDeniedException;
import io.custos.node.core.application.exception.SecretShareNotFoundException;
import io.custos.node.core.application.port.in.RetrieveSecretShareUseCase;
import io.custos.node.core.application.port.in.command.RetrieveSecretShareCommand;
import io.custos.node.core.application.port.out.NodeSignatureService;
import io.custos.node.core.application.port.out.SecretShareRepository;
import io.custos.node.core.application.port.out.ShareProtectionService;
import io.custos.node.core.application.port.out.WalletSignatureVerifier;
import io.custos.node.core.domain.PolicyValidationResult;
import io.custos.node.core.domain.model.ProtectedShare;
import io.custos.node.core.domain.model.SecretShareDelivery;
import io.custos.node.core.domain.model.StoredSecretShare;

import java.time.Clock;
import java.time.Instant;

public class RetrieveSecretShareService implements RetrieveSecretShareUseCase {

    private final String nodeId;
    private final Clock clock;
    private final SecretShareRepository repository;
    private final WalletSignatureVerifier walletSignatureVerifier;
    private final PolicyValidationService policyValidationService;
    private final WalletNonceService walletNonceService;
    private final ShareProtectionService shareProtectionService;
    private final NodeSignatureService nodeSignatureService;

    public RetrieveSecretShareService(String nodeId, Clock clock,
                                      SecretShareRepository repository,
                                      WalletSignatureVerifier walletSignatureVerifier,
                                      PolicyValidationService policyValidationService,
                                      WalletNonceService walletNonceService,
                                      ShareProtectionService shareProtectionService,
                                      NodeSignatureService nodeSignatureService) {
        this.nodeId = nodeId;
        this.clock = clock;
        this.repository = repository;
        this.walletSignatureVerifier = walletSignatureVerifier;
        this.policyValidationService = policyValidationService;
        this.walletNonceService = walletNonceService;
        this.shareProtectionService = shareProtectionService;
        this.nodeSignatureService = nodeSignatureService;
    }

    @Override
    public SecretShareDelivery retrieve(RetrieveSecretShareCommand command) {

        walletSignatureVerifier.verifyRetrieveSecretSignature(command);

        walletNonceService.markNonceAsUsed(command.userAddress(), command.secretId(), command.nonce());

        StoredSecretShare stored = repository.findBySecretId(command.secretId())
                .orElseThrow(() -> new SecretShareNotFoundException(command.secretId()));

        PolicyValidationResult policyValidationResult = policyValidationService.validate(stored.accessPolicy(),
                command.userAddress());
        if (!policyValidationResult.isValid()) {
            throw new SecretShareAccessDeniedException(command.secretId(), command.userAddress(),
                    policyValidationResult.reason());
        }

        ProtectedShare protectedShare = shareProtectionService.protect(
                stored.encryptedShare(),
                command.readerPublicKey()
        );

        String payloadToSign = deliveryPayloadToSign(command, protectedShare);
        String nodeSignature = nodeSignatureService.sign(payloadToSign);

        return new SecretShareDelivery(command.secretId(), nodeId, protectedShare, nodeSignature, Instant.now(clock));
    }

    private String deliveryPayloadToSign(
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
