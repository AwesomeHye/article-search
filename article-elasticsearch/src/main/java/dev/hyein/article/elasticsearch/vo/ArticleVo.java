package dev.hyein.article.elasticsearch.vo;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString @Builder
public class ArticleVo {
    private Integer articleNumber;
    private String articleTitle;
    private int read;
    private Boolean isOpen;
}
