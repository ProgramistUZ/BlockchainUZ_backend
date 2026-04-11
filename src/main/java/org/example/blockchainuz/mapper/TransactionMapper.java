package org.example.blockchainuz.mapper;

import org.example.blockchainuz.dto.TransactionDTO;
import org.example.blockchainuz.entity.Transaction;

import java.time.Instant;

public final class TransactionMapper {

    private TransactionMapper() {}

    public static TransactionDTO toDTO(Transaction tx) {
        Instant blockTimestamp = null;
        if (tx.getBlock() != null) {
            blockTimestamp = tx.getBlock().getTimestamp();
        }
        return toDTO(tx, blockTimestamp);
    }

    public static TransactionDTO toDTO(Transaction tx, Instant blockTimestamp) {
        return TransactionDTO.builder()
                .hash(tx.getHash())
                .fromAddress(tx.getFromAddress())
                .toAddress(tx.getToAddress())
                .value(tx.getValue())
                .blockNumber(tx.getBlockNumber())
                .status(tx.getStatus() != null ? tx.getStatus().name() : null)
                .timestamp(blockTimestamp)
                .build();
    }
}
