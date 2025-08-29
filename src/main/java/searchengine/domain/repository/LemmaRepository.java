package searchengine.domain.repository;

import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.SiteEntity;

import java.util.List;

public interface LemmaRepository {
    int countBySite(SiteEntity siteEntity);

    List<LemmaEntity> findByLemmaInAndSiteIn(List<String> lemmas,
        List<SiteEntity> siteEntities);

    List<LemmaEntity> findByLemmaInAndSite(List<String> lemmas, SiteEntity siteEntity);

    void deleteAll();

    void batchInsertLemmas(List<String> lemmas, SiteEntity siteEntity);
}