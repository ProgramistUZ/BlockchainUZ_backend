package org.example.blockchainuz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.dto.PagedResponseDTO;
import org.example.blockchainuz.dto.TransactionDTO;
import org.example.blockchainuz.entity.TransactionStatus;
import org.example.blockchainuz.service.TransactionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Blockchain transaction endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List transactions", description = "Returns a paginated list of transactions")
    public ResponseEntity<PagedResponseDTO<TransactionDTO>> getTransactions(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transactionService.getTransactions(pageable));
    }

    @GetMapping("/{hash}")
    @Operation(summary = "Get transaction by hash")
    public ResponseEntity<TransactionDTO> getTransactionByHash(@PathVariable String hash) {
        return ResponseEntity.ok(transactionService.getTransactionByHash(hash));
    }

    @GetMapping("/search")
    @Operation(summary = "Search transactions", description = "Filter transactions by hash, block number, status, or address")
    public ResponseEntity<PagedResponseDTO<TransactionDTO>> searchTransactions(
            @RequestParam(required = false) String hash,
            @RequestParam(required = false) Long blockNumber,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String address,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transactionService.searchTransactions(hash, blockNumber, status, address, pageable));
    }
}
