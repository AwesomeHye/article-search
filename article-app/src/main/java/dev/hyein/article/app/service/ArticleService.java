package dev.hyein.article.app.service;

import dev.hyein.article.app.query.ArticleQuery;
import dev.hyein.article.app.request.SearchRequest;
import dev.hyein.article.elasticsearch.dao.ArticleDao;
import dev.hyein.article.elasticsearch.vo.ArticleVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {
    private final ArticleDao articleDao;

    public List<ArticleVo> getArticles(SearchRequest searchRequest) throws IOException {
        return articleDao.searchArticles(ArticleQuery.getSearchArticlesQuery(searchRequest));
    }

    public ArticleVo getArticle(Integer articleNumber) throws IOException {
        return articleDao.findArticleById(articleNumber);
    }

    public void writeArticle(ArticleVo articleVo) throws IOException {
        articleDao.index(articleVo);
    }

    public void updateArticle(Integer articleNumber, ArticleVo articleVo) throws IOException {
        articleDao.update(articleNumber, articleVo);
    }

    public void deleteArticle(Integer articleNumber) throws IOException {
        articleDao.delete(articleNumber);
    }
}
