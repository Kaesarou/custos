package io.custos.node.adapters.out.security;

import io.custos.node.core.application.exception.EvmSignatureRecoveryException;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class EvmPersonalSignAddressRecoverer {

    private static final String EVM_ADDRESS_HEX_PREFIX = "0x";

    /**
     * Ethereum ECDSA signatures are encoded as:
     * r: 32 bytes
     * s: 32 bytes
     * v: 1 byte
     * <p>
     * Total = 65 bytes.
     */
    private static final int ECDSA_SIGNATURE_LENGTH_BYTES = 65;

    private static final int ECDSA_R_COMPONENT_START_INDEX = 0;
    private static final int ECDSA_R_COMPONENT_END_INDEX_EXCLUSIVE = 32;

    private static final int ECDSA_S_COMPONENT_START_INDEX = 32;
    private static final int ECDSA_S_COMPONENT_END_INDEX_EXCLUSIVE = 64;

    private static final int ECDSA_V_COMPONENT_INDEX = 64;

    /**
     * Ethereum signatures usually encode the recovery id as 27/28.
     * Some libraries return it as 0/1, so we normalize by adding 27.
     */
    private static final int ETHEREUM_RECOVERY_ID_OFFSET = 27;

    public String recoverAddress(String message, String signature) {
        try {
            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);

            if (signatureBytes.length != ECDSA_SIGNATURE_LENGTH_BYTES) {
                throw new EvmSignatureRecoveryException("Invalid signature length");
            }

            byte recoveryId = signatureBytes[ECDSA_V_COMPONENT_INDEX];

            if (recoveryId < ETHEREUM_RECOVERY_ID_OFFSET) {
                recoveryId += ETHEREUM_RECOVERY_ID_OFFSET;
            }

            byte[] r = Arrays.copyOfRange(
                    signatureBytes,
                    ECDSA_R_COMPONENT_START_INDEX,
                    ECDSA_R_COMPONENT_END_INDEX_EXCLUSIVE
            );

            byte[] s = Arrays.copyOfRange(
                    signatureBytes,
                    ECDSA_S_COMPONENT_START_INDEX,
                    ECDSA_S_COMPONENT_END_INDEX_EXCLUSIVE
            );

            Sign.SignatureData signatureData = new Sign.SignatureData(recoveryId, r, s);

            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

            byte[] prefixedMessageHash = Sign.getEthereumMessageHash(messageBytes);

            BigInteger publicKey = Sign.signedMessageHashToKey(
                    prefixedMessageHash,
                    signatureData
            );

            return EVM_ADDRESS_HEX_PREFIX + Keys.getAddress(publicKey);

        } catch (EvmSignatureRecoveryException e) {
            throw e;
        } catch (Exception e) {
            throw new EvmSignatureRecoveryException("Unable to recover signer address", e);
        }
    }
}