package org.example.blockchainuz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(description = "Error response")
public record ErrorResponseDTO(
        @Schema(description = "HTTP status code", example = "404") int status,
        @Schema(description = "Error message", example = "Block not found") String message,
        @Schema(description = "Timestamp of the error") Instant timestamp,
        @Schema(description = "Request path", example = "/api/blocks/999999999") String path
) {}
