package io.custos.node.adapters.out.security;

import io.custos.node.core.application.exception.EvmSignatureRecoveryException;
import io.custos.node.core.domain.RetrieveSecretShareSignatureChallenge;
import io.custos.node.core.domain.StoreSecretShareSignatureChallenge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvmPersonalSignAddressRecovererTest {

    private final EvmPersonalSignAddressRecoverer recoverer = new EvmPersonalSignAddressRecoverer();
    @Test
    void shouldRecoverSignerAddressFromValidPersonalSignSignature() {

        String message = new RetrieveSecretShareSignatureChallenge(
                "1",
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "test-nonce-1234"
        ).message();

        String recoveredAddress = recoverer.recoverAddress(
                message,
                "0x825415a329279b10c39560d83fca7aeb2f93e311bb1478c5d6560767dbc5b1496735e96af0c5b1ac844e792dd54a226496725fe1fc41630e17b670978eb875fe1b"
        );

        Assertions.assertEquals(
                "0x70997970c51812dc3a010c7d01b50e0d17dc79c8",
                recoveredAddress.toLowerCase()
        );
    }

    @Test
    void shouldRecoverPublisherAddressFromValidStoreSecretShareSignature() {
        String encryptedShare = "encrypted-share";
        String canonicalPolicy =
                "EVM_ERC1155_BALANCE|31337|0xe7f1725e7734ce288f8367e1bb143e90bb3f0512|{\"tokenId\":\"1\",\"minBalance\":\"1\"}";

        String message = new StoreSecretShareSignatureChallenge(
                "1",
                "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC",
                hash(encryptedShare),
                hash(canonicalPolicy)
        ).message();

        String publisherSignature = "0xda63e0b40990ee76ea8d76a5707f6014077826b581f3a387110310cee9c71a3073da1c1576e16ab64154e46afc35f804e8487950153eaf15be461d38c75e6b571c";

        String recoveredAddress = recoverer.recoverAddress(message, publisherSignature);

        assertEquals(
                "0x3c44cdddb6a900fa2b585dd299e03d12fa4293bc",
                recoveredAddress.toLowerCase()
        );
    }

    @Test
    void shouldRejectSignatureWithInvalidLength() {
        assertThrows(
                EvmSignatureRecoveryException.class,
                () -> recoverer.recoverAddress("message", "0x1234")
        );
    }

    private String hash(String value) {
        return Hash.sha3(Numeric.toHexString(value.getBytes(StandardCharsets.UTF_8)));
    }
}