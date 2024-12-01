package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.GamePortfolio;
import tn.zeros.marketmaster.entity.User;

import java.util.List;
import java.util.Optional;

public interface GamePortfolioRepository extends JpaRepository<GamePortfolio,Long> {

    List<GamePortfolio> findByUser(User user);

    List<GamePortfolio> findByGame(Game game);

    List<GamePortfolio> findByUserUsername(String username);

    @Query("SELECT gp FROM GamePortfolio gp WHERE gp.user = :user AND gp.game = :game")
    Optional<GamePortfolio> findByUserAndGame(@Param("user") User user, @Param("game") Game game);

    @Query("SELECT gp FROM GamePortfolio gp WHERE gp.user = :user AND gp.game = :game")
    List<GamePortfolio> findAllByUserAndGame(@Param("user") User user, @Param("game") Game game);

    Optional<GamePortfolio> findByUserUsernameAndGameId(String username, Long gameId);

    List<GamePortfolio> findByGameId(Long gameId);
}
