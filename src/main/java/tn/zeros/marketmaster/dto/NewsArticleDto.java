package tn.zeros.marketmaster.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class NewsArticleDto {
    private String id ;
    private String category;
    private String headline;
    private String source;
    private String related;
    private String summary;
    private String url;
    private String image;
    private long datetime;  // The Unix timestamp (seconds)

    // You can add methods to convert the Unix timestamp to LocalDateTime if needed
    public LocalDateTime getPublishedDate() {
        return Instant.ofEpochSecond(this.datetime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}