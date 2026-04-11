package org.example.blockchainuz.service;

import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.dto.WalletDTO;
import org.example.blockchainuz.exception.ResourceNotFoundException;
import org.example.blockchainuz.mapper.WalletMapper;
import org.example.blockchainuz.repository.TransactionRepository;
import org.example.blockchainuz.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletDTO getWalletByAddress(String address) {
        var wallet = walletRepository.findByAddress(address)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + address));
        long txCount = transactionRepository.countByAddress(address);
        return WalletMapper.toDTO(wallet, txCount);
    }
}
