package searchengine.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.repository.PageRepository;

@Repository
@RequiredArgsConstructor
public class JpaPageRepository implements PageRepository {
    private final SpringDataPageRepository jpa;

    @Override
    public PageEntity exists(String path, long siteId) {
        return jpa.findByPathAndSiteId(path, siteId);
    }

    @Override
    public void delete(PageEntity page) {
        jpa.delete(page);
    }

    @Override
    public int countBySite(SiteEntity siteEntity) {
        return jpa.countBySite(siteEntity);
    }

    @Override
    public void deleteAll() {
        jpa.deleteAllInBatch();
    }

    @Override
    public PageEntity save(PageEntity pageEntity) {
        return jpa.save(pageEntity);
    }

    @Override
    public void flush() {
        jpa.flush();
    }
}