package io.custos.node.adapters.in.web;

import io.custos.node.core.application.port.in.GetLocalNetworkViewUseCase;
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

@WebMvcTest(NetworkController.class)
class NetworkControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetLocalNetworkViewUseCase getLocalNetworkViewUseCase;

    @Test
    void shouldReturnLocalNetworkView() throws Exception {
        when(getLocalNetworkViewUseCase.getLocalNetworkView()).thenReturn(
                new LocalNetworkView(
                        "local-node-1",
                        List.of(
                                PeerNodeView.reachable(
                                        "http://localhost:8082",
                                        new NodeIdentity(
                                                "local-node-2",
                                                "0x0000000000000000000000000000000000000001",
                                                "0x0000000000000000000000000000000000000001",
                                                "http://localhost:8080",
                                                "ECDSA_SECP256K1_PERSONAL_SIGN"
                                        ),
                                        new NodeStatus(
                                                "local-node-2",
                                                "UP",
                                                Instant.parse("2026-05-22T10:00:00Z"),
                                                Instant.parse("2026-05-22T10:15:00Z"),
                                                900
                                        ),
                                        new NodeCapabilities(
                                                "local-node-2",
                                                List.of(PolicyType.EVM_ERC1155_BALANCE),
                                                List.of(ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM),
                                                "ECDSA_SECP256K1_PERSONAL_SIGN",
                                                List.of(new NodeCapabilities.SupportedChain(31337L))
                                        )
                                ),
                                PeerNodeView.unreachable(
                                        "http://localhost:8083",
                                        "PEER_UNREACHABLE"
                                )
                        )
                )
        );

        mockMvc.perform(get("/api/v1/network/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observerNodeId").value("local-node-1"))
                .andExpect(jsonPath("$.peers[0].baseUrl").value("http://localhost:8082"))
                .andExpect(jsonPath("$.peers[0].reachable").value(true))
                .andExpect(jsonPath("$.peers[0].identity.nodeId").value("local-node-2"))
                .andExpect(jsonPath("$.peers[0].status.status").value("UP"))
                .andExpect(jsonPath("$.peers[0].capabilities.supportedPolicyTypes[0]").value("EVM_ERC1155_BALANCE"))
                .andExpect(jsonPath("$.peers[1].baseUrl").value("http://localhost:8083"))
                .andExpect(jsonPath("$.peers[1].reachable").value(false))
                .andExpect(jsonPath("$.peers[1].failureReason").value("PEER_UNREACHABLE"));
    }
}