package io.custos.node.adapters.out.blockchain.policy;

import java.math.BigInteger;

public interface EvmErc1155BalanceReader {

    BigInteger balanceOf(
            String rpcUrl,
            String contractAddress,
            String walletAddress,
            BigInteger tokenId
    );
}