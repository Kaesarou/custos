package io.custos.node.adapters.out.security;

import io.custos.node.core.domain.RetrieveSecretShareSignatureChallenge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EvmPersonalSignAddressRecovererTest {
    @Test
    void shouldRecoverSignerAddressFromValidPersonalSignSignature() {
        var recoverer = new EvmPersonalSignAddressRecoverer();

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
}