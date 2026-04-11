package org.example.blockchainuz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.service.BlockchainSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for blockchain synchronization operations
 */
@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Blockchain synchronization endpoints")
public class SyncController {

    private final BlockchainSyncService syncService;

    @GetMapping("/status")
    @Operation(summary = "Get sync status", description = "Returns the current synchronization status")
    public ResponseEntity<BlockchainSyncService.SyncStatus> getSyncStatus() {
        return ResponseEntity.ok(syncService.getSyncStatus());
    }

    @PostMapping("/trigger")
    @Operation(summary = "Trigger manual sync", description = "Manually trigger blockchain synchronization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> triggerSync() {
        syncService.syncNewBlocks();
        return ResponseEntity.ok("Sync triggered successfully");
    }

    @PostMapping("/range")
    @Operation(summary = "Sync block range", description = "Manually sync a specific range of blocks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> syncBlockRange(
            @RequestParam long startBlock,
            @RequestParam long endBlock) {
        syncService.syncBlockRange(startBlock, endBlock);
        return ResponseEntity.ok(String.format("Synced blocks %d to %d", startBlock, endBlock));
    }
}
