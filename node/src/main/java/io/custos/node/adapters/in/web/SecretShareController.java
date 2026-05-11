package io.custos.node.adapters.in.web;

import io.custos.node.adapters.in.web.dto.*;
import io.custos.node.core.application.port.in.RetrieveSecretShareUseCase;
import io.custos.node.core.application.port.in.StoreSecretShareUseCase;
import io.custos.node.core.application.port.in.command.RetrieveSecretShareCommand;
import io.custos.node.core.application.port.in.command.StoreSecretShareCommand;
import io.custos.node.core.domain.model.AccessPolicy;
import io.custos.node.core.domain.model.PolicyType;
import io.custos.node.core.domain.model.SecretShareDelivery;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${custos.api.base-path:/api/v1}/secret-shares")
public class SecretShareController {

    private final StoreSecretShareUseCase storeSecretShareUseCase;
    private final RetrieveSecretShareUseCase retrieveSecretShareUseCase;

    public SecretShareController(
            StoreSecretShareUseCase storeSecretShareUseCase,
            RetrieveSecretShareUseCase retrieveSecretShareUseCase
    ) {
        this.storeSecretShareUseCase = storeSecretShareUseCase;
        this.retrieveSecretShareUseCase = retrieveSecretShareUseCase;
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterSecretShareResponse register(@Valid @RequestBody RegisterSecretShareRequest request) {
        storeSecretShareUseCase.store(new StoreSecretShareCommand(
                request.secretId(),
                request.encryptedShare(),
                toDomainPolicy(request.policy()),
                request.publisherAddress(),
                request.publisherSignature()
        ));
        return new RegisterSecretShareResponse("OK");
    }

    @PostMapping("/{secretId}/retrieve")
    public RetrieveSecretShareResponseDto requestShare(@PathVariable String secretId,
                                                       @Valid @RequestBody RetrieveSecretShareRequestDto request) {
        SecretShareDelivery delivery = retrieveSecretShareUseCase.retrieve(new RetrieveSecretShareCommand(
                secretId,
                request.userAddress(),
                request.walletSignature(),
                request.readerPublicKey(),
                request.nonce()
        ));

        return new RetrieveSecretShareResponseDto(
                delivery.secretId(),
                delivery.nodeId(),
                ProtectedShareDto.fromDomain(delivery.protectedShare()),
                delivery.nodeSignature(),
                delivery.deliveredAt().toString()
        );
    }

    private AccessPolicy toDomainPolicy(PolicyDto policyDto) {
        return new AccessPolicy(
                PolicyType.valueOf(policyDto.type()),
                policyDto.chainId(),
                policyDto.contractAddress(),
                policyDto.policyData()
        );
    }
}
