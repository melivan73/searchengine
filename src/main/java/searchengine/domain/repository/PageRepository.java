package searchengine.domain.repository;

import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;

public interface PageRepository {
    PageEntity exists(String path, long siteId);

    void delete(PageEntity page);

    int countBySite(SiteEntity siteEntity);

    void deleteAll();

    PageEntity save(PageEntity pageEntity);

    void flush();
}
