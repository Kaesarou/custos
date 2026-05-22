package io.custos.node.config;

import io.custos.node.core.application.port.in.GetNodeIdentityUseCase;
import io.custos.node.core.application.port.in.RetrieveSecretShareUseCase;
import io.custos.node.core.application.port.in.StoreSecretShareUseCase;
import io.custos.node.core.application.port.out.*;
import io.custos.node.core.application.service.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
@EnableConfigurationProperties(CustosProperties.class)
public class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    PolicyValidationService policyValidationService(List<AccessPolicyValidator> accessPolicyValidators) {
        return new PolicyValidationService(accessPolicyValidators);
    }

    @Bean
    WalletNonceService walletNonceService(Clock clock, WalletNonceStore walletNonceStore) {
        return new WalletNonceService(clock, walletNonceStore);
    }

    @Bean
    StoreSecretShareUseCase storeSecretShareUseCase(
            Clock clock,
            SecretShareRepository repository,
            PublisherSignatureVerifier publisherSignatureVerifier
    ) {
        return new StoreSecretShareService(clock, repository, publisherSignatureVerifier);
    }

    @Bean
    RetrieveSecretShareUseCase retrieveSecretShareUseCase(
            CustosProperties custosProperties,
            Clock clock,
            SecretShareRepository repository,
            WalletSignatureVerifier walletSignatureVerifier,
            PolicyValidationService policyValidationService,
            WalletNonceService walletNonceService,
            ShareProtectionService shareProtectionService,
            NodeSignatureService nodeSignatureService
    ) {
        return new RetrieveSecretShareService(
                custosProperties.node().id(),
                clock,
                repository,
                walletSignatureVerifier,
                policyValidationService,
                walletNonceService,
                shareProtectionService,
                nodeSignatureService
        );
    }

    @Bean
    public GetNodeIdentityUseCase getNodeIdentityUseCase(
            NodeIdentityProvider nodeIdentityProvider
    ) {
        return new GetNodeIdentityService(nodeIdentityProvider);
    }
}
