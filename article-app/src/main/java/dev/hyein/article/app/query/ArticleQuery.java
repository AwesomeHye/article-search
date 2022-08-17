package dev.hyein.article.app.query;

import dev.hyein.article.app.request.SearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.StringUtils;

@Slf4j
public class ArticleQuery {
    /**
     * 아티클 목록 검색 쿼리 생성
     * @param searchRequest
     * @return
     */
    public static SearchSourceBuilder getSearchArticlesQuery(SearchRequest searchRequest) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        appendArticleTitleQueryIfValid(query, searchRequest.getArticleTitle());
        appendFilterQueryIfValid(query, searchRequest.getFilter());

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(query)
                ;
        appendSortIfValid(searchSourceBuilder, searchRequest.getSort());

        return searchSourceBuilder;
    }

    /**
     * 검색 쿼리 생성
     * @param queryBuilder
     * @param articleTitle
     */
    private static void appendArticleTitleQueryIfValid(BoolQueryBuilder queryBuilder, String articleTitle) {
        if(!StringUtils.hasText(articleTitle))
            return;

        queryBuilder
                .must(QueryBuilders.boolQuery()
                        .should(QueryBuilders.termQuery("articleTitle.standard", articleTitle)) // 기본 노리 형태소 분석 필드
                        .should(QueryBuilders.termQuery("articleTitle.dic", articleTitle)) // 사용자 사전 반영 필드
                )
        ;
    }

    /**
     * 필터 쿼리 생성
     * @param queryBuilder
     * @param filter
     */
    private static void appendFilterQueryIfValid(BoolQueryBuilder queryBuilder, String filter) {
        if(!StringUtils.hasText(filter))
            return;

        switch (filter.toUpperCase()) {
            case "OPEN":
                queryBuilder.must(QueryBuilders.termQuery("isOpen", true));
                break;
            default:
                log.warn("Invalid filter: {}", filter);
        }
    }

    /**
     * 정렬 쿼리 생성
     * @param searchSourceBuilder
     * @param sort
     */
    private static void appendSortIfValid(SearchSourceBuilder searchSourceBuilder, String sort) {
        if(!StringUtils.hasText(sort))
            return;

        switch (sort.toUpperCase()) {
            case "READ":
                searchSourceBuilder.sort(getReadSortBuilder());
                break;
            default:
                log.warn("Invalid sort: {}", sort);
        }
    }

    /**
     * 조회순 정렬 스크립트 쿼리 생성
     * @return
     */
    private static SortBuilder<ScriptSortBuilder> getReadSortBuilder() {
        Script script = new Script("doc['read'].value + doc['articleTitle'].value.length()"); // read 점수 + 아티클 제목의 길이
        return SortBuilders.scriptSort(script, ScriptSortBuilder.ScriptSortType.NUMBER).order(SortOrder.DESC);
    }

}
