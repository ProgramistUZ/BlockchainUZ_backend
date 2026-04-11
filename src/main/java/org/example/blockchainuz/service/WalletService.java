package org.example.blockchainuz.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.blockchainuz.client.CryptoNodeClient;
import org.example.blockchainuz.dto.PagedResponseDTO;
import org.example.blockchainuz.dto.TransactionDTO;
import org.example.blockchainuz.dto.WalletDTO;
import org.example.blockchainuz.entity.Wallet;
import org.example.blockchainuz.exception.ResourceNotFoundException;
import org.example.blockchainuz.mapper.TransactionMapper;
import org.example.blockchainuz.mapper.WalletMapper;
import org.example.blockchainuz.repository.TransactionRepository;
import org.example.blockchainuz.repository.WalletRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CryptoNodeClient cryptoNodeClient;

    public WalletDTO getWalletByAddress(String address) {
        var walletOpt = walletRepository.findByAddress(address);

        if (walletOpt.isEmpty()) {
            // Wallet not in DB, fetch balance from blockchain
            log.info("Wallet {} not found in DB, fetching from blockchain", address);
            BigDecimal balance = getBalanceFromBlockchain(address);

            return WalletDTO.builder()
                    .address(address)
                    .balance(balance)
                    .transactionCount(0L)
                    .lastSeen(null)
                    .build();
        }

        var wallet = walletOpt.get();
        long txCount = transactionRepository.countByAddress(address);
        return WalletMapper.toDTO(wallet, txCount);
    }

    public BigDecimal getBalance(String address) {
        var walletOpt = walletRepository.findByAddress(address);

        if (walletOpt.isEmpty()) {
            // Wallet not in DB, fetch from blockchain
            log.info("Fetching balance for {} directly from blockchain", address);
            return getBalanceFromBlockchain(address);
        }

        var wallet = walletOpt.get();
        return wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
    }

    private BigDecimal getBalanceFromBlockchain(String address) {
        try {
            BigInteger balanceWei = cryptoNodeClient.getBalance(address);
            return Convert.fromWei(new BigDecimal(balanceWei), Convert.Unit.ETHER);
        } catch (Exception e) {
            log.error("Failed to fetch balance from blockchain for address: {}", address, e);
            return BigDecimal.ZERO;
        }
    }

    public PagedResponseDTO<TransactionDTO> getTransactionHistory(String address, Pageable pageable) {
        var page = transactionRepository.findByAddress(address, pageable);
        var dtos = page.getContent().stream()
                .map(TransactionMapper::toDTO)
                .toList();
        return PagedResponseDTO.of(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Transactional
    public Wallet createOrUpdateWallet(String address, BigDecimal balance) {
        var wallet = walletRepository.findByAddress(address)
                .orElse(new Wallet());
        wallet.setAddress(address);
        wallet.setBalance(balance);
        wallet.setLastSeen(java.time.Instant.now());
        return walletRepository.save(wallet);
    }
}
