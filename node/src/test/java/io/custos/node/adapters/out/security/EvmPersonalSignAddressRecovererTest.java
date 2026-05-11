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

    private static final String ALICE_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";

    private static final String BOB_ADDRESS = "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC";

    private static final String READER_PUBLIC_KEY = "y5VMaQ_llLbDlKwKwV0au2VWPiijb125n_fvOSoS61o";

    private static final String VALID_RETRIEVE_SIGNATURE_WITH_READER_PUBLIC_KEY =
            "0x22965675a0fc18c4f9b7ac04b6d4621ab690be18c00d85018162a0d36a0a0fd849b0a56467e8cdb6bbb178ae227458e25a3aeb3e52a0fffe277b142931f968211c";

    private static final String VALID_STORE_SIGNATURE =
            "0xda63e0b40990ee76ea8d76a5707f6014077826b581f3a387110310cee9c71a3073da1c1576e16ab64154e46afc35f804e8487950153eaf15be461d38c75e6b571c";

    private final EvmPersonalSignAddressRecoverer recoverer = new EvmPersonalSignAddressRecoverer();

    @Test
    void shouldRecoverSignerAddressFromValidPersonalSignSignature() {
        String message = new RetrieveSecretShareSignatureChallenge(
                "1",
                ALICE_ADDRESS,
                READER_PUBLIC_KEY,
                "test-nonce-1234"
        ).message();

        String recoveredAddress = recoverer.recoverAddress(
                message,
                VALID_RETRIEVE_SIGNATURE_WITH_READER_PUBLIC_KEY
        );

        Assertions.assertEquals(
                ALICE_ADDRESS.toLowerCase(),
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
                BOB_ADDRESS,
                hash(encryptedShare),
                hash(canonicalPolicy)
        ).message();

        String recoveredAddress = recoverer.recoverAddress(message, VALID_STORE_SIGNATURE);

        assertEquals(
                BOB_ADDRESS.toLowerCase(),
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