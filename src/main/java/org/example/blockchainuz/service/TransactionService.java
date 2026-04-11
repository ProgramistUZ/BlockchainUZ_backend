package org.example.blockchainuz.service;

import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.dto.PagedResponseDTO;
import org.example.blockchainuz.dto.TransactionDTO;
import org.example.blockchainuz.entity.TransactionStatus;
import org.example.blockchainuz.exception.ResourceNotFoundException;
import org.example.blockchainuz.mapper.TransactionMapper;
import org.example.blockchainuz.repository.TransactionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public PagedResponseDTO<TransactionDTO> getTransactions(Pageable pageable) {
        var page = transactionRepository.findAll(pageable);
        var dtos = page.getContent().stream()
                .map(TransactionMapper::toDTO)
                .toList();
        return PagedResponseDTO.of(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public TransactionDTO getTransactionByHash(String hash) {
        var tx = transactionRepository.findByHash(hash)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + hash));
        return TransactionMapper.toDTO(tx);
    }

    public PagedResponseDTO<TransactionDTO> searchTransactions(
            String hash, Long blockNumber, TransactionStatus status, String address, Pageable pageable) {
        var page = transactionRepository.search(hash, blockNumber, status, address, pageable);
        var dtos = page.getContent().stream()
                .map(TransactionMapper::toDTO)
                .toList();
        return PagedResponseDTO.of(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public PagedResponseDTO<TransactionDTO> getTransactionsByBlock(Long blockNumber, Pageable pageable) {
        var page = transactionRepository.findByBlockNumber(blockNumber, pageable);
        var dtos = page.getContent().stream()
                .map(TransactionMapper::toDTO)
                .toList();
        return PagedResponseDTO.of(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
