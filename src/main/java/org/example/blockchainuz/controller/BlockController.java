package org.example.blockchainuz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.dto.BlockDTO;
import org.example.blockchainuz.dto.PagedResponseDTO;
import org.example.blockchainuz.service.BlockService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@Tag(name = "Blocks", description = "Blockchain block endpoints")
public class BlockController {

    private final BlockService blockService;

    @GetMapping
    @Operation(summary = "List blocks", description = "Returns a paginated list of blocks")
    public ResponseEntity<PagedResponseDTO<BlockDTO>> getBlocks(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(blockService.getBlocks(pageable));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest block", description = "Returns the most recent block")
    public ResponseEntity<BlockDTO> getLatestBlock() {
        return ResponseEntity.ok(blockService.getLatestBlock());
    }

    @GetMapping("/hash/{hash}")
    @Operation(summary = "Get block by hash", description = "Returns a single block with its transactions")
    public ResponseEntity<BlockDTO> getBlockByHash(@PathVariable String hash) {
        return ResponseEntity.ok(blockService.getBlockByHash(hash));
    }

    @GetMapping("/number/{number}")
    @Operation(summary = "Get block by number", description = "Returns a single block with its transactions")
    public ResponseEntity<BlockDTO> getBlockByNumber(@PathVariable Long number) {
        return ResponseEntity.ok(blockService.getBlockByNumber(number));
    }

    @GetMapping("/number/{number}/previous")
    @Operation(summary = "Get previous block", description = "Returns the block before the specified block number")
    public ResponseEntity<BlockDTO> getPreviousBlock(@PathVariable Long number) {
        return ResponseEntity.ok(blockService.getPreviousBlock(number));
    }

    @GetMapping("/number/{number}/next")
    @Operation(summary = "Get next block", description = "Returns the block after the specified block number")
    public ResponseEntity<BlockDTO> getNextBlock(@PathVariable Long number) {
        return ResponseEntity.ok(blockService.getNextBlock(number));
    }
}
