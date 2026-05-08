package io.custos.node.adapters.in.web;

import io.custos.node.EmbeddedPostgresConfiguration;
import io.custos.node.adapters.out.blockchain.policy.EvmErc1155BalanceReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;

import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "custos.node-id=local-node-1",
        "custos.api.base-path=/api/v1",
        "custos.chains.31337.rpc-url=http://localhost:8545",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(EmbeddedPostgresConfiguration.class)
class SecretShareControllerIT {

    private static final String USER_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";
    private static final String CONTRACT_ADDRESS = "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512";
    private static final String RPC_URL = "http://localhost:8545";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvmErc1155BalanceReader balanceReader;

    @Test
    void shouldStoreThenRetrieveSecretShareWhenPolicyIsValid() throws Exception {
        when(balanceReader.balanceOf(
                eq(RPC_URL),
                eq(CONTRACT_ADDRESS),
                eq(USER_ADDRESS),
                eq(BigInteger.ONE)
        )).thenReturn(BigInteger.TEN);

        mockMvc.perform(post("/api/v1/secret-shares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(storePayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OK"));

        mockMvc.perform(post("/api/v1/secret-shares/1/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRetrievePayload("test-nonce-1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secretId").value("1"))
                .andExpect(jsonPath("$.nodeId").value("local-node-1"))
                .andExpect(jsonPath("$.protectedShare").exists())
                .andExpect(jsonPath("$.protectedShare").value(not("")))
                .andExpect(jsonPath("$.nodeSignature").exists())
                .andExpect(jsonPath("$.nodeSignature").value(not("")))
                .andExpect(jsonPath("$.deliveredAt").exists());
    }

    @Test
    void shouldRejectReplayWhenSameNonceIsUsedTwice() throws Exception {
        when(balanceReader.balanceOf(
                eq(RPC_URL),
                eq(CONTRACT_ADDRESS),
                eq(USER_ADDRESS),
                eq(BigInteger.ONE)
        )).thenReturn(BigInteger.TEN);

        mockMvc.perform(post("/api/v1/secret-shares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(storePayload()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/secret-shares/1/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRetrievePayload("test-nonce-1234")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldDenyRetrieveWhenPolicyValidatorRejectsAccess() throws Exception {
        when(balanceReader.balanceOf(
                eq(RPC_URL),
                eq(CONTRACT_ADDRESS),
                eq(USER_ADDRESS),
                eq(BigInteger.ONE)
        )).thenReturn(BigInteger.ZERO);

        mockMvc.perform(post("/api/v1/secret-shares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(storePayload()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/secret-shares/1/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRetrievePayload("test-nonce-1234")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectRetrieveWhenWalletSignatureIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/secret-shares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(storePayload()))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/secret-shares/1/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userAddress": "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC",
                                  "walletSignature": "0x825415a329279b10c39560d83fca7aeb2f93e311bb1478c5d6560767dbc5b1496735e96af0c5b1ac844e792dd54a226496725fe1fc41630e17b670978eb875fe1b",
                                  "readerPublicKey": "0xREADER_PUBLIC_KEY_TEST",
                                  "nonce": "test-nonce-1234"
                                }
                                """))
                .andExpect(status().is4xxClientError());
    }

    private String storePayload() {
        return """
                {
                  "secretId": "1",
                  "encryptedShare": "encrypted-share",
                  "policy": {
                    "type": "EVM_ERC1155_BALANCE",
                    "chainId": 31337,
                    "contractAddress": "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                    "policyData": "{\\"tokenId\\":\\"1\\",\\"minBalance\\":\\"1\\"}"
                  },
                  "publisherAddress": "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC",
                  "publisherSignature": "0xda63e0b40990ee76ea8d76a5707f6014077826b581f3a387110310cee9c71a3073da1c1576e16ab64154e46afc35f804e8487950153eaf15be461d38c75e6b571c"
                }
                """;
    }

    private String validRetrievePayload(String nonce) {
        return """
                {
                  "userAddress": "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                  "walletSignature": "0x825415a329279b10c39560d83fca7aeb2f93e311bb1478c5d6560767dbc5b1496735e96af0c5b1ac844e792dd54a226496725fe1fc41630e17b670978eb875fe1b",
                  "readerPublicKey": "0xREADER_PUBLIC_KEY_TEST",
                  "nonce": "%s"
                }
                """.formatted(nonce);
    }
}