package org.example.blockchainuz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Schema(description = "Wallet / address info")
public record WalletDTO(
        @Schema(description = "Wallet address", example = "0xWalletAddress...") String address,
        @Schema(description = "Current balance", example = "42.123456789") BigDecimal balance,
        @Schema(description = "Number of transactions involving this address", example = "37") Long transactionCount,
        @Schema(description = "Last seen timestamp") Instant lastSeen
) {}
