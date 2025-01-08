package tn.zeros.marketmaster.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsArticleDto {
    private String id ;
    private String category;
    private String headline;
    private String source;
    private String related;
    private String summary;
    private String url;
    private String image;
    private long datetime;

    public LocalDateTime getPublishedDate() {
        return Instant.ofEpochSecond(this.datetime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}