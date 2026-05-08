package io.custos.node.adapters.out.security;

import io.custos.node.core.application.exception.InvalidPublisherSignatureException;
import io.custos.node.core.application.exception.InvalidWalletSignatureException;
import io.custos.node.core.application.port.in.command.StoreSecretShareCommand;
import io.custos.node.core.domain.model.AccessPolicy;
import io.custos.node.core.domain.model.PolicyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EvmPublisherSignVerifierTest {

    private static final String BOB_ADDRESS =
            "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC";

    private static final String VALID_BOB_STORE_SIGNATURE =
            "0xda63e0b40990ee76ea8d76a5707f6014077826b581f3a387110310cee9c71a3073da1c1576e16ab64154e46afc35f804e8487950153eaf15be461d38c75e6b571c";

    private final EvmPublisherSignVerifier verifier =
            new EvmPublisherSignVerifier(new EvmPersonalSignAddressRecoverer());

    @Test
    void shouldAcceptValidPublisherSignature() {
        verifier.verifyStoreSecretSignature(validCommand());
    }

    @Test
    void shouldRejectInvalidPublisherAddress() {
        StoreSecretShareCommand command = new StoreSecretShareCommand(
                "1",
                "encrypted-share",
                validPolicy(),
                "invalid-address",
                VALID_BOB_STORE_SIGNATURE
        );

        assertThrows(
                InvalidPublisherSignatureException.class,
                () -> verifier.verifyStoreSecretSignature(command)
        );
    }

    @Test
    void shouldRejectSignatureWhenPublisherAddressDoesNotMatchRecoveredAddress() {
        StoreSecretShareCommand command = new StoreSecretShareCommand(
                "1",
                "encrypted-share",
                validPolicy(),
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                VALID_BOB_STORE_SIGNATURE
        );

        assertThrows(
                InvalidPublisherSignatureException.class,
                () -> verifier.verifyStoreSecretSignature(command)
        );
    }

    @Test
    void shouldRejectSignatureWhenEncryptedShareIsModified() {
        StoreSecretShareCommand command = new StoreSecretShareCommand(
                "1",
                "tampered-encrypted-share",
                validPolicy(),
                BOB_ADDRESS,
                VALID_BOB_STORE_SIGNATURE
        );

        assertThrows(
                InvalidPublisherSignatureException.class,
                () -> verifier.verifyStoreSecretSignature(command)
        );
    }

    @Test
    void shouldRejectSignatureWhenPolicyDataIsModified() {
        AccessPolicy modifiedPolicy = new AccessPolicy(
                PolicyType.EVM_ERC1155_BALANCE,
                31337L,
                "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                "{\"tokenId\":\"2\",\"minBalance\":\"1\"}"
        );

        StoreSecretShareCommand command = new StoreSecretShareCommand(
                "1",
                "encrypted-share",
                modifiedPolicy,
                BOB_ADDRESS,
                VALID_BOB_STORE_SIGNATURE
        );

        assertThrows(
                InvalidPublisherSignatureException.class,
                () -> verifier.verifyStoreSecretSignature(command)
        );
    }

    @Test
    void shouldRejectSignatureWhenSecretIdIsModified() {
        StoreSecretShareCommand command = new StoreSecretShareCommand(
                "2",
                "encrypted-share",
                validPolicy(),
                BOB_ADDRESS,
                VALID_BOB_STORE_SIGNATURE
        );

        assertThrows(
                InvalidPublisherSignatureException.class,
                () -> verifier.verifyStoreSecretSignature(command)
        );
    }

    private StoreSecretShareCommand validCommand() {
        return new StoreSecretShareCommand(
                "1",
                "encrypted-share",
                validPolicy(),
                BOB_ADDRESS,
                VALID_BOB_STORE_SIGNATURE
        );
    }

    private AccessPolicy validPolicy() {
        return new AccessPolicy(
                PolicyType.EVM_ERC1155_BALANCE,
                31337L,
                "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                "{\"tokenId\":\"1\",\"minBalance\":\"1\"}"
        );
    }
}