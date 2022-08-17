package dev.hyein.article.elasticsearch.dao;

import dev.hyein.article.elasticsearch.exception.DocumentAlreadyExistException;
import dev.hyein.article.elasticsearch.exception.DocumentNotFoundException;
import dev.hyein.article.elasticsearch.properties.ArticleProperties;
import dev.hyein.article.elasticsearch.vo.ArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(ContainerExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Slf4j
class ArticleDaoTest {
    public static final int WAIT_EVENT_MS = 1000;

    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private ArticleProperties articleProperties;

    @AfterEach
    public void deleteAllDocuments() throws IOException, InterruptedException {
        articleDao.deleteAllDocument();
        Thread.sleep(WAIT_EVENT_MS); // wait until deleting
    }

    @DisplayName("아티클 등록_성공")
    @ParameterizedTest
    @MethodSource
    @Order(1)
    public void index(ArticleVo articleVo) throws IOException, InterruptedException {
        // when
        articleDao.index(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // then
        List<ArticleVo> result = articleDao.searchArticles(new SearchSourceBuilder());
        assertEquals(1, result.size());
        assertThat(result.get(0)).isEqualToComparingFieldByField(articleVo);
    }

    private static Stream<Arguments> index() {
        //given
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "제목", 9999, true))
        );
    }

    @DisplayName("아티클 번호 중복 등록_실패")
    @ParameterizedTest
    @MethodSource
    @Order(2)
    public void indexDuplicatedId(ArticleVo articleVo) throws IOException, InterruptedException {
        // given
        articleDao.index(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        assertThrows(DocumentAlreadyExistException.class, () -> articleDao.index(articleVo));
    }

    private static Stream<Arguments> indexDuplicatedId() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "제목", 9999, true))
        );
    }

    @DisplayName("유효하지 않은 데이터 아티클 등록_실패")
    @ParameterizedTest
    @MethodSource
    @Order(3)
    public void indexInvalidDocument(ArticleVo articleVo) throws IOException, InterruptedException {
        // when & then
        assertThrows(NullPointerException.class, () -> articleDao.index(articleVo));
    }

    private static Stream<Arguments> indexInvalidDocument() {
        return Stream.of(
                Arguments.of(new ArticleVo(null, null, 9999, true)),
                Arguments.of(new ArticleVo(null, "제목", 9999, true)),
                Arguments.of(new ArticleVo(1232, "", 9999, true))
        );
    }

    @DisplayName("아티클 검색_검색 결과 없음_성공")
    @ParameterizedTest
    @MethodSource
    @Order(4)
    public void getNoSearchArticles(ArticleVo articleVo, String searchWord) throws IOException, InterruptedException {
        // given
        articleDao.index(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when
        List<ArticleVo> results = articleDao.searchArticles(new SearchSourceBuilder().query(
                QueryBuilders.boolQuery().must(
                        QueryBuilders.boolQuery()
                                .should(QueryBuilders.termQuery("articleTitle.standard", searchWord))
                                .should(QueryBuilders.termQuery("articleTitle.dic", searchWord))
                ))
        );

        // then
        assertEquals(0, results.size());
    }

    private static Stream<Arguments> getNoSearchArticles() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "제목", 9999, true), "초콜릿")
        );
    }

    @DisplayName("아티클 검색_검색 결과 있음_성공")
    @ParameterizedTest
    @MethodSource
    @Order(5)
    public void getHasSearchArticles(ArticleVo articleVo, String searchWord) throws IOException, InterruptedException {
        // given
        articleDao.index(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when
        List<ArticleVo> results = articleDao.searchArticles(new SearchSourceBuilder().query(
                QueryBuilders.boolQuery().must(
                        QueryBuilders.boolQuery()
                                .should(QueryBuilders.termQuery("articleTitle.standard", searchWord))
                                .should(QueryBuilders.termQuery("articleTitle.dic", searchWord))
                ))
        );

        // then
        assertEquals(1, results.size());
    }

    private static Stream<Arguments> getHasSearchArticles() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true), "초콜릿")
        );
    }

    @DisplayName("아티클 단건 조회_성공")
    @ParameterizedTest
    @MethodSource
    @Order(6)
    public void getArticleThatExist(ArticleVo articleVo, Integer articleId) throws IOException, InterruptedException {
        // given
        articleDao.index(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when
        ArticleVo result = articleDao.findArticleById(articleId);

        // then
        assertThat(result).isEqualToComparingFieldByField(articleVo);
    }

    private static Stream<Arguments> getArticleThatExist() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true), 1232)
        );
    }

    @DisplayName("없는 아티클 단건 조회_실패")
    @ParameterizedTest
    @MethodSource
    @Order(7)
    public void getArticleThatNotExist(ArticleVo articleVo, Integer articleId) throws IOException, InterruptedException {
        // given
        articleDao.index(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        assertThrows(DocumentNotFoundException.class, () -> articleDao.findArticleById(articleId));
    }

    private static Stream<Arguments> getArticleThatNotExist() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true), 2321)
        );
    }

    @DisplayName("아티클 수정(아티클 번호 수정 제외)_성공")
    @Order(8)
    @ParameterizedTest
    @MethodSource
    public void updateArticleExceptArticleNumber(ArticleVo oldArticleVo, ArticleVo newArticleVo) throws Exception {
        // given
        articleDao.index(oldArticleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when
        articleDao.update(oldArticleVo.getArticleNumber(), newArticleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until updating

        // then
        assertThat(articleDao.findArticleById(newArticleVo.getArticleNumber())).isEqualToComparingFieldByField(newArticleVo);
    }

    public static Stream<Arguments> updateArticleExceptArticleNumber() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true),
                        new ArticleVo(1232, "사탕이란", 1, false))
        );
    }

    @DisplayName("아티클 수정(아티클 번호 수정 포함)_성공")
    @Order(9)
    @ParameterizedTest
    @MethodSource
    public void updateArticleIncludeArticleNumber(ArticleVo oldArticleVo, ArticleVo newArticleVo) throws Exception {
        // given
        articleDao.index(oldArticleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when
        articleDao.update(oldArticleVo.getArticleNumber(), newArticleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until updating

        // then
        assertThat(articleDao.findArticleById(newArticleVo.getArticleNumber())).isEqualToComparingFieldByField(newArticleVo);
    }

    public static Stream<Arguments> updateArticleIncludeArticleNumber() {
        // given
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true),
                        new ArticleVo(6, "사탕이란", 1, false))
        );
    }

    @DisplayName("비정상 데이터 아티클 수정_실패")
    @Order(10)
    @ParameterizedTest
    @MethodSource
    public void updateInvalidArticle(ArticleVo oldArticleVo, ArticleVo newArticleVo) throws Exception {
        // given
        articleDao.index(oldArticleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        assertThrows(NullPointerException.class, () -> articleDao.update(oldArticleVo.getArticleNumber(), newArticleVo));
    }

    public static Stream<Arguments> updateInvalidArticle() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true),
                        new ArticleVo(null, "사탕이란", 1, false)),
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true),
                        new ArticleVo(null, "", 1, false)),
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true),
                        new ArticleVo(null, null, 1, false))
        );
    }

    @DisplayName("없는 아티클 수정_실패")
    @Order(11)
    @ParameterizedTest
    @MethodSource
    public void updateArticleThatNotExist(Integer oldArticleNumber, ArticleVo newArticleVo) {
        // when & then
       assertThrows(DocumentNotFoundException.class, () -> articleDao.update(oldArticleNumber, newArticleVo));
    }

    public static Stream<Arguments> updateArticleThatNotExist() {
        // given
        return Stream.of(
                Arguments.of(4, new ArticleVo(6, "사탕이란", 1, false))
        );
    }

    @DisplayName("이미 존재하는 아티클 번호로 수정_실패")
    @Order(12)
    @ParameterizedTest
    @MethodSource
    public void updateArticleNumberThatAlreadyExist(ArticleVo oldArticleVo, ArticleVo newArticleVo) throws Exception {
        // given
        articleDao.index(oldArticleVo);
        articleDao.index(newArticleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        assertThrows(DocumentAlreadyExistException.class, () -> articleDao.update(oldArticleVo.getArticleNumber(), newArticleVo));
    }

    public static Stream<Arguments> updateArticleNumberThatAlreadyExist() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true),
                        new ArticleVo(6, "사탕이란", 1, false))
        );
    }

    @DisplayName("아티클 삭제_성공")
    @Order(13)
    @ParameterizedTest
    @MethodSource
    public void deleteArticleThatExist(ArticleVo articleVo, Integer articleId) throws Exception {
        // given
        articleDao.index(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when
        articleDao.delete(articleId);
        Thread.sleep(WAIT_EVENT_MS); // wait until deleting

        // then
        assertThrows(DocumentNotFoundException.class, () -> articleDao.findArticleById(articleId));
    }

    public static Stream<Arguments> deleteArticleThatExist() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "초콜릿이란", 9999, true), 1232)
        );
    }


    @DisplayName("없는 아티클 삭제_실패")
    @Order(14)
    @ParameterizedTest
    @MethodSource
    public void deleteArticleThatNotExist(Integer articleId) {
        // when & then
        assertThrows(DocumentNotFoundException.class, () -> articleDao.delete(articleId));
    }

    public static Stream<Arguments> deleteArticleThatNotExist() {
        // given
        return Stream.of(
                Arguments.of(5)
        );
    }



}