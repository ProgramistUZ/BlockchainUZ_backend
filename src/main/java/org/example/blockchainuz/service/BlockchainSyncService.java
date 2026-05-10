package org.example.blockchainuz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.blockchainuz.client.CryptoNodeClient;
import org.example.blockchainuz.client.dto.BlockResponse;
import org.example.blockchainuz.client.dto.TransactionResponse;
import org.example.blockchainuz.entity.Block;
import org.example.blockchainuz.entity.Transaction;
import org.example.blockchainuz.entity.TransactionStatus;
import org.example.blockchainuz.repository.BlockRepository;
import org.example.blockchainuz.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for syncing blockchain data from the node to the database
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainSyncService {

    private final CryptoNodeClient cryptoNodeClient;
    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    @Value("${blockchain.sync.enabled:true}")
    private boolean syncEnabled;

    @Value("${blockchain.sync.batch-size:10}")
    private int batchSize;

    @Value("${blockchain.sync.start-block:0}")
    private long startBlock;

    /**
     * Scheduled sync job that runs every minute
     * Fixed delay ensures previous execution completes before next starts
     */
    @Scheduled(fixedDelayString = "${blockchain.sync.interval:60000}", initialDelay = 10000)
    @Transactional
    public void syncNewBlocks() {
        if (!syncEnabled) {
            log.debug("Blockchain sync is disabled");
            return;
        }

        try {
            log.info("Starting blockchain sync...");

            // Get latest block from blockchain
            BigInteger latestBlockNumber = cryptoNodeClient.getLatestBlockNumber();

            // Get latest synced block from database
            BigInteger lastSyncedBlock = blockRepository.findTopByOrderByNumberDesc()
                    .map(b -> BigInteger.valueOf(b.getNumber()))
                    .orElse(BigInteger.valueOf(startBlock - 1));

            log.info("Latest blockchain block: {}, Last synced block: {}", latestBlockNumber, lastSyncedBlock);

            // Calculate blocks to sync
            BigInteger blocksToSync = latestBlockNumber.subtract(lastSyncedBlock);

            if (blocksToSync.compareTo(BigInteger.ZERO) <= 0) {
                log.info("No new blocks to sync");
                return;
            }

            // Limit the number of blocks to sync in one batch
            BigInteger blocksInThisBatch = blocksToSync.min(BigInteger.valueOf(batchSize));

            log.info("Syncing {} blocks", blocksInThisBatch);

            // Sync blocks
            for (BigInteger i = BigInteger.ZERO; i.compareTo(blocksInThisBatch) < 0; i = i.add(BigInteger.ONE)) {
                BigInteger blockNumber = lastSyncedBlock.add(i).add(BigInteger.ONE);
                syncBlock(blockNumber);
            }

            log.info("Blockchain sync completed. Synced {} blocks", blocksInThisBatch);

        } catch (Exception e) {
            log.error("Error during blockchain sync", e);
        }
    }

    /**
     * Sync a single block and its transactions
     */
    @Transactional
    public void syncBlock(BigInteger blockNumber) {
        try {
            // Check if block already exists
            if (blockRepository.findByNumber(blockNumber.longValue()).isPresent()) {
                log.debug("Block {} already exists, skipping", blockNumber);
                return;
            }

            log.info("Syncing block {}", blockNumber);

            // Fetch block from blockchain
            BlockResponse blockResponse = cryptoNodeClient.getBlockByNumber(blockNumber);

            // Save block
            Block block = new Block();
            block.setHash(blockResponse.getHash());
            block.setNumber(blockResponse.getNumber().longValue());
            block.setTimestamp(blockResponse.getTimestamp());
            block.setParentHash(blockResponse.getParentHash());
            block.setTransactionCount(blockResponse.getTransactions() != null ? blockResponse.getTransactions().size() : 0);

            block = blockRepository.save(block);

            // Save transactions
            if (blockResponse.getTransactions() != null && !blockResponse.getTransactions().isEmpty()) {
                List<Transaction> transactions = new ArrayList<>();

                for (TransactionResponse txResponse : blockResponse.getTransactions()) {
                    Transaction tx = new Transaction();
                    tx.setHash(txResponse.getHash());
                    tx.setFromAddress(txResponse.getFromAddress());
                    tx.setToAddress(txResponse.getToAddress());
                    tx.setValue(txResponse.getValue());
                    tx.setBlockNumber(blockResponse.getNumber().longValue());
                    tx.setStatus(TransactionStatus.CONFIRMED);
                    tx.setBlock(block);

                    transactions.add(tx);
                }

                transactionRepository.saveAll(transactions);

                // Wallet balances are fetched on-demand by WalletService.getBalance() —
                // syncing them per block costs ~1 RPC per unique address and blows the
                // public-node rate limit without providing useful freshness.

                log.info("Saved block {} with {} transactions", blockNumber, transactions.size());
            } else {
                log.info("Saved block {} with no transactions", blockNumber);
            }

        } catch (Exception e) {
            log.error("Error syncing block {}", blockNumber, e);
            throw new RuntimeException("Failed to sync block " + blockNumber, e);
        }
    }

    /**
     * Manual trigger for syncing a specific block range
     */
    @Transactional
    public void syncBlockRange(long startBlock, long endBlock) {
        log.info("Manually syncing blocks {} to {}", startBlock, endBlock);

        for (long i = startBlock; i <= endBlock; i++) {
            syncBlock(BigInteger.valueOf(i));
        }

        log.info("Manual sync completed for blocks {} to {}", startBlock, endBlock);
    }

    /**
     * Get sync status
     */
    public SyncStatus getSyncStatus() {
        BigInteger latestBlockchain = cryptoNodeClient.getLatestBlockNumber();
        BigInteger latestDatabase = blockRepository.findTopByOrderByNumberDesc()
                .map(b -> BigInteger.valueOf(b.getNumber()))
                .orElse(BigInteger.ZERO);

        BigInteger blocksBehind = latestBlockchain.subtract(latestDatabase);

        return SyncStatus.builder()
                .latestBlockchainBlock(latestBlockchain.longValue())
                .latestDatabaseBlock(latestDatabase.longValue())
                .blocksBehind(blocksBehind.longValue())
                .syncEnabled(syncEnabled)
                .isFullySynced(blocksBehind.compareTo(BigInteger.ZERO) == 0)
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class SyncStatus {
        private long latestBlockchainBlock;
        private long latestDatabaseBlock;
        private long blocksBehind;
        private boolean syncEnabled;
        private boolean isFullySynced;
    }
}
