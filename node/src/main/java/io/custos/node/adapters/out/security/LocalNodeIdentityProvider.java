package io.custos.node.adapters.out.security;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.out.NodeIdentityProvider;
import io.custos.node.core.domain.model.NodeIdentity;
import io.custos.node.core.domain.model.NodeSignatureAlgorithm;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

@Service
public class LocalNodeIdentityProvider implements NodeIdentityProvider {

    private final CustosProperties custosProperties;
    private final Credentials nodeCredentials;

    public LocalNodeIdentityProvider(CustosProperties custosProperties) {
        this.custosProperties = custosProperties;
        this.nodeCredentials = Credentials.create(custosProperties.node().privateKey());
    }

    @Override
    public NodeIdentity getNodeIdentity() {
        String nodeAddress = nodeCredentials.getAddress();
        String rewardAddress = resolveRewardAddress(nodeAddress);

        return new NodeIdentity(
                custosProperties.node().id(),
                nodeAddress,
                rewardAddress,
                NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN
        );
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
}