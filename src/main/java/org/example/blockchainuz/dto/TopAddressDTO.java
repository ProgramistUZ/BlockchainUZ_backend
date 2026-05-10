package org.example.blockchainuz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Most active address statistics")
public record TopAddressDTO(
        @Schema(description = "Wallet address", example = "0xAddress123...") String address,
        @Schema(description = "Total number of transactions", example = "456") Long transactionCount
) {}
