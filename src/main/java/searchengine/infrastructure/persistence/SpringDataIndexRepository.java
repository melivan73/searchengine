package searchengine.infrastructure.persistence;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    @Query("SELECT i.lemma FROM IndexEntity i WHERE i.page = :page")
    List<LemmaEntity> getLemmasByPage(@Param("page") PageEntity page);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM IndexEntity i WHERE i.page = :page")
    void deleteByPage(@Param("page") PageEntity page);

    @Query("""
             SELECT COUNT(p)
             FROM PageEntity p
             WHERE p.site IN :sites
               AND (
                 SELECT COUNT(DISTINCT i1.lemma.lemma)
                 FROM IndexEntity i1
                 WHERE i1.page = p
                   AND i1.lemma.site = p.site
                   AND i1.lemma.lemma IN :lemmaTexts
               ) = :lemmasCount
           """)
    int countPagesByLemmasInSitesIn(
        @Param("lemmaTexts") List<String> lemmaTexts,
        @Param("lemmasCount") long lemmasCount,
        @Param("sites") List<SiteEntity> sites
    );

    @Transactional(readOnly = true)
    @Query("""
             SELECT p AS page, SUM(i.indexRank) AS absRel
             FROM IndexEntity i
               JOIN i.page  p
               JOIN i.lemma l
             WHERE l.lemma IN :lemmaTexts
               AND l.site = p.site
               AND p.site IN :sites
             GROUP BY p
             HAVING COUNT(DISTINCT l.lemma) = :lemmasCount
             ORDER BY p.site.id ASC, SUM(i.indexRank) DESC, p.id DESC
           """)
    Page<Tuple> findPagesWithAbsRelevanceAndSiteIn(
        @Param("lemmaTexts") List<String> lemmaTexts,
        @Param("lemmasCount") long lemmasCount,
        @Param("sites") List<SiteEntity> sites,
        Pageable pageable
    );


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
