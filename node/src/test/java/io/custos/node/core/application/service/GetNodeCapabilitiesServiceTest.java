package io.custos.node.core.application.service;

import io.custos.node.core.application.port.out.NodeCapabilitiesProvider;
import io.custos.node.core.domain.ShareProtectionAlgorithm;
import io.custos.node.core.domain.model.NodeCapabilities;
import io.custos.node.core.domain.model.NodeSignatureAlgorithm;
import io.custos.node.core.domain.model.PolicyType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GetNodeCapabilitiesServiceTest {

    @Test
    void shouldReturnNodeCapabilitiesFromProvider() {
        NodeCapabilitiesProvider provider = mock(NodeCapabilitiesProvider.class);

        NodeCapabilities expected = new NodeCapabilities(
                "local-node-1",
                List.of(PolicyType.EVM_ERC1155_BALANCE),
                List.of(ShareProtectionAlgorithm.X25519_HKDF_SHA256_AES_256_GCM),
                NodeSignatureAlgorithm.ECDSA_SECP256K1_PERSONAL_SIGN,
                List.of(new NodeCapabilities.SupportedChain(31337L))
        );

        when(provider.getNodeCapabilities()).thenReturn(expected);

        GetNodeCapabilitiesService service = new GetNodeCapabilitiesService(provider);

        NodeCapabilities result = service.getNodeCapabilities();

        assertEquals(expected, result);
        verify(provider).getNodeCapabilities();
    }
}