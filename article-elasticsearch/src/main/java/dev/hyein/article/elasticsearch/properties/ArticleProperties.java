package dev.hyein.article.elasticsearch.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "elasticsearch.article")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ArticleProperties {
    private String alias;
    private int port;
    private String host;
    private int connectionTimeout;
    private int socketTimeout;
    private int connectionRequestTimeout;
}
