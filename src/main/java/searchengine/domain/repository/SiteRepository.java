package searchengine.domain.repository;

import searchengine.application.services.ErrorMessage;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.model.SiteStatus;

import java.util.List;
import java.util.Set;

public interface SiteRepository {
    boolean existsByStatus(SiteStatus status);

    void updateStatusWhereIn(Set<SiteStatus> fromStatuses, SiteStatus toStatus);

    void updateErrMessage(Set<SiteStatus> statuses, ErrorMessage err);

    SiteEntity findByUrlMatching(String pageUrl);

    List<SiteEntity> findAll();

    SiteEntity save(SiteEntity siteEntity);

    void deleteAll();
}
