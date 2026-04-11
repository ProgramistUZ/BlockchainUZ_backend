package org.example.blockchainuz.mapper;

import org.example.blockchainuz.dto.BlockDTO;
import org.example.blockchainuz.entity.Block;

import java.util.List;

public final class BlockMapper {

    private BlockMapper() {}

    public static BlockDTO toDTO(Block block) {
        return BlockDTO.builder()
                .hash(block.getHash())
                .number(block.getNumber())
                .timestamp(block.getTimestamp())
                .parentHash(block.getParentHash())
                .transactionCount(block.getTransactionCount())
                .build();
    }

    public static BlockDTO toDTOWithTransactions(Block block) {
        List<org.example.blockchainuz.dto.TransactionDTO> txDtos = null;
        if (block.getTransactions() != null) {
            txDtos = block.getTransactions().stream()
                    .map(tx -> TransactionMapper.toDTO(tx, block.getTimestamp()))
                    .toList();
        }
        return BlockDTO.builder()
                .hash(block.getHash())
                .number(block.getNumber())
                .timestamp(block.getTimestamp())
                .parentHash(block.getParentHash())
                .transactionCount(block.getTransactionCount())
                .transactions(txDtos)
                .build();
    }
}
