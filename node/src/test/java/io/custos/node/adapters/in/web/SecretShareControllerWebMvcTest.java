package io.custos.node.adapters.in.web;

import io.custos.node.core.application.port.in.RetrieveSecretShareUseCase;
import io.custos.node.core.application.port.in.StoreSecretShareUseCase;
import io.custos.node.core.domain.model.ProtectedShare;
import io.custos.node.core.domain.model.SecretShareDelivery;
import io.custos.node.core.domain.model.ShareProtectionAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecretShareController.class)
class SecretShareControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreSecretShareUseCase storeSecretShareUseCase;

    @MockBean
    private RetrieveSecretShareUseCase retrieveSecretShareUseCase;

    @Test
    void shouldMapStoreRequestToStoreCommand() throws Exception {
        mockMvc.perform(post("/api/v1/secret-shares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "secretId": "1",
                                  "encryptedShare": "encrypted-share",
                                  "policy": {
                                    "type": "EVM_ERC1155_BALANCE",
                                    "chainId": 31337,
                                    "contractAddress": "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                                    "policyData": "{\\"tokenId\\":\\"1\\",\\"minBalance\\":\\"1\\"}"
                                  },
                                  "publisherAddress": "0xPublisher",
                                  "publisherSignature": "publisher-signature"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OK"));

        verify(storeSecretShareUseCase).store(argThat(command ->
                command.secretId().equals("1")
                        && command.encryptedShare().equals("encrypted-share")
                        && command.publisherAddress().equals("0xPublisher")
                        && command.publisherSignature().equals("publisher-signature")
                        && command.accessPolicy().chainId() == 31337L
        ));
    }

    @Test
    void shouldMapRetrieveRequestToRetrieveCommand() throws Exception {
        ProtectedShare protectedShare = new ProtectedShare(
                io.custos.node.core.domain.model.ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM,
                "ephemeral-public-key",
                "iv",
                "ciphertext"
        );

        when(retrieveSecretShareUseCase.retrieve(any())).thenReturn(
                new SecretShareDelivery(
                        "1",
                        "local-node-1",
                        protectedShare,
                        "node-signature",
                        Instant.parse("2026-05-04T10:15:30Z")
                )
        );

        mockMvc.perform(post("/api/v1/secret-shares/1/retrieve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userAddress": "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                                  "walletSignature": "0x2eaae67211205e5ad48847d3a64251b2ec1d0b3bedee679a468127aa842aa8400c4ef9939ec8c628fbe9d240255d411532baed6e76a2c2e99d92d166cb3cfbb71c",
                                  "readerPublicKey": "reader-public-key",
                                  "nonce": "test-nonce-1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secretId").value("1"))
                .andExpect(jsonPath("$.nodeId").value("local-node-1"))
                .andExpect(jsonPath("$.protectedShare.alg").value(ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM))
                .andExpect(jsonPath("$.protectedShare.ephemeralPublicKey").value("ephemeral-public-key"))
                .andExpect(jsonPath("$.protectedShare.iv").value("iv"))
                .andExpect(jsonPath("$.protectedShare.ciphertext").value("ciphertext"))
                .andExpect(jsonPath("$.nodeSignature").value("node-signature"))
                .andExpect(jsonPath("$.deliveredAt").value("2026-05-04T10:15:30Z"));

        verify(retrieveSecretShareUseCase).retrieve(argThat(command ->
                command.secretId().equals("1")
                        && command.userAddress().equals("0x70997970C51812dc3A010C7d01b50e0d17dc79C8")
                        && command.walletSignature().startsWith("0x2eaae")
                        && command.readerPublicKey().equals("reader-public-key")
                        && command.nonce().equals("test-nonce-1234")
        ));
    }
}