package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.GameParticipation;
import tn.zeros.marketmaster.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface GameParticipationRepository extends JpaRepository<GameParticipation,Long> {
    boolean existsByGameAndUser(Game game, User user);
    List<GameParticipation> findByUser(User user);
    @Query("SELECT DISTINCT gp.user FROM GameParticipation gp")
    List<User> findAllUsers();

    List<GameParticipation> findByUserUsername(String username);
    @Query("SELECT gp.game FROM GameParticipation gp WHERE gp.user = :user")
    List<Game> findGamesByUser(@Param("user") User user);

    List<GameParticipation> findByGame(Game game);

    @Query("SELECT gp.lastPauseTimestamp " +
            "FROM GameParticipation gp " +
            "WHERE gp.user = :user AND gp.game.id = :gameId")
    LocalDateTime findLastPauseTimestamp(@Param("user") User user,
                                         @Param("gameId") Long gameId);
}
