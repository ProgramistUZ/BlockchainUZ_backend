package org.example.blockchainuz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.dto.WalletDTO;
import org.example.blockchainuz.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
