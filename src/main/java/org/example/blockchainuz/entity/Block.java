package org.example.blockchainuz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "blocks", schema = "blockchainuz")
@Data
@ToString(exclude = "transactions")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hash;

    @Column(nullable = false, unique = true)
    private Long number;

    @Column(nullable = false)
    private Instant timestamp;

    private String parentHash;

    private Integer transactionCount;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;
}
