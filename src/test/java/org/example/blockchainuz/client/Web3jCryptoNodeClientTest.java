package org.example.blockchainuz.client;

import org.example.blockchainuz.client.dto.BlockResponse;
import org.example.blockchainuz.exception.CryptoNodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Web3jCryptoNodeClientTest {

    @Mock
    private Web3j web3j;

    private Web3jCryptoNodeClient client;

    @BeforeEach
    void setUp() {
        client = new Web3jCryptoNodeClient(web3j);
    }

    @Test
    void getLatestBlockNumber_returnsValueFromNode() throws IOException {
        // Web3j returns a Request<?, EthBlockNumber> that you .send() to hit the network.
        // To mock: stub ethBlockNumber() -> fake Request whose .send() returns a pre-filled EthBlockNumber.
        EthBlockNumber response = new EthBlockNumber();
        response.setResult("0xa531be"); // hex for 10826174

        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> request = mock(Request.class);
        when(request.send()).thenReturn(response);
        when(web3j.ethBlockNumber()).thenReturn((Request) request);

        BigInteger result = client.getLatestBlockNumber();

        assertEquals(BigInteger.valueOf(10826174L), result);
    }

    @Test
    void getLatestBlockNumber_wrapsIOExceptionInCryptoNodeException() throws IOException {
        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> request = mock(Request.class);
        when(request.send()).thenThrow(new IOException("network down"));
        when(web3j.ethBlockNumber()).thenReturn((Request) request);

        CryptoNodeException ex = assertThrows(CryptoNodeException.class, () -> client.getLatestBlockNumber());
        assertNotNull(ex.getCause());
        assertInstanceOf(IOException.class, ex.getCause());
    }

    @Test
    void getBlockByNumber_throwsWhenBlockIsNull() throws IOException {
        // Sepolia returns null for blocks that don't exist yet. The client should translate
        // that into CryptoNodeException("Block not found: ...") rather than NPE later.
        EthBlock ethBlock = new EthBlock();
        ethBlock.setResult(null);

        @SuppressWarnings("unchecked")
        Request<?, EthBlock> request = mock(Request.class);
        when(request.send()).thenReturn(ethBlock);
        when(web3j.ethGetBlockByNumber(any(DefaultBlockParameter.class), anyBoolean()))
                .thenReturn((Request) request);

        BigInteger futureBlock = BigInteger.valueOf(Long.MAX_VALUE);
        CryptoNodeException ex = assertThrows(CryptoNodeException.class,
                () -> client.getBlockByNumber(futureBlock));
        assertTrue(ex.getMessage().contains(futureBlock.toString()));
    }

    @Test
    void getBlockByNumber_mapsBlockFields() throws IOException {
        // Web3j's Block has a huge constructor that changes between versions.
        // Using the no-arg constructor + setters is version-robust.
        EthBlock.Block block = new EthBlock.Block();
        block.setNumber("0x64");            // 100
        block.setHash("0xblockhash");
        block.setParentHash("0xparenthash");
        block.setTimestamp("0x5f5e100");    // 100000000 epoch
        block.setTransactions(List.of());

        EthBlock ethBlock = new EthBlock();
        ethBlock.setResult(block);

        @SuppressWarnings("unchecked")
        Request<?, EthBlock> request = mock(Request.class);
        when(request.send()).thenReturn(ethBlock);
        when(web3j.ethGetBlockByNumber(any(DefaultBlockParameter.class), anyBoolean()))
                .thenReturn((Request) request);

        BlockResponse response = client.getBlockByNumber(BigInteger.valueOf(100));

        assertEquals("0xblockhash", response.getHash());
        assertEquals(BigInteger.valueOf(100), response.getNumber());
        assertEquals("0xparenthash", response.getParentHash());
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTransactions().isEmpty());
    }

    @Test
    void isConnected_returnsTrueWhenNodeResponds() throws IOException {
        EthBlockNumber response = new EthBlockNumber();
        response.setResult("0x1");

        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> request = mock(Request.class);
        when(request.send()).thenReturn(response);
        when(web3j.ethBlockNumber()).thenReturn((Request) request);

        assertTrue(client.isConnected());
    }

    @Test
    void isConnected_returnsFalseOnIOException() throws IOException {
        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> request = mock(Request.class);
        when(request.send()).thenThrow(new IOException("socket closed"));
        when(web3j.ethBlockNumber()).thenReturn((Request) request);

        assertFalse(client.isConnected());
    }

    // Documents a bug: isConnected only catches IOException. Any unchecked exception
    // from .send() — e.g. a malformed response deserialization — escapes and crashes
    // whatever health-check calls this. A connection-check method that can itself throw
    // is arguably not a connection check. When the impl is fixed to catch Exception,
    // flip this to assertFalse.
    @Test
    void isConnected_currentlyPropagatesRuntimeException_shouldReturnFalse() throws IOException {
        @SuppressWarnings("unchecked")
        Request<?, EthBlockNumber> request = mock(Request.class);
        when(request.send()).thenThrow(new RuntimeException("bad JSON"));
        when(web3j.ethBlockNumber()).thenReturn((Request) request);

        assertThrows(RuntimeException.class, () -> client.isConnected());
    }
}
