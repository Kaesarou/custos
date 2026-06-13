package io.custos.node.adapters.out.security;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.out.NodeIdentityProvider;
import io.custos.node.core.domain.model.NodeIdentity;
import io.custos.node.core.domain.model.NodeSignatureAlgorithm;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.net.URI;

@Service
public class LocalNodeIdentityProvider implements NodeIdentityProvider {

    private final CustosProperties custosProperties;
    private final RomanNodeIdGenerator nodeIdGenerator;
    private final Credentials nodeCredentials;

    public LocalNodeIdentityProvider(
            CustosProperties custosProperties,
            RomanNodeIdGenerator nodeIdGenerator
    ) {
        this.custosProperties = custosProperties;
        this.nodeIdGenerator = nodeIdGenerator;
        this.nodeCredentials = Credentials.create(resolvePrivateKey(custosProperties));
    }

    @Override
    public NodeIdentity getNodeIdentity() {
        String nodeAddress = nodeCredentials.getAddress();
        String nodeId = resolveNodeId(nodeAddress);
        String rewardAddress = resolveRewardAddress(nodeAddress);
        String publicBaseUrl = resolvePublicBaseUrl();

        return new NodeIdentity(
                nodeId,
                nodeAddress,
                rewardAddress,
                publicBaseUrl,
                NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN
        );
    }

    private String resolvePrivateKey(CustosProperties custosProperties) {
        String privateKey = custosProperties.node().privateKey();

        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException("custos.node.private-key is required");
        }

        return privateKey;
    }

    private String resolveNodeId(String nodeAddress) {
        String configuredNodeId = custosProperties.node().id();

        if (configuredNodeId == null || configuredNodeId.isBlank()) {
            return nodeIdGenerator.generateFromAddress(nodeAddress);
        }

        return configuredNodeId;
    }

    private String resolveRewardAddress(String nodeAddress) {
        String configuredRewardAddress = custosProperties.node().rewardAddress();

        if (configuredRewardAddress == null || configuredRewardAddress.isBlank()) {
            return nodeAddress;
        }

        if (!WalletUtils.isValidAddress(configuredRewardAddress)) {
            throw new IllegalStateException("Invalid custos.node.reward-address");
        }

        return configuredRewardAddress;
    }

    private String resolvePublicBaseUrl() {
        String publicBaseUrl = custosProperties.node().publicBaseUrl();

        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw new IllegalStateException("custos.node.public-base-url is required");
        }

        try {
            URI uri = URI.create(publicBaseUrl);

            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalStateException("Invalid custos.node.public-base-url");
            }

            if (!uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {
                throw new IllegalStateException("Unsupported custos.node.public-base-url scheme");
            }

            return publicBaseUrl;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid custos.node.public-base-url", e);
        }
    }
}