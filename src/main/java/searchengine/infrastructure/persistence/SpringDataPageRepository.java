package searchengine.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;

public interface SpringDataPageRepository extends JpaRepository<PageEntity, Integer> {
    PageEntity findByPathAndSite(String path, SiteEntity site);

    int countBySite(SiteEntity siteEntity);

    void deleteAll();
}
