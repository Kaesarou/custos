package io.custos.node.adapters.out.blockchain.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.custos.node.adapters.out.blockchain.ChainRpcResolver;
import io.custos.node.core.application.port.out.AccessPolicyValidator;
import io.custos.node.core.domain.EvmErc1155BalancePolicyData;
import io.custos.node.core.domain.PolicyValidationResult;
import io.custos.node.core.domain.model.AccessPolicy;
import io.custos.node.core.domain.model.PolicyType;
import org.springframework.stereotype.Service;
import org.web3j.crypto.WalletUtils;

import java.math.BigInteger;

import static io.custos.node.core.application.exception.errorcode.PolicyErrorCode.*;
import static io.custos.node.core.application.exception.errorcode.WalletErrorCode.INVALID_WALLET;

@Service
public class EvmErc1155BalancePolicyValidator implements AccessPolicyValidator {

    private final ObjectMapper objectMapper;
    private final ChainRpcResolver chainRpcResolver;
    private final EvmErc1155BalanceReader balanceReader;

    public EvmErc1155BalancePolicyValidator(
            ObjectMapper objectMapper,
            ChainRpcResolver chainRpcResolver,
            EvmErc1155BalanceReader balanceReader
    ) {
        this.objectMapper = objectMapper;
        this.chainRpcResolver = chainRpcResolver;
        this.balanceReader = balanceReader;
    }

    @Override
    public PolicyType supportedType() {
        return PolicyType.EVM_ERC1155_BALANCE;
    }

    @Override
    public PolicyValidationResult validate(AccessPolicy policy, String walletAddress) {
        if (policy == null || policy.type() != PolicyType.EVM_ERC1155_BALANCE) {
            return PolicyValidationResult.invalid(UNSUPPORTED_POLICY_TYPE.name());
        }

        if (!WalletUtils.isValidAddress(walletAddress)) {
            return PolicyValidationResult.invalid(INVALID_WALLET.name());
        }

        if (!WalletUtils.isValidAddress(policy.contractAddress())) {
            return PolicyValidationResult.invalid(INVALID_CONTRACT_ADDRESS.name());
        }

        var rpcUrl = chainRpcResolver.resolveRpcUrl(policy.chainId());

        if (rpcUrl.isEmpty()) {
            return PolicyValidationResult.invalid(CHAIN_NOT_CONFIGURED.name());
        }

        EvmErc1155BalancePolicyData policyData;

        try {
            policyData = objectMapper.readValue(policy.policyData(), EvmErc1155BalancePolicyData.class);
        } catch (Exception e) {
            return PolicyValidationResult.invalid(INVALID_POLICY_DATA.name());
        }

        BigInteger tokenId;
        BigInteger minBalance;

        try {
            tokenId = new BigInteger(policyData.tokenId());
            minBalance = new BigInteger(policyData.minBalance());
        } catch (Exception e) {
            return PolicyValidationResult.invalid(INVALID_POLICY_DATA.name());
        }

        if (tokenId.signum() < 0 || minBalance.signum() <= 0) {
            return PolicyValidationResult.invalid(INVALID_POLICY_DATA.name());
        }

        try {
            BigInteger balance = balanceReader.balanceOf(
                    rpcUrl.get(),
                    policy.contractAddress(),
                    walletAddress,
                    tokenId
            );

            if (balance.compareTo(minBalance) >= 0) {
                return PolicyValidationResult.valid();
            }

            return PolicyValidationResult.invalid(INSUFFICIENT_BALANCE.name());

        } catch (Exception e) {
            return PolicyValidationResult.invalid(ON_CHAIN_CALL_FAILED.name());
        }
    }
}