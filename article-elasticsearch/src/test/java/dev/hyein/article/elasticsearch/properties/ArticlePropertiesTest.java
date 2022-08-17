package dev.hyein.article.elasticsearch.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ArticlePropertiesTest {
    @Autowired
    ArticleProperties articleProperties;

    @Test
    void notNull() {
        assertNotNull(articleProperties.getAlias());
    }
}