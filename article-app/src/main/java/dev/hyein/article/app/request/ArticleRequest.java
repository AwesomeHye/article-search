package dev.hyein.article.app.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class ArticleRequest {
    @NotNull(message = "아티클번호 입력은 필수입니다.")
    private Integer articleNumber;
    @NotEmpty(message = "아티클명 입력은 필수입니다.")
    private String articleTitle;
    private int read;
    @NotNull(message = "오픈여부 입력은 필수입니다.")
    private Boolean isOpen;
}
