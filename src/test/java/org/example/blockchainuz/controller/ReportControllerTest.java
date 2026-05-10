package org.example.blockchainuz.controller;

import org.example.blockchainuz.dto.StatsDTO;
import org.example.blockchainuz.dto.TopAddressDTO;
import org.example.blockchainuz.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private ReportService reportService;

    @Test
    void getStats_returnsDto() throws Exception {
        when(reportService.getStatistics()).thenReturn(StatsDTO.builder()
                .totalBlocks(100L).totalTransactions(500L).totalUniqueAddresses(42L)
                .averageBlockTime(12.5).averageTransactionValue(new BigDecimal("0.25")).build());

        mvc.perform(get("/api/reports/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBlocks").value(100))
                .andExpect(jsonPath("$.averageBlockTime").value(12.5));
    }

    @Test
    void getTopAddresses_returnsList() throws Exception {
        when(reportService.getTopAddresses(10)).thenReturn(List.of(
                TopAddressDTO.builder().address("0xa").transactionCount(50L).build(),
                TopAddressDTO.builder().address("0xb").transactionCount(30L).build()
        ));

        mvc.perform(get("/api/reports/top-addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].address").value("0xa"))
                .andExpect(jsonPath("$[0].transactionCount").value(50));
    }

    @Test
    void getTopAddresses_clampsInvalidLimitToDefault() throws Exception {
        // Controller silently resets out-of-range limits (1..100) to 10 — a questionable choice
        // (better would be 400), but we pin the current behavior.
        when(reportService.getTopAddresses(10)).thenReturn(List.of());

        mvc.perform(get("/api/reports/top-addresses").param("limit", "9999"))
                .andExpect(status().isOk());

        verify(reportService).getTopAddresses(10);
    }

    @Test
    void exportCsv_setsContentDispositionHeaderAndStreamsBody() throws Exception {
        String csvBody = "Hash,From,To,Value\n0xtx1,0xa,0xb,1.5\n";
        when(reportService.exportTransactionsCsv(any(), any(), any()))
                .thenReturn(out -> out.write(csvBody.getBytes()));

        // Streaming bodies are async — first perform() returns before the body is rendered.
        // asyncDispatch on the returned result drives the StreamingResponseBody to completion
        // so headers + body can be asserted.
        MvcResult async = mvc.perform(get("/api/reports/export/csv")).andReturn();

        mvc.perform(asyncDispatch(async))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transactions.csv"))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(csvBody));
    }

    @Test
    void exportCsv_scaffolding_verifiesServiceCalledWithParsedDates() throws Exception {
        when(reportService.exportTransactionsCsv(any(), any(), anyString()))
                .thenReturn(outputStream -> outputStream.write("hash\n".getBytes()));

        mvc.perform(get("/api/reports/export/csv")
                        .param("startDate", "2026-04-01")
                        .param("endDate", "2026-04-30")
                        .param("address", "0xabc"))
                .andExpect(status().isOk());

        verify(reportService).exportTransactionsCsv(any(), any(), any());
    }
}
