package searchengine.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import searchengine.application.services.ErrorMessage;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.model.SiteStatus;
import searchengine.domain.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JpaSiteRepository implements SiteRepository {
    private final SpringDataSiteRepository jpa;

    @Override
    public boolean existsByStatus(SiteStatus status) {
        return jpa.existsByStatus(status);
    }

    @Override
    public void updateStatusWhereIn(Set<SiteStatus> fromStatuses, SiteStatus toStatus) {
        jpa.updateStatuses(fromStatuses, toStatus, LocalDateTime.now());
    }

    @Override
    public void updateErrMessage(Set<SiteStatus> statuses, ErrorMessage err) {
        jpa.updateErrMessage(statuses, err.getErrorMessage());
    }

    @Override
    public SiteEntity findByUrlMatching(String pageUrl) {
        return jpa.findByUrlMatching(pageUrl);
    }

    @Override
    public List<SiteEntity> findAll() {
        return jpa.findAll();
    }

    @Override
    public SiteEntity save(SiteEntity siteEntity) {
        return jpa.save(siteEntity);
    }

    @Override
    public void deleteAll() {
        jpa.deleteAllInBatch();
    }
}