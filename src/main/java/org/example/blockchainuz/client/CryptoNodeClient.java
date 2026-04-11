package org.example.blockchainuz.client;

import org.example.blockchainuz.client.dto.BlockResponse;
import org.example.blockchainuz.client.dto.TransactionResponse;

import java.math.BigInteger;

/**
 * Interface for blockchain node interaction.
 * Allows swapping provider implementations without changing business logic.
 */
public interface CryptoNodeClient {

    /**
     * Get block by block number
     * @param blockNumber the block number
     * @return block data
     */
    BlockResponse getBlockByNumber(BigInteger blockNumber);

    /**
     * Get block by hash
     * @param hash the block hash
     * @return block data
     */
    BlockResponse getBlockByHash(String hash);

    /**
     * Get the latest block number
     * @return the latest block number
     */
    BigInteger getLatestBlockNumber();

    /**
     * Get transaction by hash
     * @param hash the transaction hash
     * @return transaction data
     */
    TransactionResponse getTransactionByHash(String hash);

    /**
     * Get balance of an address
     * @param address the wallet address
     * @return balance in wei
     */
    BigInteger getBalance(String address);

    /**
     * Check if the client is connected to the node
     * @return true if connected
     */
    boolean isConnected();
}
