package org.example.blockchainuz.repository;

import org.example.blockchainuz.entity.Transaction;
import org.example.blockchainuz.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
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

    @Query("SELECT COUNT(DISTINCT t.fromAddress) + COUNT(DISTINCT t.toAddress) FROM Transaction t")
    Long countUniqueAddresses();

    @Query("SELECT AVG(t.value) FROM Transaction t WHERE t.value IS NOT NULL")
    Double getAverageTransactionValue();

    // COALESCE on the Instant params is a PostgreSQL workaround: a bare `:param IS NULL`
    // bind-parameter check on Instant fails with "could not determine data type of parameter $1"
    // because typed-null inference is unreliable for timestamps. Since b.timestamp is NOT NULL,
    // `b.timestamp >= coalesce(:startDate, b.timestamp)` is true whenever :startDate is null,
    // i.e. no filter — without needing PG to type-infer a standalone NULL.
    @Query("""
            SELECT t FROM Transaction t
            JOIN FETCH t.block b
            WHERE b.timestamp >= coalesce(:startDate, b.timestamp)
              AND b.timestamp <= coalesce(:endDate, b.timestamp)
              AND (:address IS NULL OR t.fromAddress = :address OR t.toAddress = :address)
            ORDER BY b.timestamp DESC
            """)
    List<Transaction> findForExport(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("address") String address
    );

    @Query(value = """
            SELECT
                CAST(b.timestamp AS DATE) as date,
                COUNT(t.id) as transaction_count,
                COALESCE(SUM(t.value), 0) as total_volume
            FROM blockchainuz.transactions t
            JOIN blockchainuz.blocks b ON t.block_id = b.id
            WHERE b.timestamp >= :startDate
            GROUP BY CAST(b.timestamp AS DATE)
            ORDER BY date DESC
            """, nativeQuery = true)
    List<Object[]> getDailyVolume(@Param("startDate") Instant startDate);

    @Query(value = """
            SELECT
                DATE_TRUNC('week', b.timestamp) as week,
                COUNT(t.id) as transaction_count,
                COALESCE(SUM(t.value), 0) as total_volume
            FROM blockchainuz.transactions t
            JOIN blockchainuz.blocks b ON t.block_id = b.id
            WHERE b.timestamp >= :startDate
            GROUP BY DATE_TRUNC('week', b.timestamp)
            ORDER BY week DESC
            """, nativeQuery = true)
    List<Object[]> getWeeklyVolume(@Param("startDate") Instant startDate);

    @Query(value = """
            SELECT address, COUNT(*) as tx_count
            FROM (
                SELECT from_address as address FROM blockchainuz.transactions WHERE from_address IS NOT NULL
                UNION ALL
                SELECT to_address as address FROM blockchainuz.transactions WHERE to_address IS NOT NULL
            ) addresses
            GROUP BY address
            ORDER BY tx_count DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopActiveAddresses(@Param("limit") int limit);
}
