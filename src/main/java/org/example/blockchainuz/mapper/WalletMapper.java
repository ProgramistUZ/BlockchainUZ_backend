package org.example.blockchainuz.mapper;

import org.example.blockchainuz.dto.WalletDTO;
import org.example.blockchainuz.entity.Wallet;

public final class WalletMapper {

    private WalletMapper() {}

    public static WalletDTO toDTO(Wallet wallet, long transactionCount) {
        return WalletDTO.builder()
                .address(wallet.getAddress())
                .balance(wallet.getBalance())
                .transactionCount(transactionCount)
                .lastSeen(wallet.getLastSeen())
                .build();
    }
}
