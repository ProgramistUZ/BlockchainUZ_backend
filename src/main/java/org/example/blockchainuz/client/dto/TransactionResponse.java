package org.example.blockchainuz.client.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Response DTO for transaction data from blockchain node
 */
@Data
@Builder
public class TransactionResponse {
    private String hash;
    private String fromAddress;
    private String toAddress;
    private BigDecimal value;
    private BigInteger blockNumber;
    private String blockHash;
}
