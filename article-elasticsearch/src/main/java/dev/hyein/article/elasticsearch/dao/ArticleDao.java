package dev.hyein.article.elasticsearch.dao;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hyein.article.elasticsearch.dto.ArticleDto;
import dev.hyein.article.elasticsearch.exception.DocumentNotFoundException;
import dev.hyein.article.elasticsearch.properties.ArticleProperties;
import dev.hyein.article.elasticsearch.validator.ArticleValidator;
import dev.hyein.article.elasticsearch.vo.ArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class ArticleDao {
    private final ObjectMapper objectMapper;
    private final RestHighLevelClient client;
    private final ArticleValidator articleValidator;
    private final String alias;

    public ArticleDao(ObjectMapper objectMapper, @Qualifier("articleClient") RestHighLevelClient articleClient, ArticleValidator articleValidator, ArticleProperties articleProperties) throws IOException {
        this.objectMapper = objectMapper;
        this.client = articleClient;
        this.articleValidator = articleValidator;
        this.alias = articleProperties.getAlias();
    }

    /**
     * 아티클 검색
     * @param searchSourceBuilder
     * @return articleVo list
     * @throws IOException
     */
    public List<ArticleVo> searchArticles(SearchSourceBuilder searchSourceBuilder) throws IOException {
        Objects.requireNonNull(searchSourceBuilder);

        List<ArticleVo> articleVoList = new ArrayList<>();
        SearchResponse searchResponse = CommonEsDao.searchDocument(client, alias, searchSourceBuilder);
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            articleVoList.add(objectMapper.convertValue(hit.getSourceAsMap(), ArticleVo.class));
        }
        return articleVoList;
    }

    /**
     * 아티클 번호가 일치하는 아티클 반환
     * @param docId
     * @return articleVo
     * @throws IOException
     */
    public ArticleVo findArticleById(Integer docId) throws IOException {
        Objects.requireNonNull(docId);

        GetResponse getResponse = CommonEsDao.findDocumentById(client, alias, String.valueOf(docId));
        if(!getResponse.isExists()) {
            throw new DocumentNotFoundException(String.valueOf(docId), alias);
        }
        return objectMapper.convertValue(getResponse.getSourceAsMap(), ArticleVo.class);
    }

    /**
     * 아티클 정보 색인
     * @param articleVo
     * @return response
     * @throws IOException
     */
    public IndexResponse index(ArticleVo articleVo) throws IOException {
        articleValidator.validateArticleVo(articleVo);

        ArticleDto articleDto = objectMapper.convertValue(articleVo, ArticleDto.class);
        articleValidator.validateIsDocIdNotExist(articleDto.getArticleNumber());

        return CommonEsDao.indexDocument(client, alias, String.valueOf(articleDto.getArticleNumber()), objectMapper.convertValue(articleDto, Map.class));
    }


    /**
     * 아티클 정보 업데이트
     * @param docId
     * @param articleVo
     * @return response
     * @throws IOException
     */
    public DocWriteResponse update(Integer docId, ArticleVo articleVo) throws IOException {
        articleValidator.validateArticleVo(articleVo);
        articleValidator.validateIsDocIdExist(docId);

        ArticleDto articleDto = objectMapper.convertValue(articleVo, ArticleDto.class);
        String currentDocId = String.valueOf(docId);
        String futureDocId = String.valueOf(articleDto.getArticleNumber());

        if(isDocIdUpdated(currentDocId, futureDocId)) {
            // 아티클번호를 수정할 경우
            articleValidator.validateIsDocIdNotExist(futureDocId);
            return updateNewDocIdDocument(articleDto, currentDocId, futureDocId);
        }

        return CommonEsDao.updateDocument(client, alias, currentDocId, objectMapper.convertValue(articleDto, Map.class));
    }

    /**
     * _id 가 currentDocId 인 아티클 삭제 후 futureDocId 인 아티클 새로 색인
     * @param articleDto 색인할 데이터
     * @param currentDocId 삭제할 doc id
     * @param futureDocId 색인할 doc id
     * @return
     * @throws IOException
     */
    private IndexResponse updateNewDocIdDocument(ArticleDto articleDto, String currentDocId, String futureDocId) throws IOException {
        CommonEsDao.deleteDocument(client, alias, currentDocId); // _id는 수정이 안돼서 delete & index
        return CommonEsDao.indexDocument(client, alias, futureDocId, objectMapper.convertValue(articleDto, Map.class));
    }

    /**
     * 아티클 번호 변경 여부
     * @param currentDocId
     * @param futureDocId
     * @return
     */
    private boolean isDocIdUpdated(String currentDocId, String futureDocId) {
        return !currentDocId.equals(futureDocId);
    }

    /**
     * 아티클 삭제
     * @param docId
     * @return response
     * @throws IOException
     */
    public DeleteResponse delete(Integer docId) throws IOException {
        articleValidator.validateIsDocIdExist(docId);

        return CommonEsDao.deleteDocument(client, alias, String.valueOf(docId));
    }

    /**
     * 아티클 인덱스 생성
     * @param mappings
     * @return response
     * @throws IOException
     */
    public CreateIndexResponse createIndex(String mappings) throws IOException {
        Objects.requireNonNull(mappings);

        return CommonEsDao.createIndex(client, alias, mappings);
    }

    /**
     * 모든 아티클 삭제
     * @return
     * @throws IOException
     */
    public BulkByScrollResponse deleteAllDocument() throws IOException {
        return CommonEsDao.deleteAllDocument(client, alias);
    }


}
