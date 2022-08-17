package dev.hyein.article.elasticsearch.dto;

import lombok.*;

@AllArgsConstructor @NoArgsConstructor @Getter @Setter @Builder @ToString
public class ArticleDto {
    private Integer articleNumber;
    private String articleTitle;
    private int read;
    private Boolean isOpen;
}
