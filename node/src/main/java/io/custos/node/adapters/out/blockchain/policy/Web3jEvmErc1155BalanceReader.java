package io.custos.node.adapters.out.blockchain.policy;

import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.List;

@Service
public class Web3jEvmErc1155BalanceReader implements EvmErc1155BalanceReader {

    @Override
    public BigInteger balanceOf(
            String rpcUrl,
            String contractAddress,
            String walletAddress,
            BigInteger tokenId
    ) {
        try {
            Web3j web3j = Web3j.build(new HttpService(rpcUrl));

            Function function = new Function(
                    "balanceOf",
                    List.of(
                            new Address(walletAddress),
                            new Uint256(tokenId)
                    ),
                    List.of(new TypeReference<Uint256>() {
                    })
            );

            String encodedFunction = FunctionEncoder.encode(function);

            Transaction transaction = Transaction.createEthCallTransaction(
                    walletAddress,
                    contractAddress,
                    encodedFunction
            );

            var response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();

            if (response.hasError()) {
                throw new IllegalStateException(response.getError().getMessage());
            }

            String value = response.getValue();

            if (value == null || value.equals("0x")) {
                throw new IllegalStateException("Empty response from contract");
            }

            var decoded = FunctionReturnDecoder.decode(value, function.getOutputParameters());

            if (decoded.isEmpty()) {
                throw new IllegalStateException("Unable to decode balanceOf response");
            }

            return (BigInteger) decoded.getFirst().getValue();

        } catch (Exception e) {
            throw new IllegalStateException("Unable to read ERC1155 balance", e);
        }
    }
}