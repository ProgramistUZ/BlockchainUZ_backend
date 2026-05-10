package org.example.blockchainuz.controller;

import org.example.blockchainuz.dto.BlockDTO;
import org.example.blockchainuz.dto.PagedResponseDTO;
import org.example.blockchainuz.exception.ResourceNotFoundException;
import org.example.blockchainuz.service.BlockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BlockController.class)
@AutoConfigureMockMvc(addFilters = false)
class BlockControllerTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private BlockService blockService;

    @Test
    void listBlocks_returnsPagedResponse() throws Exception {
        BlockDTO dto = BlockDTO.builder()
                .hash("0xh1").number(42L).timestamp(Instant.parse("2026-05-10T00:00:00Z"))
                .parentHash("0xp1").transactionCount(3).build();
        when(blockService.getBlocks(any(Pageable.class)))
                .thenReturn(PagedResponseDTO.of(List.of(dto), 0, 20, 1L, 1));

        mvc.perform(get("/api/blocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].hash").value("0xh1"))
                .andExpect(jsonPath("$.content[0].number").value(42))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getLatest_returnsOk() throws Exception {
        BlockDTO dto = BlockDTO.builder().hash("0xlatest").number(99L).build();
        when(blockService.getLatestBlock()).thenReturn(dto);

        mvc.perform(get("/api/blocks/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hash").value("0xlatest"))
                .andExpect(jsonPath("$.number").value(99));
    }

    @Test
    void getByNumber_returns404WhenMissing() throws Exception {
        when(blockService.getBlockByNumber(9999L))
                .thenThrow(new ResourceNotFoundException("Block not found: 9999"));

        mvc.perform(get("/api/blocks/number/{n}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Block not found: 9999"))
                .andExpect(jsonPath("$.path").value("/api/blocks/number/9999"));
    }

    @Test
    void getByHash_returnsOk() throws Exception {
        BlockDTO dto = BlockDTO.builder().hash("0xabc").number(7L).build();
        when(blockService.getBlockByHash("0xabc")).thenReturn(dto);

        mvc.perform(get("/api/blocks/hash/{h}", "0xabc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hash").value("0xabc"));
    }
}
