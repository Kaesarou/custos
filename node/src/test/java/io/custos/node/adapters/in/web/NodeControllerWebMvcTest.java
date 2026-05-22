package io.custos.node.adapters.in.web;

import io.custos.node.core.application.port.in.GetNodeCapabilitiesUseCase;
import io.custos.node.core.application.port.in.GetNodeIdentityUseCase;
import io.custos.node.core.application.port.in.GetNodeStatusUseCase;
import io.custos.node.core.domain.ShareProtectionAlgorithm;
import io.custos.node.core.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NodeController.class)
class NodeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetNodeIdentityUseCase getNodeIdentityUseCase;

    @MockBean
    private GetNodeStatusUseCase getNodeStatusUseCase;

    @MockBean
    private GetNodeCapabilitiesUseCase getNodeCapabilitiesUseCase;

    @Test
    void shouldReturnNodeIdentity() throws Exception {
        when(getNodeIdentityUseCase.getNodeIdentity()).thenReturn(
                new NodeIdentity(
                        "local-node-1",
                        "0x90f79bf6eb2c4f870365e785982e1f101e93b906",
                        "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC",
                        NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN
                )
        );

        mockMvc.perform(get("/api/v1/node/id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId").value("local-node-1"))
                .andExpect(jsonPath("$.nodeAddress").value("0x90f79bf6eb2c4f870365e785982e1f101e93b906"))
                .andExpect(jsonPath("$.rewardAddress").value("0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC"))
                .andExpect(jsonPath("$.signatureAlgorithm").value(NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN));
    }

    @Test
    void shouldReturnNodeStatus() throws Exception {
        when(getNodeStatusUseCase.getNodeStatus()).thenReturn(
                new NodeStatus(
                        "local-node-1",
                        "UP",
                        Instant.parse("2026-05-22T10:00:00Z"),
                        Instant.parse("2026-05-22T10:15:00Z"),
                        900
                )
        );

        mockMvc.perform(get("/api/v1/node/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId").value("local-node-1"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.startedAt").value("2026-05-22T10:00:00Z"))
                .andExpect(jsonPath("$.currentTime").value("2026-05-22T10:15:00Z"))
                .andExpect(jsonPath("$.uptimeSeconds").value(900));
    }

    @Test
    void shouldReturnNodeCapabilities() throws Exception {
        when(getNodeCapabilitiesUseCase.getNodeCapabilities()).thenReturn(
                new NodeCapabilities(
                        "local-node-1",
                        List.of(PolicyType.EVM_ERC1155_BALANCE),
                        List.of(ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM),
                        NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN,
                        List.of(new NodeCapabilities.SupportedChain(31337L))
                )
        );

        mockMvc.perform(get("/api/v1/node/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId").value("local-node-1"))
                .andExpect(jsonPath("$.supportedPolicyTypes[0]").value("EVM_ERC1155_BALANCE"))
                .andExpect(jsonPath("$.supportedShareProtectionAlgorithms[0]").value("X25519-HKDF-SHA256-AES-256-GCM"))
                .andExpect(jsonPath("$.signatureAlgorithm").value(NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN))
                .andExpect(jsonPath("$.supportedChains[0].chainId").value(31337));
    }
}