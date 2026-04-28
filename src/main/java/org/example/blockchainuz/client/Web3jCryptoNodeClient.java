package org.example.blockchainuz.client;

import lombok.extern.slf4j.Slf4j;
import org.example.blockchainuz.client.dto.BlockResponse;
import org.example.blockchainuz.client.dto.TransactionResponse;
import org.example.blockchainuz.exception.CryptoNodeException;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Web3j implementation of CryptoNodeClient for Ethereum blockchain
 */
@Slf4j
@Component
public class Web3jCryptoNodeClient implements CryptoNodeClient {

    private final Web3j web3j;

    public Web3jCryptoNodeClient(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public BlockResponse getBlockByNumber(BigInteger blockNumber) {
        try {
            log.debug("Fetching block by number: {}", blockNumber);
            EthBlock ethBlock = web3j.ethGetBlockByNumber(
                    DefaultBlockParameter.valueOf(blockNumber),
                    true
            ).send();

            if (ethBlock.getBlock() == null) {
                throw new CryptoNodeException("Block not found: " + blockNumber);
            }

            return mapToBlockResponse(ethBlock.getBlock());
        } catch (IOException e) {
            log.error("Error fetching block by number: {}", blockNumber, e);
            throw new CryptoNodeException("Failed to fetch block: " + blockNumber, e);
        }
    }

    @Override
    public BlockResponse getBlockByHash(String hash) {
        try {
            log.debug("Fetching block by hash: {}", hash);
            EthBlock ethBlock = web3j.ethGetBlockByHash(hash, true).send();

            if (ethBlock.getBlock() == null) {
                throw new CryptoNodeException("Block not found: " + hash);
            }

            return mapToBlockResponse(ethBlock.getBlock());
        } catch (IOException e) {
            log.error("Error fetching block by hash: {}", hash, e);
            throw new CryptoNodeException("Failed to fetch block: " + hash, e);
        }
    }

    @Override
    public BigInteger getLatestBlockNumber() {
        try {
            log.debug("Fetching latest block number");
            return web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            log.error("Error fetching latest block number", e);
            throw new CryptoNodeException("Failed to fetch latest block number", e);
        }
    }

    @Override
    public TransactionResponse getTransactionByHash(String hash) {
        try {
            log.debug("Fetching transaction by hash: {}", hash);
            var tx = web3j.ethGetTransactionByHash(hash).send().getTransaction();

            if (tx.isEmpty()) {
                throw new CryptoNodeException("Transaction not found: " + hash);
            }

            return mapToTransactionResponse(tx.get());
        } catch (IOException e) {
            log.error("Error fetching transaction by hash: {}", hash, e);
            throw new CryptoNodeException("Failed to fetch transaction: " + hash, e);
        }
    }

    @Override
    public BigInteger getBalance(String address) {
        try {
            log.debug("Fetching balance for address: {}", address);
            EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).send();
            return balance.getBalance();
        } catch (IOException e) {
            log.error("Error fetching balance for address: {}", address, e);
            throw new CryptoNodeException("Failed to fetch balance for address: " + address, e);
        }
    }

    @Override
    public boolean isConnected() {
        try {
            web3j.ethBlockNumber().send();
            return true;
        } catch (IOException e) {
            log.warn("Connection check failed", e);
            return false;
        }
    }

    private BlockResponse mapToBlockResponse(EthBlock.Block block) {
        List<TransactionResponse> transactions = new ArrayList<>();

        if (block.getTransactions() != null) {
            for (EthBlock.TransactionResult txResult : block.getTransactions()) {
                if (txResult instanceof EthBlock.TransactionObject) {
                    EthBlock.TransactionObject txObject = (EthBlock.TransactionObject) txResult;
                    Transaction tx = txObject.get();
                    transactions.add(mapToTransactionResponse(tx));
                }
            }
        }

        return BlockResponse.builder()
                .hash(block.getHash())
                .number(block.getNumber())
                .timestamp(Instant.ofEpochSecond(block.getTimestamp().longValue()))
                .parentHash(block.getParentHash())
                .transactions(transactions)
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction tx) {
        BigDecimal valueInEth = tx.getValue() != null
                ? Convert.fromWei(new BigDecimal(tx.getValue()), Convert.Unit.ETHER)
                : BigDecimal.ZERO;

        return TransactionResponse.builder()
                .hash(tx.getHash())
                .fromAddress(tx.getFrom())
                .toAddress(tx.getTo())
                .value(valueInEth)
                .blockNumber(tx.getBlockNumber())
                .blockHash(tx.getBlockHash())
                .build();
    }
}
