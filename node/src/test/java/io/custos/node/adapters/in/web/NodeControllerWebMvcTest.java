package io.custos.node.adapters.in.web;

import io.custos.node.core.application.port.in.GetNodeIdentityUseCase;
import io.custos.node.core.domain.model.NodeIdentity;
import io.custos.node.core.domain.model.NodeSignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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
}