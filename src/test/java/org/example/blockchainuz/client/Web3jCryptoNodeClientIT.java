package org.example.blockchainuz.client;

import org.example.blockchainuz.client.dto.BlockResponse;
import org.example.blockchainuz.client.dto.TransactionResponse;
import org.example.blockchainuz.exception.CryptoNodeException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Tag("integration")
class Web3jCryptoNodeClientIT {

    @Autowired
    private CryptoNodeClient cryptoNodeClient;

    @Test
    void testConnectionIsSuccessful() {
        // Test that client can connect to the node
        assertTrue(cryptoNodeClient.isConnected(), "Client should be connected to the blockchain node");
    }

    @Test
    void testGetLatestBlockNumber() {
        // Test fetching latest block number
        BigInteger latestBlockNumber = cryptoNodeClient.getLatestBlockNumber();
        assertNotNull(latestBlockNumber, "Latest block number should not be null");
        assertTrue(latestBlockNumber.compareTo(BigInteger.ZERO) > 0, "Latest block number should be greater than 0");
    }

    @Test
    void testGetBlockByNumber() {
        // Test fetching a specific block by number
        // Block 1 exists on all Ethereum networks
        BigInteger blockNumber = BigInteger.ONE;
        BlockResponse block = cryptoNodeClient.getBlockByNumber(blockNumber);

        assertNotNull(block, "Block should not be null");
        assertEquals(blockNumber, block.getNumber(), "Block number should match");
        assertNotNull(block.getHash(), "Block hash should not be null");
        assertNotNull(block.getTimestamp(), "Block timestamp should not be null");
        assertNotNull(block.getParentHash(), "Parent hash should not be null");
    }

    @Test
    void testGetBlockByHash() {
        // First get a block by number to get its hash
        BigInteger blockNumber = BigInteger.ONE;
        BlockResponse block1 = cryptoNodeClient.getBlockByNumber(blockNumber);

        // Then fetch the same block by hash
        BlockResponse block2 = cryptoNodeClient.getBlockByHash(block1.getHash());

        assertNotNull(block2, "Block should not be null");
        assertEquals(block1.getHash(), block2.getHash(), "Block hashes should match");
        assertEquals(block1.getNumber(), block2.getNumber(), "Block numbers should match");
    }

    @Test
    void testGetBlockByNumberNotFound() {
        // Test fetching a block that doesn't exist (very high number)
        BigInteger futureBlockNumber = BigInteger.valueOf(Long.MAX_VALUE);

        assertThrows(CryptoNodeException.class, () -> {
            cryptoNodeClient.getBlockByNumber(futureBlockNumber);
        }, "Should throw CryptoNodeException for non-existent block");
    }

    @Test
    void testGetBalance() {
        // Test fetching balance for a well-known address
        // Using zero address (0x0000...0000) which should have 0 balance
        String zeroAddress = "0x0000000000000000000000000000000000000000";
        BigInteger balance = cryptoNodeClient.getBalance(zeroAddress);

        assertNotNull(balance, "Balance should not be null");
        // Zero address should have 0 balance
        assertEquals(BigInteger.ZERO, balance, "Zero address should have 0 balance");
    }

    @Test
    void testTransactionParsing() {
        // Get a block with transactions
        BigInteger latestBlockNumber = cryptoNodeClient.getLatestBlockNumber();

        // Iterate backwards to find a block with transactions
        BlockResponse blockWithTxs = null;
        for (int i = 0; i < 10; i++) {
            BigInteger blockNum = latestBlockNumber.subtract(BigInteger.valueOf(i));
            BlockResponse block = cryptoNodeClient.getBlockByNumber(blockNum);
            if (block.getTransactions() != null && !block.getTransactions().isEmpty()) {
                blockWithTxs = block;
                break;
            }
        }

        // If we found a block with transactions, verify transaction data
        if (blockWithTxs != null) {
            assertFalse(blockWithTxs.getTransactions().isEmpty(), "Block should have transactions");

            TransactionResponse tx = blockWithTxs.getTransactions().get(0);
            assertNotNull(tx.getHash(), "Transaction hash should not be null");
            assertNotNull(tx.getFromAddress(), "From address should not be null");
            assertNotNull(tx.getValue(), "Value should not be null");
            assertEquals(blockWithTxs.getNumber(), tx.getBlockNumber(), "Block number should match");
        }
    }
}
