package io.custos.node.adapters.out.security;

import io.custos.node.core.application.port.out.NodeSignatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "custos.node.id=local-node-1",
        "custos.node.private-key=0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80",
        "custos.node.reward-address="
})
class LocalNodeSignatureServiceTest {

    @Autowired
    private NodeSignatureService nodeSignatureService;

    @Test
    void shouldSignPayloadWithNodePrivateKey() {
        String signature = nodeSignatureService.sign("payload-to-sign");

        String recoveredAddress = new EvmPersonalSignAddressRecoverer()
                .recoverAddress("payload-to-sign", signature);

        assertEquals(
                "0xf39fd6e51aad88f6f4ce6ab8827279cfffb92266",
                recoveredAddress.toLowerCase()
        );
    }
}