package searchengine.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.repository.PageRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaPageRepository implements PageRepository {
    private final SpringDataPageRepository jpa;

    @Override
    public long countPagesByLemmasIn(List<LemmaEntity> lemmas) {
        return jpa.countPagesByLemmasIn(lemmas, lemmas.size());
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
}