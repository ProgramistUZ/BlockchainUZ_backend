package org.example.blockchainuz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.dto.PagedResponseDTO;
import org.example.blockchainuz.dto.TransactionDTO;
import org.example.blockchainuz.dto.WalletDTO;
import org.example.blockchainuz.service.WalletService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Wallet / address endpoints")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{address}")
    @Operation(summary = "Get wallet by address", description = "Returns wallet info including transaction count")
    public ResponseEntity<WalletDTO> getWalletByAddress(@PathVariable String address) {
        return ResponseEntity.ok(walletService.getWalletByAddress(address));
    }

    @GetMapping("/{address}/balance")
    @Operation(summary = "Get wallet balance", description = "Returns the current balance of the wallet")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String address) {
        return ResponseEntity.ok(walletService.getBalance(address));
    }

    @GetMapping("/{address}/transactions")
    @Operation(summary = "Get wallet transaction history", description = "Returns paginated transaction history for the wallet")
    public ResponseEntity<PagedResponseDTO<TransactionDTO>> getTransactionHistory(
            @PathVariable String address,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(walletService.getTransactionHistory(address, pageable));
    }
}
