package io.custos.node.adapters.out.security;

import io.custos.node.core.application.exception.InvalidPublisherSignatureException;
import io.custos.node.core.application.exception.InvalidWalletSignatureException;
import io.custos.node.core.application.port.in.command.StoreSecretShareCommand;
import io.custos.node.core.application.port.out.PublisherSignatureVerifier;
import io.custos.node.core.domain.StoreSecretShareSignatureChallenge;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Hash;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;

import static io.custos.node.core.application.exception.errorcode.WalletSignatureErrorCode.INVALID_USER_ADDRESS;
import static io.custos.node.core.application.exception.errorcode.WalletSignatureErrorCode.INVALID_WALLET_SIGNATURE;

@Service
public class EvmPublisherSignVerifier implements PublisherSignatureVerifier {

    private final EvmPersonalSignAddressRecoverer evmPersonalSignAddressRecoverer;

    public EvmPublisherSignVerifier(EvmPersonalSignAddressRecoverer evmPersonalSignAddressRecoverer) {
        this.evmPersonalSignAddressRecoverer = evmPersonalSignAddressRecoverer;
    }

    @Override
    public void verifyStoreSecretSignature(StoreSecretShareCommand command) {
        if (!WalletUtils.isValidAddress(command.publisherAddress())) {
            throw new InvalidPublisherSignatureException(
                    INVALID_USER_ADDRESS,
                    "Invalid user address"
            );
        }

        String message = new StoreSecretShareSignatureChallenge(
                command.secretId(),
                command.publisherAddress(),
                this.hash(command.encryptedShare()),
                this.hash(command.accessPolicy().getCanonical())
        ).message();

        String recoveredAddress = evmPersonalSignAddressRecoverer.recoverAddress(message, command.publisherSignature());

        if (!recoveredAddress.equalsIgnoreCase(command.publisherAddress())) {
            throw new InvalidWalletSignatureException(
                    INVALID_WALLET_SIGNATURE,
                    "Wallet signature does not match publisher address"
            );
        }
    }

    private String hash(String toHash) {
        return Hash.sha3(Numeric.toHexString(toHash.getBytes(StandardCharsets.UTF_8)));
    }
}
