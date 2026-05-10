package org.example.blockchainuz.repository;

import org.example.blockchainuz.entity.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByHash(String hash);

    Optional<Block> findByNumber(Long number);

    Optional<Block> findTopByOrderByNumberDesc();

    Page<Block> findAll(Pageable pageable);

    @Query(value = """
            SELECT AVG(EXTRACT(EPOCH FROM (b2.timestamp - b1.timestamp)))
            FROM blockchainuz.blocks b1
            JOIN blockchainuz.blocks b2 ON b2.number = b1.number + 1
            WHERE b1.number < (SELECT MAX(number) FROM blockchainuz.blocks)
            """, nativeQuery = true)
    Double getAverageBlockTime();
}
