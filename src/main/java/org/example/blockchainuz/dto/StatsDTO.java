package org.example.blockchainuz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "General blockchain statistics")
public record StatsDTO(
        @Schema(description = "Total number of blocks", example = "18946231") Long totalBlocks,
        @Schema(description = "Total number of transactions", example = "2456789") Long totalTransactions,
        @Schema(description = "Total number of unique addresses", example = "125678") Long totalUniqueAddresses,
        @Schema(description = "Average block time in seconds", example = "12.5") Double averageBlockTime,
        @Schema(description = "Average transaction value", example = "0.15") BigDecimal averageTransactionValue
) {}
