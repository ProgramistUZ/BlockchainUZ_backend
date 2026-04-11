package org.example.blockchainuz.repository;

import org.example.blockchainuz.entity.Transaction;
import org.example.blockchainuz.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByHash(String hash);

    Page<Transaction> findByBlockNumber(Long blockNumber, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.fromAddress = :address OR t.toAddress = :address")
    Page<Transaction> findByAddress(@Param("address") String address, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAddress = :address OR t.toAddress = :address")
    long countByAddress(@Param("address") String address);

    @Query("""
            SELECT t FROM Transaction t
            WHERE (:hash IS NULL OR t.hash = :hash)
              AND (:blockNumber IS NULL OR t.blockNumber = :blockNumber)
              AND (:status IS NULL OR t.status = :status)
              AND (:address IS NULL OR t.fromAddress = :address OR t.toAddress = :address)
            """)
    Page<Transaction> search(
            @Param("hash") String hash,
            @Param("blockNumber") Long blockNumber,
            @Param("status") TransactionStatus status,
            @Param("address") String address,
            Pageable pageable
    );
}
