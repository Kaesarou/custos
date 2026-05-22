package io.custos.node.adapters.out.security;

import io.custos.node.config.CustosProperties;
import io.custos.node.core.application.port.out.NodeSignatureService;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;

@Service
public class LocalNodeSignatureService implements NodeSignatureService {

    private final Credentials nodeCredentials;

    public LocalNodeSignatureService(CustosProperties custosProperties) {
        this.nodeCredentials = Credentials.create(custosProperties.node().privateKey());
    }

    @Override
    public String sign(String payload) {
        byte[] messageBytes = payload.getBytes(StandardCharsets.UTF_8);
        byte[] messageHash = Sign.getEthereumMessageHash(messageBytes);

        Sign.SignatureData signature = Sign.signMessage(
                messageHash,
                nodeCredentials.getEcKeyPair(),
                false
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