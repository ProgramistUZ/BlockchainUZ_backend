package org.example.blockchainuz.controller;

import org.example.blockchainuz.dto.WalletDTO;
import org.example.blockchainuz.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private WalletService walletService;

    @Test
    void getWallet_returnsDto() throws Exception {
        WalletDTO dto = WalletDTO.builder()
                .address("0xabc").balance(new BigDecimal("2.5")).transactionCount(4L).build();
        when(walletService.getWalletByAddress("0xabc")).thenReturn(dto);

        mvc.perform(get("/api/wallets/{a}", "0xabc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("0xabc"))
                .andExpect(jsonPath("$.balance").value(2.5))
                .andExpect(jsonPath("$.transactionCount").value(4));
    }

    @Test
    void getBalance_returnsScalar() throws Exception {
        when(walletService.getBalance("0xabc")).thenReturn(new BigDecimal("10.5"));

        mvc.perform(get("/api/wallets/{a}/balance", "0xabc"))
                .andExpect(status().isOk())
                .andExpect(content().string("10.5"));
    }
}
