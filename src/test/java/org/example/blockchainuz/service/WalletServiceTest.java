package org.example.blockchainuz.service;

import org.example.blockchainuz.client.CryptoNodeClient;
import org.example.blockchainuz.dto.WalletDTO;
import org.example.blockchainuz.entity.Wallet;
import org.example.blockchainuz.exception.CryptoNodeException;
import org.example.blockchainuz.repository.TransactionRepository;
import org.example.blockchainuz.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    private static final String ADDR = "0xabc";

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CryptoNodeClient cryptoNodeClient;

    @InjectMocks private WalletService walletService;

    @Test
    void getWalletByAddress_returnsFromDbWhenPresent() {
        Wallet wallet = new Wallet();
        wallet.setAddress(ADDR);
        wallet.setBalance(new BigDecimal("1.5"));
        wallet.setLastSeen(Instant.now());

        when(walletRepository.findByAddress(ADDR)).thenReturn(Optional.of(wallet));
        when(transactionRepository.countByAddress(ADDR)).thenReturn(7L);

        WalletDTO dto = walletService.getWalletByAddress(ADDR);

        assertEquals(ADDR, dto.address());
        assertEquals(0, new BigDecimal("1.5").compareTo(dto.balance()));
        assertEquals(7L, dto.transactionCount());
        verifyNoInteractions(cryptoNodeClient);
    }

    @Test
    void getWalletByAddress_fallsBackToBlockchainWhenNotInDb() {
        when(walletRepository.findByAddress(ADDR)).thenReturn(Optional.empty());
        // 2.0 ETH in wei
        when(cryptoNodeClient.getBalance(ADDR))
                .thenReturn(new BigInteger("2000000000000000000"));

        WalletDTO dto = walletService.getWalletByAddress(ADDR);

        assertEquals(ADDR, dto.address());
        assertEquals(0, new BigDecimal("2").compareTo(dto.balance()));
        assertEquals(0L, dto.transactionCount());
        assertNull(dto.lastSeen());
        verify(transactionRepository, never()).countByAddress(any());
    }

    // Pins current (questionable) behavior: a node failure during a DB-miss is silently
    // converted to a 0-balance response. If you ever change getBalanceFromBlockchain to
    // propagate the exception, this test must be updated — which is exactly the point.
    @Test
    void getWalletByAddress_returnsZeroBalanceWhenNodeFails_documentsSilentFailure() {
        when(walletRepository.findByAddress(ADDR)).thenReturn(Optional.empty());
        when(cryptoNodeClient.getBalance(ADDR))
                .thenThrow(new CryptoNodeException("RPC down"));

        WalletDTO dto = walletService.getWalletByAddress(ADDR);

        assertEquals(0, BigDecimal.ZERO.compareTo(dto.balance()),
                "Current impl swallows RPC errors and returns ZERO — see WalletService.getBalanceFromBlockchain");
    }

    @Test
    void getBalance_returnsDbBalanceWhenPresent() {
        Wallet wallet = new Wallet();
        wallet.setAddress(ADDR);
        wallet.setBalance(new BigDecimal("3.14"));
        when(walletRepository.findByAddress(ADDR)).thenReturn(Optional.of(wallet));

        BigDecimal balance = walletService.getBalance(ADDR);

        assertEquals(0, new BigDecimal("3.14").compareTo(balance));
        verifyNoInteractions(cryptoNodeClient);
    }

    @Test
    void getBalance_returnsZeroWhenDbBalanceIsNull() {
        Wallet wallet = new Wallet();
        wallet.setAddress(ADDR);
        wallet.setBalance(null);
        when(walletRepository.findByAddress(ADDR)).thenReturn(Optional.of(wallet));

        BigDecimal balance = walletService.getBalance(ADDR);

        assertEquals(0, BigDecimal.ZERO.compareTo(balance));
    }

    @Test
    void createOrUpdateWallet_createsNewWhenMissing() {
        when(walletRepository.findByAddress(ADDR)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        Wallet saved = walletService.createOrUpdateWallet(ADDR, new BigDecimal("5"));

        assertEquals(ADDR, saved.getAddress());
        assertEquals(0, new BigDecimal("5").compareTo(saved.getBalance()));
        assertNotNull(saved.getLastSeen());
    }

    @Test
    void createOrUpdateWallet_updatesExisting() {
        Wallet existing = new Wallet();
        existing.setId(42L);
        existing.setAddress(ADDR);
        existing.setBalance(BigDecimal.ONE);
        when(walletRepository.findByAddress(ADDR)).thenReturn(Optional.of(existing));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        Wallet saved = walletService.createOrUpdateWallet(ADDR, new BigDecimal("10"));

        assertEquals(42L, saved.getId(), "Should reuse existing row, not create new");
        assertEquals(0, new BigDecimal("10").compareTo(saved.getBalance()));
    }
}
