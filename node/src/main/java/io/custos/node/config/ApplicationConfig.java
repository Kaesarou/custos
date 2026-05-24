package io.custos.node.config;

import io.custos.node.core.application.port.in.*;
import io.custos.node.core.application.port.out.*;
import io.custos.node.core.application.service.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Configuration
@EnableConfigurationProperties(CustosProperties.class)
public class ApplicationConfig {

    @Bean
    public Instant nodeStartedAt() {
        return Instant.now();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
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

    @Bean
    public GetNodeStatusUseCase getNodeStatusUseCase(
            NodeStatusProvider nodeStatusProvider
    ) {
        return new GetNodeStatusService(nodeStatusProvider);
    }

    @Bean
    public GetNodeCapabilitiesUseCase getNodeCapabilitiesUseCase(
            NodeCapabilitiesProvider nodeCapabilitiesProvider
    ) {
        return new GetNodeCapabilitiesService(nodeCapabilitiesProvider);
    }

    @Bean
    public GetNodePeersUseCase getNodePeersUseCase(
            NodePeerProvider nodePeerProvider
    ) {
        return new GetNodePeersService(nodePeerProvider);
    }

    @Bean
    public GetLocalNetworkViewUseCase getLocalNetworkViewUseCase(
            NodePeerProvider nodePeerProvider,
            PeerClient peerClient
    ) {
        return new GetLocalNetworkViewService(
                nodePeerProvider,
                peerClient
        );
    }
}
