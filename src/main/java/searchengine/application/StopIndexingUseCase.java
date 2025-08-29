package searchengine.application;

import lombok.RequiredArgsConstructor;
import searchengine.domain.model.SiteStatus;
import searchengine.domain.repository.SiteRepository;

import java.util.Set;

@RequiredArgsConstructor
public class StopIndexingUseCase {
    private final SiteRepository siteRepository;
    private final IndexingManager indexingManager;

    public boolean execute() {
        if (!siteRepository.existsByStatus(SiteStatus.INDEXING)) {
            return false;
        }

        indexingManager.stop();
        siteRepository.updateStatusWhereIn(Set.of(SiteStatus.INDEXING), SiteStatus.FAILED);
        return true;
    }
}