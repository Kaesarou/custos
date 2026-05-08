package io.custos.node.adapters.out.security;

import io.custos.node.core.application.exception.InvalidWalletSignatureException;
import io.custos.node.core.application.port.out.WalletSignatureVerifier;
import io.custos.node.core.domain.RetrieveSecretShareSignatureChallenge;
import org.springframework.stereotype.Service;
import org.web3j.crypto.WalletUtils;

import static io.custos.node.core.application.exception.errorcode.WalletSignatureErrorCode.*;

@Service
public class EvmPersonalSignVerifier implements WalletSignatureVerifier {

    private final EvmPersonalSignAddressRecoverer evmPersonalSignAddressRecoverer;

    public EvmPersonalSignVerifier(EvmPersonalSignAddressRecoverer evmPersonalSignAddressRecoverer) {
        this.evmPersonalSignAddressRecoverer = evmPersonalSignAddressRecoverer;
    }

    public void verifyRetrieveSecretSignature(
            String secretId,
            String userAddress,
            String nonce,
            String walletSignature
    ) {
        validateInputs(userAddress, nonce, walletSignature);

        String message = new RetrieveSecretShareSignatureChallenge(
                secretId,
                userAddress,
                nonce
        ).message();

        String recoveredAddress = evmPersonalSignAddressRecoverer.recoverAddress(message, walletSignature);

        if (!recoveredAddress.equalsIgnoreCase(userAddress)) {
            throw new InvalidWalletSignatureException(
                    INVALID_WALLET_SIGNATURE,
                    "Wallet signature does not match user address"
            );
        }
    }

    private void validateInputs(String userAddress, String nonce, String walletSignature) {
        if (userAddress == null || !WalletUtils.isValidAddress(userAddress)) {
            throw new InvalidWalletSignatureException(
                    INVALID_USER_ADDRESS,
                    "Invalid user address"
            );
        }

        if (nonce == null || nonce.isBlank()) {
            throw new InvalidWalletSignatureException(
                    MISSING_NONCE,
                    "Nonce is required"
            );
        }

        if (walletSignature == null || walletSignature.isBlank()) {
            throw new InvalidWalletSignatureException(
                    MISSING_WALLET_SIGNATURE,
                    "Wallet signature is required"
            );
        }
    }
}