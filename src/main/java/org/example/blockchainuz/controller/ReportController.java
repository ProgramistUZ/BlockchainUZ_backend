package org.example.blockchainuz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.dto.StatsDTO;
import org.example.blockchainuz.dto.TopAddressDTO;
import org.example.blockchainuz.dto.VolumeReportDTO;
import org.example.blockchainuz.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Blockchain reporting and analytics endpoints")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/stats")
    @Operation(summary = "Get general statistics", description = "Returns overall blockchain statistics including total blocks, transactions, addresses, and averages")
    public ResponseEntity<StatsDTO> getStatistics() {
        return ResponseEntity.ok(reportService.getStatistics());
    }

    @GetMapping("/volume")
    @Operation(summary = "Get transaction volume", description = "Returns transaction volume grouped by period (daily or weekly)")
    public ResponseEntity<List<VolumeReportDTO>> getVolume(
            @Parameter(description = "Period for grouping: daily or weekly", example = "daily")
            @RequestParam(defaultValue = "daily") String period
    ) {
        return ResponseEntity.ok(reportService.getVolumeReport(period));
    }

    @GetMapping("/top-addresses")
    @Operation(summary = "Get most active addresses", description = "Returns the most active addresses by transaction count")
    public ResponseEntity<List<TopAddressDTO>> getTopAddresses(
            @Parameter(description = "Maximum number of addresses to return", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (limit < 1 || limit > 100) {
            limit = 10;
        }
        return ResponseEntity.ok(reportService.getTopAddresses(limit));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export transactions to CSV", description = "Exports filtered transactions as CSV with streaming response for large datasets")
    public ResponseEntity<StreamingResponseBody> exportTransactionsCsv(
            @Parameter(description = "Start date for filtering (ISO 8601)", example = "2026-04-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for filtering (ISO 8601)", example = "2026-04-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter by address (sender or receiver)", example = "0xAddress123...")
            @RequestParam(required = false) String address
    ) {
        Instant start = startDate != null ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant end = endDate != null ? endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

        StreamingResponseBody stream = reportService.exportTransactionsCsv(start, end, address);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(stream);
    }

    @GetMapping("/export/json")
    @Operation(summary = "Export transactions to JSON", description = "Exports filtered transactions as JSON with streaming response for large datasets")
    public ResponseEntity<StreamingResponseBody> exportTransactionsJson(
            @Parameter(description = "Start date for filtering (ISO 8601)", example = "2026-04-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for filtering (ISO 8601)", example = "2026-04-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Filter by address (sender or receiver)", example = "0xAddress123...")
            @RequestParam(required = false) String address
    ) {
        Instant start = startDate != null ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant end = endDate != null ? endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

        StreamingResponseBody stream = reportService.exportTransactionsJson(start, end, address);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }
}
