package org.example.blockchainuz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Schema(description = "Blockchain transaction")
public record TransactionDTO(
        @Schema(description = "Transaction hash", example = "0xtx123abc...") String hash,
        @Schema(description = "Sender address", example = "0xSenderAddress...") String fromAddress,
        @Schema(description = "Receiver address", example = "0xReceiverAddress...") String toAddress,
        @Schema(description = "Transaction value", example = "1.5") BigDecimal value,
        @Schema(description = "Block number this transaction belongs to", example = "18946231") Long blockNumber,
        @Schema(description = "Transaction status", example = "CONFIRMED") String status,
        @Schema(description = "Timestamp from the parent block") Instant timestamp
) {}
