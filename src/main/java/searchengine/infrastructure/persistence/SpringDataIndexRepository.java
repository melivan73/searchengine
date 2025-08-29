package searchengine.infrastructure.persistence;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.domain.model.IndexEntity;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;

import java.util.List;

public interface SpringDataIndexRepository extends JpaRepository<IndexEntity, Integer> {

    @Transactional(readOnly = true)
    @Query("""
             SELECT i.page AS page, SUM(i.indexRank) AS absRel
             FROM IndexEntity i
             JOIN i.lemma l
             WHERE l IN :lemmas
             GROUP BY i.page.id, i.page
             HAVING COUNT(DISTINCT l.id) = :lemmasCount
             ORDER BY SUM(i.indexRank) DESC, i.page.id DESC
           """)
    Page<Tuple> findPagesWithAbsRelevance(
        @Param("lemmas") List<LemmaEntity> lemmas,
        @Param("lemmasCount") int lemmasCount,
        Pageable pageable);

    @Transactional(readOnly = true)
    @Query(
        "SELECT i.page FROM IndexEntity i WHERE i.lemma = :lemma " +
        "AND i.lemma.site IN (:sites)")
    List<PageEntity> findPagesByLemmaAndSiteIn(@Param("lemma") LemmaEntity lemma,
                                               @Param("sites") List<SiteEntity> sites);

    @Transactional(readOnly = true)
    @Query("SELECT i.page FROM IndexEntity i WHERE i.lemma = :lemma")
    List<PageEntity> findPagesByLemma(@Param("lemma") LemmaEntity lemma);
}
