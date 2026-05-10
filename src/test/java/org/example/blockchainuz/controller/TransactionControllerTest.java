package org.example.blockchainuz.controller;

import org.example.blockchainuz.dto.PagedResponseDTO;
import org.example.blockchainuz.dto.TransactionDTO;
import org.example.blockchainuz.entity.TransactionStatus;
import org.example.blockchainuz.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private TransactionService transactionService;

    @Test
    void list_returnsPagedResponse() throws Exception {
        TransactionDTO tx = TransactionDTO.builder()
                .hash("0xtx").fromAddress("0xa").toAddress("0xb")
                .value(new BigDecimal("1.5")).blockNumber(10L).status("CONFIRMED").build();
        when(transactionService.getTransactions(any(Pageable.class)))
                .thenReturn(PagedResponseDTO.of(List.of(tx), 0, 20, 1L, 1));

        mvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].hash").value("0xtx"))
                .andExpect(jsonPath("$.content[0].value").value(1.5));
    }

    @Test
    void search_passesAllFiltersToService() throws Exception {
        when(transactionService.searchTransactions(
                eq("0xh"), eq(5L), eq(TransactionStatus.CONFIRMED), eq("0xaddr"), any(Pageable.class)))
                .thenReturn(PagedResponseDTO.of(List.of(), 0, 20, 0L, 0));

        mvc.perform(get("/api/transactions/search")
                        .param("hash", "0xh")
                        .param("blockNumber", "5")
                        .param("status", "CONFIRMED")
                        .param("address", "0xaddr"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageCap = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionService).searchTransactions(
                eq("0xh"), eq(5L), eq(TransactionStatus.CONFIRMED), eq("0xaddr"), pageCap.capture());
        assertThat(pageCap.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void search_invalidEnumCurrentlyReturns500_shouldBe400() throws Exception {
        // Documents a bug: invalid enum binding throws MethodArgumentTypeMismatchException,
        // which falls through to the generic @ExceptionHandler(Exception.class) and returns 500
        // with "Internal server error". It should be a 400. See GlobalExceptionHandler.handleGeneric.
        mvc.perform(get("/api/transactions/search").param("status", "NOT_A_STATUS"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }
}
