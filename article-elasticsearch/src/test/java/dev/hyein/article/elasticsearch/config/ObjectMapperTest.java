package dev.hyein.article.elasticsearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hyein.article.elasticsearch.dto.ArticleDto;
import dev.hyein.article.elasticsearch.vo.ArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class ObjectMapperTest {

    @Autowired
    ObjectMapper objectMapper;

    @DisplayName("Vo -> Dto 변환")
    @Test
    public void vo2Dto() {
        //given
        ArticleVo articleVo = ArticleVo.builder()
                .articleNumber(2)
                .articleTitle("ARTICLE2")
                .read(40)
                .isOpen(true)
                .build();

        //when
        ArticleDto articleDto = objectMapper.convertValue(articleVo, ArticleDto.class);

        //then
        assertThat(articleVo).isEqualToComparingFieldByField(articleDto);
    }

    @DisplayName("Dto -> Vo 변환")
    @Test
    public void dto2Vo() {
        //given
        ArticleDto articleDto = ArticleDto.builder()
                .articleNumber(2)
                .articleTitle("ARTICLE2")
                .read(40)
                .isOpen(true)
                .build();

        //when
        ArticleVo articleVo = objectMapper.convertValue(articleDto, ArticleVo.class);

        //then
        assertThat(articleDto).isEqualToComparingFieldByField(articleVo);
    }

}