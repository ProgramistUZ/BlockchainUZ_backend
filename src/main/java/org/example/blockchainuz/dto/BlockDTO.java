package org.example.blockchainuz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
@Schema(description = "Blockchain block")
public record BlockDTO(
        @Schema(description = "Block hash", example = "0xabc123def456...") String hash,
        @Schema(description = "Block number", example = "18946231") Long number,
        @Schema(description = "Block timestamp") Instant timestamp,
        @Schema(description = "Parent block hash", example = "0xdef789abc012...") String parentHash,
        @Schema(description = "Number of transactions in this block", example = "142") Integer transactionCount,
        @Schema(description = "Transactions in this block (included on single block fetch)") List<TransactionDTO> transactions
) {}
