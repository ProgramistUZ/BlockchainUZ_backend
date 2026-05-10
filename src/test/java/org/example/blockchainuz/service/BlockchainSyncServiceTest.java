package org.example.blockchainuz.service;

import org.example.blockchainuz.client.CryptoNodeClient;
import org.example.blockchainuz.client.dto.BlockResponse;
import org.example.blockchainuz.client.dto.TransactionResponse;
import org.example.blockchainuz.entity.Block;
import org.example.blockchainuz.repository.BlockRepository;
import org.example.blockchainuz.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BlockchainSyncService
 */
@ExtendWith(MockitoExtension.class)
class BlockchainSyncServiceTest {

    @Mock
    private CryptoNodeClient cryptoNodeClient;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private BlockchainSyncService syncService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(syncService, "syncEnabled", true);
        ReflectionTestUtils.setField(syncService, "batchSize", 10);
        ReflectionTestUtils.setField(syncService, "startBlock", 0L);
    }

    @Test
    void testSyncNewBlocks_NoNewBlocks() {
        // Given
        BigInteger latestBlock = BigInteger.valueOf(100);
        Block lastSyncedBlock = new Block();
        lastSyncedBlock.setNumber(100L);

        when(cryptoNodeClient.getLatestBlockNumber()).thenReturn(latestBlock);
        when(blockRepository.findTopByOrderByNumberDesc()).thenReturn(Optional.of(lastSyncedBlock));

        // When
        syncService.syncNewBlocks();

        // Then
        verify(cryptoNodeClient, times(1)).getLatestBlockNumber();
        verify(blockRepository, times(1)).findTopByOrderByNumberDesc();
        verify(blockRepository, never()).save(any());
    }

    @Test
    void testSyncBlock_Success() {
        // Given
        BigInteger blockNumber = BigInteger.valueOf(101);
        BlockResponse blockResponse = BlockResponse.builder()
                .hash("0xabc123")
                .number(blockNumber)
                .timestamp(Instant.now())
                .parentHash("0xparent")
                .transactions(List.of(
                        TransactionResponse.builder()
                                .hash("0xtx1")
                                .fromAddress("0xfrom")
                                .toAddress("0xto")
                                .value(BigDecimal.ONE)
                                .blockNumber(blockNumber)
                                .build()
                ))
                .build();

        when(blockRepository.findByNumber(blockNumber.longValue())).thenReturn(Optional.empty());
        when(cryptoNodeClient.getBlockByNumber(blockNumber)).thenReturn(blockResponse);
        when(blockRepository.save(any(Block.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.saveAll(any())).thenReturn(List.of());

        // When
        syncService.syncBlock(blockNumber);

        // Then
        verify(cryptoNodeClient, times(1)).getBlockByNumber(blockNumber);
        verify(blockRepository, times(1)).save(any(Block.class));
        verify(transactionRepository, times(1)).saveAll(any());
        // Balance fetching during sync was removed — wallets are populated lazily by
        // WalletService.getBalance() on first read. See BlockchainSyncService.syncBlock.
        verify(cryptoNodeClient, never()).getBalance(anyString());
    }

    @Test
    void testSyncBlock_BlockAlreadyExists() {
        // Given
        BigInteger blockNumber = BigInteger.valueOf(101);
        Block existingBlock = new Block();
        existingBlock.setNumber(blockNumber.longValue());

        when(blockRepository.findByNumber(blockNumber.longValue())).thenReturn(Optional.of(existingBlock));

        // When
        syncService.syncBlock(blockNumber);

        // Then
        verify(blockRepository, times(1)).findByNumber(blockNumber.longValue());
        verify(cryptoNodeClient, never()).getBlockByNumber(any());
        verify(blockRepository, never()).save(any());
    }

    @Test
    void testGetSyncStatus() {
        // Given
        BigInteger latestBlockchain = BigInteger.valueOf(1000);
        Block lastSyncedBlock = new Block();
        lastSyncedBlock.setNumber(950L);

        when(cryptoNodeClient.getLatestBlockNumber()).thenReturn(latestBlockchain);
        when(blockRepository.findTopByOrderByNumberDesc()).thenReturn(Optional.of(lastSyncedBlock));

        // When
        BlockchainSyncService.SyncStatus status = syncService.getSyncStatus();

        // Then
        assertEquals(1000L, status.getLatestBlockchainBlock());
        assertEquals(950L, status.getLatestDatabaseBlock());
        assertEquals(50L, status.getBlocksBehind());
        assertTrue(status.isSyncEnabled());
        assertFalse(status.isFullySynced());
    }

    @Test
    void testGetSyncStatus_FullySynced() {
        // Given
        BigInteger latestBlockchain = BigInteger.valueOf(1000);
        Block lastSyncedBlock = new Block();
        lastSyncedBlock.setNumber(1000L);

        when(cryptoNodeClient.getLatestBlockNumber()).thenReturn(latestBlockchain);
        when(blockRepository.findTopByOrderByNumberDesc()).thenReturn(Optional.of(lastSyncedBlock));

        // When
        BlockchainSyncService.SyncStatus status = syncService.getSyncStatus();

        // Then
        assertEquals(1000L, status.getLatestBlockchainBlock());
        assertEquals(1000L, status.getLatestDatabaseBlock());
        assertEquals(0L, status.getBlocksBehind());
        assertTrue(status.isFullySynced());
    }
}
