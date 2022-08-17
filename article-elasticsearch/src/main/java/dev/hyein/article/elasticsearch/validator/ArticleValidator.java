package dev.hyein.article.elasticsearch.validator;

import dev.hyein.article.elasticsearch.dao.CommonEsDao;
import dev.hyein.article.elasticsearch.exception.DocumentAlreadyExistException;
import dev.hyein.article.elasticsearch.exception.DocumentNotFoundException;
import dev.hyein.article.elasticsearch.properties.ArticleProperties;
import dev.hyein.article.elasticsearch.vo.ArticleVo;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * 아티클 데이터 검증 클래스
 */
@Component
public class ArticleValidator {
    private final RestHighLevelClient client;
    private final String alias;

    public ArticleValidator(@Qualifier("articleClient") RestHighLevelClient articleClient, ArticleProperties articleProperties)  {
        this.client = articleClient;
        this.alias = articleProperties.getAlias();
    }
    /**
     * ArticleVo 검증
     * @param articleVo
     */
    public void validateArticleVo(ArticleVo articleVo) {
        Objects.requireNonNull(articleVo, "ArticleVo must be not null.");
        Objects.requireNonNull(articleVo.getArticleNumber(), "ArticleNumber must be not null.");
        if(!StringUtils.hasText(articleVo.getArticleTitle()))
            throw new NullPointerException("articleTitle must be not null or empty.");
    }

    /**
     * docId 가 존재하지 않는지 검증
     * @param docId
     * @throws IOException
     */
    public void validateIsDocIdNotExist(Integer docId) throws IOException {
        validateIsDocIdNotExist(String.valueOf(docId));
    }

    /**
     * docId 가 존재하지 않는지 검증
     * @param docId
     * @throws IOException
     */
    public void validateIsDocIdNotExist(String docId) throws IOException {
        Objects.requireNonNull(docId);

        if (CommonEsDao.isDocIdExist(client, alias, docId)) {
            throw new DocumentAlreadyExistException(docId, alias);
        }
    }

    /**
     * docId 가 존재하는지 검증
     * @param articleNumber
     * @throws IOException
     */
    public void validateIsDocIdExist(Integer articleNumber) throws IOException {
        Objects.requireNonNull(articleNumber);

        String docId = String.valueOf(articleNumber);
        if (!CommonEsDao.isDocIdExist(client, alias, docId)) {
            throw new DocumentNotFoundException(docId, alias);
        }
    }
}
