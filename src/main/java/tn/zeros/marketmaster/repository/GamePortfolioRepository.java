package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.GamePortfolio;
import tn.zeros.marketmaster.entity.User;

import java.util.List;
import java.util.Optional;

public interface GamePortfolioRepository extends JpaRepository<GamePortfolio,Long> {

    List<GamePortfolio> findByUser(User user);

    List<GamePortfolio> findByGame(Game game);

    List<GamePortfolio> findByUserUsername(String username);

    Optional<GamePortfolio> findByUserAndGame(User user, Game game);

    Optional<GamePortfolio> findByUserUsernameAndGameId(String username, Long gameId);

    List<GamePortfolio> findByGameId(Long gameId);
}
