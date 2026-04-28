package org.example.blockchainuz.client.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for block data from blockchain node
 */
@Data
@Builder
public class BlockResponse {
    private String hash;
    private BigInteger number;
    private Instant timestamp;
    private String parentHash;
    private List<TransactionResponse> transactions;
}
