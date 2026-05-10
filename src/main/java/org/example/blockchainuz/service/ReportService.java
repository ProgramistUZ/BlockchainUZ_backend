package org.example.blockchainuz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.dto.StatsDTO;
import org.example.blockchainuz.dto.TopAddressDTO;
import org.example.blockchainuz.dto.TransactionDTO;
import org.example.blockchainuz.dto.VolumeReportDTO;
import org.example.blockchainuz.mapper.TransactionMapper;
import org.example.blockchainuz.repository.BlockRepository;
import org.example.blockchainuz.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final BlockRepository blockRepository;
    private final TransactionRepository transactionRepository;

    public StatsDTO getStatistics() {
        long totalBlocks = blockRepository.count();
        long totalTransactions = transactionRepository.count();
        Long uniqueAddresses = transactionRepository.countUniqueAddresses();
        Double avgBlockTime = blockRepository.getAverageBlockTime();
        Double avgTxValue = transactionRepository.getAverageTransactionValue();

        return StatsDTO.builder()
                .totalBlocks(totalBlocks)
                .totalTransactions(totalTransactions)
                .totalUniqueAddresses(uniqueAddresses != null ? uniqueAddresses : 0L)
                .averageBlockTime(avgBlockTime != null ? avgBlockTime : 0.0)
                .averageTransactionValue(avgTxValue != null ? BigDecimal.valueOf(avgTxValue) : BigDecimal.ZERO)
                .build();
    }

    public List<VolumeReportDTO> getVolumeReport(String period) {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        List<Object[]> results = "weekly".equalsIgnoreCase(period)
                ? transactionRepository.getWeeklyVolume(thirtyDaysAgo)
                : transactionRepository.getDailyVolume(thirtyDaysAgo);

        return results.stream()
                .map(row -> {
                    LocalDate date;
                    if (row[0] instanceof Date sqlDate) {
                        date = sqlDate.toLocalDate();
                    } else if (row[0] instanceof java.util.Date utilDate) {
                        date = utilDate.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
                    } else {
                        date = LocalDate.now();
                    }

                    Long count = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
                    BigDecimal volume = row[2] instanceof Number ? BigDecimal.valueOf(((Number) row[2]).doubleValue()) : BigDecimal.ZERO;

                    return VolumeReportDTO.builder()
                            .date(date)
                            .transactionCount(count)
                            .totalVolume(volume)
                            .build();
                })
                .toList();
    }

    public List<TopAddressDTO> getTopAddresses(int limit) {
        List<Object[]> results = transactionRepository.findTopActiveAddresses(limit);

        return results.stream()
                .map(row -> TopAddressDTO.builder()
                        .address((String) row[0])
                        .transactionCount(((Number) row[1]).longValue())
                        .build())
                .toList();
    }

    public StreamingResponseBody exportTransactionsCsv(Instant startDate, Instant endDate, String address) {
        return outputStream -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                writer.write("Hash,From Address,To Address,Value,Block Number,Status,Timestamp\n");

                List<TransactionDTO> transactions = transactionRepository
                        .findForExport(startDate, endDate, address)
                        .stream()
                        .map(TransactionMapper::toDTO)
                        .toList();

                for (TransactionDTO tx : transactions) {
                    writer.write(String.format("%s,%s,%s,%s,%d,%s,%s\n",
                            escapeCsv(tx.hash()),
                            escapeCsv(tx.fromAddress()),
                            escapeCsv(tx.toAddress()),
                            tx.value(),
                            tx.blockNumber(),
                            tx.status(),
                            tx.timestamp()));
                }
            }
        };
    }

    public StreamingResponseBody exportTransactionsJson(Instant startDate, Instant endDate, String address) {
        return outputStream -> {
            List<TransactionDTO> transactions = transactionRepository
                    .findForExport(startDate, endDate, address)
                    .stream()
                    .map(TransactionMapper::toDTO)
                    .toList();

            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            mapper.writeValue(outputStream, transactions);
        };
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
