package io.custos.node.adapters.in.web;

import io.custos.node.EmbeddedPostgresConfiguration;
import io.custos.node.adapters.out.blockchain.policy.EvmErc1155BalanceReader;
import io.custos.node.core.domain.RetrieveSecretShareSignatureChallenge;
import io.custos.node.core.domain.StoreSecretShareSignatureChallenge;
import io.custos.node.core.domain.model.AccessPolicy;
import io.custos.node.core.domain.model.PolicyType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "custos.node.id=local-node-1",
        "custos.node.private-key=0x8b3a350cf5c34c9194ca3a545d1e4a25a96baf46794c8c62036111ae114f5ee",
        "custos.node.reward-address=",
        "custos.api.base-path=/api/v1",
        "custos.chains.31337.rpc-url=http://localhost:8545",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(EmbeddedPostgresConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecretShareControllerIT {

    private static final String SECRET_ID = "1";
    private static final String ENCRYPTED_SHARE = "encrypted-share";

    private static final String USER_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";
    private static final String USER_PRIVATE_KEY = "0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d";

    private static final String PUBLISHER_ADDRESS = "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC";
    private static final String PUBLISHER_PRIVATE_KEY = "0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a";

    private static final String CONTRACT_ADDRESS = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";
    private static final String RPC_URL = "http://localhost:8545";
    private static final String POLICY_DATA = "{\"tokenId\":\"1\",\"minBalance\":\"1\"}";

    private static final String READER_PUBLIC_KEY = "y5VMaQ_llLbDlKwKwV0au2VWPiijb125n_fvOSoS61o";

    private static final String SUCCESS_NONCE = "test-nonce-success";
    private static final String POLICY_DENIED_NONCE = "test-nonce-policy-denied";
    private static final String INVALID_SIGNATURE_NONCE = "test-nonce-invalid-signature";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvmErc1155BalanceReader balanceReader;

    @Test
    @Order(1)
    void shouldStoreSecretShare() throws Exception {
        mockMvc.perform(post("/api/v1/secret-shares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(storePayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    @Order(2)
    void shouldRejectStoreWhenSecretShareAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/v1/secret-shares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(storePayload()))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    void shouldRetrieveSecretShareWhenPolicyIsValid() throws Exception {
        mockBalance(BigInteger.TEN);

        mockMvc.perform(post("/api/v1/secret-shares/{secretId}/retrieve", SECRET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRetrievePayload(SUCCESS_NONCE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secretId").value(SECRET_ID))
                .andExpect(jsonPath("$.nodeId").value("local-node-1"))
                .andExpect(jsonPath("$.protectedShare").exists())
                .andExpect(jsonPath("$.protectedShare").value(not("")))
                .andExpect(jsonPath("$.nodeSignature").exists())
                .andExpect(jsonPath("$.nodeSignature").value(not("")))
                .andExpect(jsonPath("$.deliveredAt").exists());
    }

    @Test
    @Order(4)
    void shouldRejectReplayWhenSameNonceIsUsedTwice() throws Exception {
        mockBalance(BigInteger.TEN);

        mockMvc.perform(post("/api/v1/secret-shares/{secretId}/retrieve", SECRET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRetrievePayload(SUCCESS_NONCE)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void shouldDenyRetrieveWhenPolicyValidatorRejectsAccess() throws Exception {
        mockBalance(BigInteger.ZERO);

        mockMvc.perform(post("/api/v1/secret-shares/{secretId}/retrieve", SECRET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRetrievePayload(POLICY_DENIED_NONCE)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    void shouldRejectRetrieveWhenWalletSignatureIsInvalid() throws Exception {
        reset(balanceReader);

        mockMvc.perform(post("/api/v1/secret-shares/{secretId}/retrieve", SECRET_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRetrievePayload(INVALID_SIGNATURE_NONCE)))
                .andExpect(status().isForbidden());
    }

    private void mockBalance(BigInteger balance) throws Exception {
        reset(balanceReader);

        when(balanceReader.balanceOf(
                eq(RPC_URL),
                eq(CONTRACT_ADDRESS),
                eq(USER_ADDRESS),
                eq(BigInteger.ONE)
        )).thenReturn(balance);
    }

    private String storePayload() {
        String publisherSignature = publisherSignature();

        return """
                {
                  "secretId": "%s",
                  "encryptedShare": "%s",
                  "policy": {
                    "type": "EVM_ERC1155_BALANCE",
                    "chainId": 31337,
                    "contractAddress": "%s",
                    "policyData": "{\\"tokenId\\":\\"1\\",\\"minBalance\\":\\"1\\"}"
                  },
                  "publisherAddress": "%s",
                  "publisherSignature": "%s"
                }
                """.formatted(
                SECRET_ID,
                ENCRYPTED_SHARE,
                CONTRACT_ADDRESS,
                PUBLISHER_ADDRESS,
                publisherSignature
        );
    }

    private String validRetrievePayload(String nonce) {
        String walletSignature = retrieveSignature(
                USER_ADDRESS,
                USER_PRIVATE_KEY,
                nonce
        );

        return retrievePayload(
                USER_ADDRESS,
                walletSignature,
                READER_PUBLIC_KEY,
                nonce
        );
    }

    private String invalidRetrievePayload(String nonce) {
        String walletSignature = retrieveSignature(
                PUBLISHER_ADDRESS,
                PUBLISHER_PRIVATE_KEY,
                nonce
        );

        return retrievePayload(
                USER_ADDRESS,
                walletSignature,
                READER_PUBLIC_KEY,
                nonce
        );
    }

    private String retrievePayload(
            String userAddress,
            String walletSignature,
            String readerPublicKey,
            String nonce
    ) {
        return """
                {
                  "userAddress": "%s",
                  "walletSignature": "%s",
                  "readerPublicKey": "%s",
                  "nonce": "%s"
                }
                """.formatted(
                userAddress,
                walletSignature,
                readerPublicKey,
                nonce
        );
    }

    private String publisherSignature() {
        AccessPolicy accessPolicy = accessPolicy();

        String message = new StoreSecretShareSignatureChallenge(
                SECRET_ID,
                PUBLISHER_ADDRESS,
                hash(ENCRYPTED_SHARE),
                hash(accessPolicy.getCanonical())
        ).message();

        return signPersonalMessage(message, PUBLISHER_PRIVATE_KEY);
    }

    private String retrieveSignature(
            String userAddress,
            String privateKey,
            String nonce
    ) {
        String message = new RetrieveSecretShareSignatureChallenge(
                SECRET_ID,
                userAddress,
                READER_PUBLIC_KEY,
                nonce
        ).message();

        return signPersonalMessage(message, privateKey);
    }

    private AccessPolicy accessPolicy() {
        return new AccessPolicy(
                PolicyType.EVM_ERC1155_BALANCE,
                31337L,
                CONTRACT_ADDRESS,
                POLICY_DATA
        );
    }

    private String hash(String value) {
        return Hash.sha3(Numeric.toHexString(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String signPersonalMessage(String message, String privateKey) {
        Sign.SignatureData signature = Sign.signPrefixedMessage(
                message.getBytes(StandardCharsets.UTF_8),
                Credentials.create(privateKey).getEcKeyPair()
        );

        return toHexSignature(signature);
    }

    private String toHexSignature(Sign.SignatureData signature) {
        byte[] value = new byte[65];

        System.arraycopy(signature.getR(), 0, value, 0, 32);
        System.arraycopy(signature.getS(), 0, value, 32, 32);
        value[64] = signature.getV()[0];

        return Numeric.toHexString(value);
    }
}