package dev.hyein.article.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hyein.article.app.request.SearchRequest;
import dev.hyein.article.app.request.ArticleRequest;
import dev.hyein.article.app.response.ErrorResponse;
import dev.hyein.article.app.service.ArticleService;
import dev.hyein.article.app.utils.ErrorHandlingUtils;
import dev.hyein.article.elasticsearch.vo.ArticleVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {
    private final ArticleService articleService;
    private final ObjectMapper objectMapper;

    /**
     * 아티클 검색
     * @param searchRequest
     * @return
     * @throws IOException
     */
    @GetMapping
    public ResponseEntity getArticles(@ModelAttribute SearchRequest searchRequest) throws IOException {
        log.info("[Request] Search articles: {}", searchRequest.toString());
        List<ArticleVo> articleVoList = articleService.getArticles(searchRequest);
        log.info("[Response] Articles count: {}", articleVoList.size());
        return ResponseEntity.ok(articleVoList);
    }

    /**
     * 아티클 단건 조회
     * @param articleNumber
     * @return
     * @throws IOException
     */
    @GetMapping("/{articleNumber}")
    public ResponseEntity getArticle(@PathVariable String articleNumber) throws IOException {
        log.info("[Request] Search article [{}]", articleNumber);
        ArticleVo articleVo = articleService.getArticle(Integer.parseInt(articleNumber));
        log.info("[Response] article: {}", articleVo.toString());
        return ResponseEntity.ok(articleVo);
    }

    /**
     * 아티클 등록
     * @param articleRequest
     * @return
     * @throws IOException
     */
    @PostMapping
    public ResponseEntity writeArticle(@RequestBody @Valid ArticleRequest articleRequest) throws IOException {
        log.info("[Request] Write article [{}]", articleRequest.toString());
        ArticleVo articleVo = objectMapper.convertValue(articleRequest, ArticleVo.class);
        articleService.writeArticle(articleVo);
        return ResponseEntity.ok("");
    }

    /**
     * 아티클 수정
     * @param articleNumber
     * @param articleRequest
     * @return
     * @throws IOException
     */
    @PutMapping("/{articleNumber}")
    public ResponseEntity updateArticle(@PathVariable Integer articleNumber, @RequestBody @Valid ArticleRequest articleRequest) throws IOException {
        log.info("[Request] Update article [{}] to {}", articleNumber, articleRequest.toString());
        ArticleVo articleVo = objectMapper.convertValue(articleRequest, ArticleVo.class);
        articleService.updateArticle(articleNumber, articleVo);
        return ResponseEntity.ok("");
    }

    /**
     * 아티클 삭제
     * @param articleNumber
     * @return
     * @throws IOException
     */
    @DeleteMapping("/{articleNumber}")
    public ResponseEntity deleteArticle(@PathVariable Integer articleNumber) throws IOException {
        log.info("[Request] Delete article [{}]", articleNumber);
        articleService.deleteArticle(articleNumber);
        return ResponseEntity.ok("");
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e) {
        log.error("[Api Exception] {}", ErrorHandlingUtils.getErrorStackTrace(e, 3));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(objectMapper.convertValue(new ErrorResponse(e.toString()), Map.class));
    }
}
