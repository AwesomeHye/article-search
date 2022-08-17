package dev.hyein.article.app.request;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @ToString
public class SearchRequest {
    private String articleTitle = "";
    private String sort = "";
    private String filter = "";
}
