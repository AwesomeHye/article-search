package dev.hyein.article.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hyein.article.elasticsearch.dao.ArticleDao;
import dev.hyein.article.elasticsearch.exception.DocumentAlreadyExistException;
import dev.hyein.article.elasticsearch.exception.DocumentNotFoundException;
import dev.hyein.article.elasticsearch.vo.ArticleVo;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class ArticleControllerTest {
    public static final int WAIT_EVENT_MS = 1000;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ArticleDao articleDao;

    @AfterEach
    public void deleteAllDocuments() throws IOException, InterruptedException {
        articleDao.deleteAllDocument();
        Thread.sleep(WAIT_EVENT_MS); // wait until deleting
    }

    @DisplayName("????????? ??????_??????")
    @Order(1)
    @ParameterizedTest
    @MethodSource
    public void writeArticle(ArticleVo articleVo) throws Exception {
        // when & then
        mockMvc.perform(post("/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articleVo))
        )
                .andExpect(status().isOk())
                .andDo(print());
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        assertThat(articleVo).isEqualToComparingFieldByField(articleDao.findArticleById(articleVo.getArticleNumber()));
    }

    public static Stream<Arguments> writeArticle() {
        // given
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true))
        );
    }

    @DisplayName("????????? ?????? ?????? ??????_??????")
    @Order(2)
    @ParameterizedTest
    @MethodSource
    public void writeDuplicateArticle(ArticleVo articleVo) throws Exception {
        // given
        writeArticle(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(post("/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articleVo))
        )
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DocumentAlreadyExistException))
        ;
    }

    public static Stream<Arguments> writeDuplicateArticle() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true))
        );
    }

    @DisplayName("???????????? ?????? ????????? ????????? ??????_??????")
    @Order(3)
    @ParameterizedTest
    @MethodSource
    public void writeInvalidArticle(ArticleVo articleVo) throws Exception {
        // when & then
        mockMvc.perform(post("/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articleVo))
        )
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
        ;
    }

    public static Stream<Arguments> writeInvalidArticle() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "?????????????????????", 9999, true)),
                Arguments.of(new ArticleVo(null, "?????????????????????", 9999, true)),
                Arguments.of(new ArticleVo(null, null, 9999, true))
        );
    }

    @DisplayName("????????? ??????_????????? ????????? ???????????? 0???_??????")
    @Order(4)
    @Test
    public void getNoDataArticles() throws Exception {
        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
        ;
    }

    @DisplayName("????????? ??????_?????? ?????? ??????_??????")
    @Order(5)
    @ParameterizedTest
    @MethodSource
    public void getNoSearchArticles(ArticleVo articleVo, String searchWord) throws Exception {
        // given
        writeArticle(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(get("/articles")
                .param("articleTitle", searchWord)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
        ;

        assertThat(articleVo).isEqualToComparingFieldByField(articleDao.findArticleById(articleVo.getArticleNumber()));
    }

    public static Stream<Arguments> getNoSearchArticles() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true), "??????")
        );
    }

    @DisplayName("????????? ??????_?????? ?????? ??????_??????")
    @Order(6)
    @ParameterizedTest
    @MethodSource
    public void getHasSearchArticles(ArticleVo articleVo, String searchWord) throws Exception {        // given
        // given
        writeArticle(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(get("/articles")
                .param("articleTitle", searchWord)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].articleNumber").value(articleVo.getArticleNumber()))
                .andExpect(jsonPath("$[0].articleTitle").value(articleVo.getArticleTitle()))
        ;

        assertThat(articleVo).isEqualToComparingFieldByField(articleDao.findArticleById(articleVo.getArticleNumber()));
    }

    public static Stream<Arguments> getHasSearchArticles() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true), "?????????")
        );
    }

    @DisplayName("????????? ??????_????????? ??????_??????")
    @Order(7)
    @ParameterizedTest
    @MethodSource
    public void readSortArticles(List<ArticleVo> articleVoList, Integer[] sortedArticleNumbers) throws Exception {
        assertEquals(articleVoList.size(), sortedArticleNumbers.length);
        // given
        for (ArticleVo articleVo : articleVoList) {
            writeArticle(articleVo);
        }
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when
        ResultActions perform = mockMvc.perform(get("/articles")
                .param("sort", "READ")
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(articleVoList.size())))
        ;
        for (int i = 0; i < articleVoList.size(); i++) { // ????????? ?????? ??????
            perform.andExpect(jsonPath("$["+i+"].articleNumber").value(sortedArticleNumbers[i]));
        }
    }

    public static Stream<Arguments> readSortArticles() {
        List<ArticleVo> articleVoList = new ArrayList<>();
        articleVoList.add(new ArticleVo(1, "???", 50, true));
        articleVoList.add(new ArticleVo(2, "??????", 20, false));
        articleVoList.add(new ArticleVo(3, "?????????", 2, true));

        // given
        return Stream.of(
                Arguments.of(articleVoList, new Integer[]{1, 2, 3})
        );
    }

    @DisplayName("????????? ??????_?????????_??????")
    @Order(8)
    @ParameterizedTest
    @MethodSource
    public void openFilterArticles(List<ArticleVo> articleVoList) throws Exception {
        // given
        for (ArticleVo articleVo : articleVoList) {
            writeArticle(articleVo);
        }
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(get("/articles")
                .param("filter", "OPEN")
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(Long.valueOf(articleVoList.stream().filter(o -> o.getIsOpen()).count()).intValue())))
        ;
    }

    public static Stream<Arguments> openFilterArticles() {
        List<ArticleVo> articleVoList = new ArrayList<>();
        articleVoList.add(new ArticleVo(1, "???", 50, true));
        articleVoList.add(new ArticleVo(2, "??????", 20, false));
        articleVoList.add(new ArticleVo(3, "?????????", 2, true));

        return Stream.of(
                Arguments.of(articleVoList)
        );
    }

    @DisplayName("????????? ??????_?????????&?????????&??????_??????")
    @Order(9)
    @ParameterizedTest
    @MethodSource
    public void searchReadOpenArticles(List<ArticleVo> articleVoList, String searchWord, int searchCount, Integer[] sortedArticleNumbers) throws Exception {
        assertEquals(searchCount, sortedArticleNumbers.length);
        // given
        for (ArticleVo articleVo : articleVoList) {
            writeArticle(articleVo);
        }
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when
        ResultActions perform = mockMvc.perform(get("/articles")
                .param("articleTitle", searchWord)
                .param("filter", "OPEN")
                .param("sort", "READ")
                .contentType(MediaType.APPLICATION_JSON)
        )
                ;

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(searchCount)))
                .andDo(print())
        ;

        for (int i = 0; i < searchCount; i++) { // ????????? ?????? ??????
            perform.andExpect(jsonPath("$["+i+"].articleNumber").value(sortedArticleNumbers[i]));
        }
    }

    public static Stream<Arguments> searchReadOpenArticles() {
        List<ArticleVo> articleVoList = new ArrayList<>();
        articleVoList.add(new ArticleVo(1, "??????", 50, true));
        articleVoList.add(new ArticleVo(2, "????????????", 30, false));
        articleVoList.add(new ArticleVo(3, "??????????????????", 20, true));
        articleVoList.add(new ArticleVo(4, "????????????", 2, true));

        // given
        return Stream.of(
                Arguments.of(articleVoList, "??????", 2, new Integer[]{1, 3})
        );
    }

    @DisplayName("????????? ?????? ??????_??????")
    @Order(10)
    @ParameterizedTest
    @MethodSource
    public void getArticleThatExist(ArticleVo articleVo, Integer articleId) throws Exception {
        // given
        writeArticle(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(get("/articles/{articleNumber}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("articleNumber").value(articleVo.getArticleNumber()))
                .andExpect(jsonPath("articleTitle").value(articleVo.getArticleTitle()))
                .andExpect(jsonPath("read").value(articleVo.getRead()))
                .andExpect(jsonPath("isOpen").value(articleVo.getIsOpen()))
        ;
    }

    public static Stream<Arguments> getArticleThatExist() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true), 1232)
        );
    }


    @DisplayName("?????? ????????? ?????? ??????_??????")
    @Order(11)
    @ParameterizedTest
    @MethodSource
    public void getArticleThatNotExist(ArticleVo articleVo, Integer articleId) throws Exception {
        // given
        writeArticle(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(get("/articles/{articleNumber}", articleId))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DocumentNotFoundException))
        ;

    }

    public static Stream<Arguments> getArticleThatNotExist() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true), 5)
        );
    }

    @DisplayName("????????? ??????(????????? ?????? ?????? ??????)_??????")
    @Order(12)
    @ParameterizedTest
    @MethodSource
    public void updateArticleExceptArticleNumber(ArticleVo oldArticleVo, ArticleVo newArticleVo) throws Exception {
        // given
        writeArticle(oldArticleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(put("/articles/{articleNumber}", oldArticleVo.getArticleNumber())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newArticleVo))
        )
                .andExpect(status().isOk())
        ;
        Thread.sleep(WAIT_EVENT_MS); // wait until updating

        assertThat(articleDao.findArticleById(newArticleVo.getArticleNumber())).isEqualToComparingFieldByField(newArticleVo);
    }

    public static Stream<Arguments> updateArticleExceptArticleNumber() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true),
                        new ArticleVo(1232, "????????????", 1, false))
        );
    }

    @DisplayName("????????? ??????(????????? ?????? ?????? ??????)_??????")
    @Order(13)
    @ParameterizedTest
    @MethodSource
    public void updateArticleIncludeArticleNumber(ArticleVo oldArticleVo, ArticleVo newArticleVo) throws Exception {
        // given
        writeArticle(oldArticleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(put("/articles/{articleNumber}", oldArticleVo.getArticleNumber())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newArticleVo))
        )
                .andExpect(status().isOk())
        ;
        Thread.sleep(WAIT_EVENT_MS); // wait until updating

        assertThat(articleDao.findArticleById(newArticleVo.getArticleNumber())).isEqualToComparingFieldByField(newArticleVo);
    }

    public static Stream<Arguments> updateArticleIncludeArticleNumber() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true),
                        new ArticleVo(6, "????????????", 1, false))
        );
    }

    @DisplayName("?????? ????????? ??????_??????")
    @Order(14)
    @ParameterizedTest
    @MethodSource
    public void updateArticleThatNotExist(Integer oldArticleNumber, ArticleVo newArticleVo) throws Exception {
        // when & then
        mockMvc.perform(put("/articles/{articleNumber}", oldArticleNumber)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newArticleVo))
        )
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DocumentNotFoundException))
        ;
    }

    public static Stream<Arguments> updateArticleThatNotExist() {
        // given
        return Stream.of(
                Arguments.of(1, new ArticleVo(6, "????????????", 1, false))
        );
    }

    @DisplayName("?????? ???????????? ????????? ????????? ??????_??????")
    @Order(15)
    @ParameterizedTest
    @MethodSource
    public void updateArticleNumberThatAlreadyExist(ArticleVo oldArticleVo, ArticleVo newArticleVo) throws Exception {
        // given
        writeArticle(oldArticleVo);
        writeArticle(newArticleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(put("/articles/{articleNumber}", oldArticleVo.getArticleNumber())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newArticleVo))
        )
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DocumentAlreadyExistException))
        ;
    }

    public static Stream<Arguments> updateArticleNumberThatAlreadyExist() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true),
                        new ArticleVo(6, "????????????", 1, false))
        );
    }

    @DisplayName("????????? ??????_??????")
    @Order(16)
    @ParameterizedTest
    @MethodSource
    public void deleteArticleThatExist(ArticleVo articleVo, Integer articleId) throws Exception {
        // given
        writeArticle(articleVo);
        Thread.sleep(WAIT_EVENT_MS); // wait until indexing

        // when & then
        mockMvc.perform(delete("/articles/{articleNumber}", articleId))
                .andExpect(status().isOk())
        ;
        Thread.sleep(WAIT_EVENT_MS); // wait until deleting

        assertEquals(0, articleDao.searchArticles(new SearchSourceBuilder()).size());
    }

    public static Stream<Arguments> deleteArticleThatExist() {
        return Stream.of(
                Arguments.of(new ArticleVo(1232, "???????????????", 9999, true), 1232)
        );
    }


    @DisplayName("?????? ????????? ??????_??????")
    @Order(17)
    @ParameterizedTest
    @MethodSource
    public void deleteArticleThatNotExist(Integer articleId) throws Exception {
        // when & then
        mockMvc.perform(delete("/articles/{articleNumber}", articleId))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DocumentNotFoundException))
        ;
    }

    public static Stream<Arguments> deleteArticleThatNotExist() {
        // given
        return Stream.of(
                Arguments.of(5)
        );
    }




}
