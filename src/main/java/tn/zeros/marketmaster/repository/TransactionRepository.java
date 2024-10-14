package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}