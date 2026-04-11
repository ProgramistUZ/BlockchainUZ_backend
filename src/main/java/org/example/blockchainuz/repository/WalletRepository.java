package org.example.blockchainuz.repository;

import org.example.blockchainuz.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByAddress(String address);
}
