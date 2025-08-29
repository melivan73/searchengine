package searchengine.domain.repository;

import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;

import java.util.List;

public interface PageRepository {
    long countPagesByLemmasIn(List<LemmaEntity> lemmas);

    int countBySite(SiteEntity siteEntity);

    void deleteAll();

    PageEntity save(PageEntity pageEntity);
}
