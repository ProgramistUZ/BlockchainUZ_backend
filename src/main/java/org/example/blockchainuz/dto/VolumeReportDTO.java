package org.example.blockchainuz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Schema(description = "Transaction volume report for a specific period")
public record VolumeReportDTO(
        @Schema(description = "Date of the report period", example = "2026-04-29") LocalDate date,
        @Schema(description = "Number of transactions", example = "1234") Long transactionCount,
        @Schema(description = "Total transaction volume", example = "1250.75") BigDecimal totalVolume
) {}
