package dev.hyein.article.elasticsearch.dao;

import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CloseIndexRequest;
import org.elasticsearch.client.indices.CloseIndexResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.Map;

public class CommonEsDao {

    /**
     * 도큐먼트 검색
     * @param searchSourceBuilder
     * @return SearchResponse
     * @throws IOException
     */
    public static SearchResponse searchDocument(RestHighLevelClient client, String alias, SearchSourceBuilder searchSourceBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest(alias)
                .source(searchSourceBuilder)
                ;

        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    /**
     * 도큐먼트 ID 조회
     * @param client
     * @param alias
     * @param docId
     * @return GetResponse
     * @throws IOException
     */
    public static GetResponse findDocumentById(RestHighLevelClient client, String alias, String docId) throws IOException {
        GetRequest getRequest = new GetRequest(alias, docId);
        return client.get(getRequest, RequestOptions.DEFAULT);
    }

    /**
     * 도큐먼트 색인
     * @param docId
     * @param source
     * @throws IOException
     * @return IndexResponse
     */
    public static IndexResponse indexDocument(RestHighLevelClient client, String alias, String docId, Map<String, Object> source) throws IOException {
        IndexRequest indexRequest = new IndexRequest(alias)
                .id(docId)
                .source(source)
                ;
        return client.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 도큐먼트 변경
     * @param docId
     * @param source
     * @throws IOException
     * @return UpdateResponse
     */
    public static UpdateResponse updateDocument(RestHighLevelClient client, String alias, String docId, Map<String, Object> source) throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(alias, docId)
                .doc(source)
                .upsert()
                ;
        return client.update(updateRequest, RequestOptions.DEFAULT);
    }

    /**
     * 도큐먼트 삭제
     * @param docId
     * @throws IOException
     * @return DeleteResponse
     */
    public static DeleteResponse deleteDocument(RestHighLevelClient client, String alias, String docId) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(alias, docId);
        return client.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    /**
     * 인덱스 생성
     * @param client
     * @param index
     * @param mappings
     * @throws IOException
     * @return CreateIndexResponse
     */
    public static CreateIndexResponse createIndex(RestHighLevelClient client, String index, String mappings) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        createIndexRequest.source(mappings, XContentType.JSON);
        return client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 도큐먼트 전체 삭제
     * @param client
     * @param alias
     * @throws IOException
     * @return BulkByScrollResponse
     */
    public static BulkByScrollResponse deleteAllDocument(RestHighLevelClient client, String alias) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(alias);
        deleteByQueryRequest.setQuery(QueryBuilders.matchAllQuery());
        return client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
    }

    /**
     * 도큐먼트 ID 존재 여부
     * @param client
     * @param alias
     * @param docId
     * @return 존재하면 true, 존재 안 하면 false
     * @throws IOException
     */
    public static boolean isDocIdExist(RestHighLevelClient client, String alias, String docId) throws IOException {
        GetRequest getRequest = new GetRequest(alias, docId);
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");

        return client.exists(getRequest, RequestOptions.DEFAULT);
    }

    /**
     * 인덱스 close & open
     * @param client
     * @param alias
     * @throws IOException
     */
    public static void reloadIndex(RestHighLevelClient client, String alias) throws IOException {
        closeIndex(client, alias); // 순단 현상 발생
        openIndex(client, alias);
    }

    /**
     * 인덱스 close
     * @param client
     * @param alias
     * @return CloseIndexResponse
     * @throws IOException
     */
    private static CloseIndexResponse closeIndex(RestHighLevelClient client, String alias) throws IOException {
        CloseIndexRequest closeIndexRequest = new CloseIndexRequest(alias);
        return client.indices().close(closeIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 인덱스 open
     * @param client
     * @param alias
     * @return OpenIndexResponse
     * @throws IOException
     */
    private static OpenIndexResponse openIndex(RestHighLevelClient client, String alias) throws IOException {
        OpenIndexRequest openIndexRequest = new OpenIndexRequest(alias);
        return client.indices().open(openIndexRequest, RequestOptions.DEFAULT);
    }
}
