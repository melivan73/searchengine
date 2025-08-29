package searchengine.domain.repository;

import searchengine.domain.model.SiteEntity;
import searchengine.domain.model.SiteStatus;

import java.util.List;
import java.util.Set;

public interface SiteRepository {
    boolean existsByStatus(SiteStatus status);

    void updateStatusWhereIn(Set<SiteStatus> fromStatuses, SiteStatus toStatus);

    SiteEntity findByUrlMatching(String pageUrl);

    List<SiteEntity> findAll();

    SiteEntity save(SiteEntity siteEntity);

    void deleteAll();
}
