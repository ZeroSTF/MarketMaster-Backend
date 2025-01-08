package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity

public class NewsArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
private String category ;
    @Column(length = 3000)
    private String headline;
    private String source;
    private String related;

    private String url;
    @Column(length = 3000)
    private String image;
    private LocalDateTime publishedDate;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;  // The relationship with the Game entity
}
