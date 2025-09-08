package searchengine.infrastructure.persistence;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.repository.IndexRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JpaIndexRepository implements IndexRepository {
    private final SpringDataIndexRepository jpa;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<LemmaEntity> getLemmasByPage(PageEntity page) {
        return jpa.getLemmasByPage(page);
    }

    public void deleteByPage(PageEntity page) {
        jpa.deleteByPage(page);
    }

    @Override
    public long countPagesByLemmaInAndSiteIn(
        List<String> lemmas,
        int lemmasCount,
        List<SiteEntity> sites) {
        return jpa.countPagesByLemmasInSitesIn(lemmas, lemmasCount, sites);
    }

    @Override
    public Page<Tuple> findPagesWithAbsRelevanceAndSiteIn(
        List<String> lemmas,
        int lemmasCount,
        List<SiteEntity> sites,
        Pageable pageable) {
        return jpa.findPagesWithAbsRelevanceAndSiteIn(lemmas, lemmasCount, sites, pageable);
    }

    @Override
    public Page<Tuple> findPagesWithAbsRelevance(List<LemmaEntity> lemmas,
                                                 int lemmasCount, Pageable pageable) {
        return jpa.findPagesWithAbsRelevance(lemmas, lemmasCount, pageable);
    }

    @Override
    @Retryable(retryFor = SQLException.class, maxAttempts = 6, backoff = @Backoff(delay = 400))
    @Transactional(propagation = Propagation.REQUIRED, isolation =
        Isolation.READ_COMMITTED)
    public void batchInsertIndexData(PageEntity page, Map<String, Integer> mapLemmaRank,
                                     List<LemmaEntity> lemmas) {
        String sql =
            "INSERT INTO page_index (page_id, lemma_id, index_rank) VALUES (?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();

        for (LemmaEntity lemmaEntity : lemmas) {
            String lemmaText = lemmaEntity.getLemma();
            Integer rank = mapLemmaRank.get(lemmaText);
            if (rank != null) {
                Object[] params = new Object[]{
                    page.getId(),
                    lemmaEntity.getId(),
                    rank
                };
                batchArgs.add(params);
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                sql,
                batchArgs,
                50,
                (ps, params) -> {
                    ps.setInt(1, (int) params[0]);
                    ps.setInt(2, (int) params[1]);
                    ps.setInt(3, (int) params[2]);
                }
            );
        }
    }

    @Override
    public void deleteAll() {
        jpa.deleteAllInBatch();
    }
}