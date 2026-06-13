package io.custos.node.adapters.out.blockchain.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.custos.node.adapters.out.blockchain.ChainRpcResolver;
import io.custos.node.config.CustosProperties;
import io.custos.node.core.domain.PolicyValidationResult;
import io.custos.node.core.domain.model.AccessPolicy;
import io.custos.node.core.domain.model.PolicyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvmErc1155BalancePolicyValidatorTest {

    private static final String RPC_URL = "http://localhost:8545";
    private static final long CHAIN_ID = 31337L;
    private static final String CONTRACT_ADDRESS = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";
    private static final String WALLET_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";

    private EvmErc1155BalanceReader balanceReader;
    private EvmErc1155BalancePolicyValidator validator;

    @BeforeEach
    void setUp() {
        balanceReader = mock(EvmErc1155BalanceReader.class);

        ChainRpcResolver chainRpcResolver = new ChainRpcResolver(custosPropertiesWithConfiguredChain());

        validator = new EvmErc1155BalancePolicyValidator(
                new ObjectMapper(),
                chainRpcResolver,
                balanceReader
        );
    }

    @Test
    void shouldSupportEvmErc1155BalancePolicyType() {
        assertEquals(PolicyType.EVM_ERC1155_BALANCE, validator.supportedType());
    }

    @Test
    void shouldValidatePolicyWhenBalanceIsGreaterThanMinBalance() {
        when(balanceReader.balanceOf(
                RPC_URL,
                CONTRACT_ADDRESS,
                WALLET_ADDRESS,
                BigInteger.ONE
        )).thenReturn(BigInteger.TEN);

        PolicyValidationResult result = validator.validate(
                validPolicy("{\"tokenId\":\"1\",\"minBalance\":\"1\"}"),
                WALLET_ADDRESS
        );

        assertTrue(result.isValid());

        verify(balanceReader).balanceOf(
                RPC_URL,
                CONTRACT_ADDRESS,
                WALLET_ADDRESS,
                BigInteger.ONE
        );
    }

    @Test
    void shouldValidatePolicyWhenBalanceIsEqualToMinBalance() {
        when(balanceReader.balanceOf(
                RPC_URL,
                CONTRACT_ADDRESS,
                WALLET_ADDRESS,
                BigInteger.ONE
        )).thenReturn(BigInteger.ONE);

        PolicyValidationResult result = validator.validate(
                validPolicy("{\"tokenId\":\"1\",\"minBalance\":\"1\"}"),
                WALLET_ADDRESS
        );

        assertTrue(result.isValid());
    }

    @Test
    void shouldRejectPolicyWhenBalanceIsLowerThanMinBalance() {
        when(balanceReader.balanceOf(
                RPC_URL,
                CONTRACT_ADDRESS,
                WALLET_ADDRESS,
                BigInteger.ONE
        )).thenReturn(BigInteger.ZERO);

        PolicyValidationResult result = validator.validate(
                validPolicy("{\"tokenId\":\"1\",\"minBalance\":\"1\"}"),
                WALLET_ADDRESS
        );

        assertFalse(result.isValid());
        assertEquals("INSUFFICIENT_BALANCE", result.reason());
    }

    @Test
    void shouldRejectNullPolicy() {
        PolicyValidationResult result = validator.validate(null, WALLET_ADDRESS);

        assertFalse(result.isValid());
        assertEquals("UNSUPPORTED_POLICY_TYPE", result.reason());

        verifyNoInteractions(balanceReader);
    }

    @Test
    void shouldRejectInvalidWalletAddress() {
        PolicyValidationResult result = validator.validate(
                validPolicy("{\"tokenId\":\"1\",\"minBalance\":\"1\"}"),
                "invalid-wallet"
        );

        assertFalse(result.isValid());
        assertEquals("INVALID_WALLET", result.reason());

        verifyNoInteractions(balanceReader);
    }

    @Test
    void shouldRejectInvalidContractAddress() {
        AccessPolicy policy = new AccessPolicy(
                PolicyType.EVM_ERC1155_BALANCE,
                CHAIN_ID,
                "invalid-contract",
                "{\"tokenId\":\"1\",\"minBalance\":\"1\"}"
        );

        PolicyValidationResult result = validator.validate(policy, WALLET_ADDRESS);

        assertFalse(result.isValid());
        assertEquals("INVALID_CONTRACT_ADDRESS", result.reason());

        verifyNoInteractions(balanceReader);
    }

    @Test
    void shouldRejectPolicyWhenChainIsNotConfigured() {
        EvmErc1155BalancePolicyValidator validatorWithoutChain =
                new EvmErc1155BalancePolicyValidator(
                        new ObjectMapper(),
                        new ChainRpcResolver(custosPropertiesWithoutConfiguredChains()),
                        balanceReader
                );

        PolicyValidationResult result = validatorWithoutChain.validate(
                validPolicy("{\"tokenId\":\"1\",\"minBalance\":\"1\"}"),
                WALLET_ADDRESS
        );

        assertFalse(result.isValid());
        assertEquals("CHAIN_NOT_CONFIGURED", result.reason());

        verifyNoInteractions(balanceReader);
    }

    @Test
    void shouldRejectInvalidJsonPolicyData() {
        PolicyValidationResult result = validator.validate(
                validPolicy("not-json"),
                WALLET_ADDRESS
        );

        assertFalse(result.isValid());
        assertEquals("INVALID_POLICY_DATA", result.reason());

        verifyNoInteractions(balanceReader);
    }

    @Test
    void shouldRejectPolicyDataWhenTokenIdIsNotANumber() {
        PolicyValidationResult result = validator.validate(
                validPolicy("{\"tokenId\":\"abc\",\"minBalance\":\"1\"}"),
                WALLET_ADDRESS
        );

        assertFalse(result.isValid());
        assertEquals("INVALID_POLICY_DATA", result.reason());

        verifyNoInteractions(balanceReader);
    }

    @Test
    void shouldRejectPolicyDataWhenMinBalanceIsNotANumber() {
        PolicyValidationResult result = validator.validate(
                validPolicy("{\"tokenId\":\"1\",\"minBalance\":\"abc\"}"),
                WALLET_ADDRESS
        );

        assertFalse(result.isValid());
        assertEquals("INVALID_POLICY_DATA", result.reason());

        verifyNoInteractions(balanceReader);
    }

    @Test
    void shouldRejectPolicyDataWhenTokenIdIsNegative() {
        PolicyValidationResult result = validator.validate(
                validPolicy("{\"tokenId\":\"-1\",\"minBalance\":\"1\"}"),
                WALLET_ADDRESS
        );

        assertFalse(result.isValid());
        assertEquals("INVALID_POLICY_DATA", result.reason());

        verifyNoInteractions(balanceReader);
    }

    @Test
    void shouldRejectPolicyDataWhenMinBalanceIsZero() {
        PolicyValidationResult result = validator.validate(
                validPolicy("{\"tokenId\":\"1\",\"minBalance\":\"0\"}"),
                WALLET_ADDRESS
        );

        assertFalse(result.isValid());
        assertEquals("INVALID_POLICY_DATA", result.reason());

        verifyNoInteractions(balanceReader);
    }

    @Test
    void shouldReturnOnChainCallFailedWhenBalanceReaderThrows() {
        when(balanceReader.balanceOf(
                RPC_URL,
                CONTRACT_ADDRESS,
                WALLET_ADDRESS,
                BigInteger.ONE
        )).thenThrow(new IllegalStateException("RPC failed"));

        PolicyValidationResult result = validator.validate(
                validPolicy("{\"tokenId\":\"1\",\"minBalance\":\"1\"}"),
                WALLET_ADDRESS
        );

        assertFalse(result.isValid());
        assertEquals("ON_CHAIN_CALL_FAILED", result.reason());
    }

    private AccessPolicy validPolicy(String policyData) {
        return new AccessPolicy(
                PolicyType.EVM_ERC1155_BALANCE,
                CHAIN_ID,
                CONTRACT_ADDRESS,
                policyData
        );
    }

    private CustosProperties custosPropertiesWithConfiguredChain() {
        return new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80",
                        "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266",
                        "http://localhost:8080",
                        List.of()
                ),
                Map.of(
                        CHAIN_ID,
                        new CustosProperties.ChainConfig(RPC_URL)
                )
        );
    }

    private CustosProperties custosPropertiesWithoutConfiguredChains() {
        return new CustosProperties(
                new CustosProperties.NodeConfig(
                        "local-node-1",
                        "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80",
                        "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266",
                        "http://localhost:8080",
                        List.of()
                ),
                Map.of()
        );
    }
}