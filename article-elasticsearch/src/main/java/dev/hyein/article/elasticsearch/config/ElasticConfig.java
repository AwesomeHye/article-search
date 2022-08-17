package dev.hyein.article.elasticsearch.config;

import dev.hyein.article.elasticsearch.properties.ArticleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ElasticConfig {
    private final ArticleProperties articleProperties;

    @Bean("articleClient")
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(HttpHost.create(articleProperties.getHost()))
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout(articleProperties.getConnectionTimeout())
                                .setSocketTimeout(articleProperties.getSocketTimeout())
                                .setConnectionRequestTimeout(articleProperties.getConnectionRequestTimeout())
                )
        );
    }
}
