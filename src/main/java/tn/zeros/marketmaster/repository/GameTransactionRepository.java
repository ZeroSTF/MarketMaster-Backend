package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.GameTransaction;

public interface GameTransactionRepository extends JpaRepository<GameTransaction,Long > {

}
