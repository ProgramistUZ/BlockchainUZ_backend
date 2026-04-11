package org.example.blockchainuz.repository;

import org.example.blockchainuz.entity.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByHash(String hash);

    Optional<Block> findByNumber(Long number);

    Optional<Block> findTopByOrderByNumberDesc();

    Page<Block> findAll(Pageable pageable);
}
