package searchengine.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.repository.LemmaRepository;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaLemmaRepository implements LemmaRepository {
    private final SpringDataLemmaRepository jpa;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Override
    public int countBySite(SiteEntity siteEntity) {
        return jpa.countBySite(siteEntity);
    }

    @Override
    public List<LemmaEntity> findByLemmaInAndSiteIn(List<String> lemmas,
        List<SiteEntity> siteEntities) {
        return jpa.findByLemmaInAndSiteIn(lemmas, siteEntities);
    }

    @Override
    public List<LemmaEntity> findByLemmaInAndSite(List<String> lemmas, SiteEntity siteEntity) {
        return jpa.findByLemmaInAndSite(lemmas, siteEntity);
    }

    @Override
    @Retryable(retryFor = SQLException.class, maxAttempts = 6, backoff = @Backoff(delay = 400))
    @Transactional(propagation = Propagation.REQUIRED, isolation =
        Isolation.READ_COMMITTED)
    public void batchInsertLemmas(List<String> lemmas, SiteEntity siteEntity) {
        List<String> sortedLemmas = lemmas.stream()
            .sorted()
            .toList();
        jdbcTemplate.batchUpdate(
            "INSERT INTO lemma (lemma, site_id, frequency) VALUES (?, ?, 1)" +
            "ON DUPLICATE KEY UPDATE frequency = frequency + 1",
            sortedLemmas,
            50,
            (ps, val) -> {
                ps.setString(1, val);
                ps.setInt(2, siteEntity.getId());
            }
        );
    }

    @Override
    public void deleteAll() {
        jpa.deleteAllInBatch();
    }
}