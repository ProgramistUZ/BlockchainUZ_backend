package org.example.blockchainuz.service;

import lombok.RequiredArgsConstructor;
import org.example.blockchainuz.dto.BlockDTO;
import org.example.blockchainuz.dto.PagedResponseDTO;
import org.example.blockchainuz.exception.ResourceNotFoundException;
import org.example.blockchainuz.mapper.BlockMapper;
import org.example.blockchainuz.repository.BlockRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    private final BlockRepository blockRepository;

    public PagedResponseDTO<BlockDTO> getBlocks(Pageable pageable) {
        var page = blockRepository.findAll(pageable);
        var dtos = page.getContent().stream()
                .map(BlockMapper::toDTO)
                .toList();
        return PagedResponseDTO.of(dtos, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public BlockDTO getBlockByHash(String hash) {
        var block = blockRepository.findByHash(hash)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found: " + hash));
        return BlockMapper.toDTOWithTransactions(block);
    }

    public BlockDTO getBlockByNumber(Long number) {
        var block = blockRepository.findByNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Block not found: " + number));
        return BlockMapper.toDTOWithTransactions(block);
    }

    public BlockDTO getLatestBlock() {
        var block = blockRepository.findTopByOrderByNumberDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No blocks found"));
        return BlockMapper.toDTO(block);
    }
}
