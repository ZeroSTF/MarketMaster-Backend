package tn.zeros.marketmaster.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NewsArticleResponseDto {
    private Long id;
    private String category;
    private String headline;
    private String source;
    private String related;
    private String url;
    private String image;
    private LocalDateTime publishedDate;
}

