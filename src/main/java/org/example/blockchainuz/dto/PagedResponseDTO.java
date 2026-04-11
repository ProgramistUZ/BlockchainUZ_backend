package org.example.blockchainuz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Paginated response wrapper")
public record PagedResponseDTO<T>(
        @Schema(description = "Page content") List<T> content,
        @Schema(description = "Current page number (0-based)", example = "0") int page,
        @Schema(description = "Page size", example = "20") int size,
        @Schema(description = "Total number of elements", example = "150") long totalElements,
        @Schema(description = "Total number of pages", example = "8") int totalPages
) {
    public static <T> PagedResponseDTO<T> of(List<T> content, int page, int size, long totalElements, int totalPages) {
        return PagedResponseDTO.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
