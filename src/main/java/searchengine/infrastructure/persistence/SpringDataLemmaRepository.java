package searchengine.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.SiteEntity;

import java.util.List;

public interface SpringDataLemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    @Transactional
    @Modifying
    @Query("UPDATE LemmaEntity l SET l.frequency = l.frequency - 1 WHERE " +
           "l IN :lemmas AND l.frequency > 0")
    void decrementFrequency(@Param("lemmas") List<LemmaEntity> lemmas);

    @Transactional
    @Modifying
    @Query("DELETE FROM LemmaEntity l WHERE l.frequency <= 0")
    void deleteEmptyLemmas();

    int countBySite(SiteEntity siteEntity);

    @Transactional(readOnly = true)
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma IN :lemmas AND l.site IN " +
           "(:siteEntities)")
    List<LemmaEntity> findByLemmaInAndSiteIn(@Param("lemmas") List<String> lemmas,
                                             @Param("siteEntities") List<SiteEntity> siteEntities);


    @Transactional(readOnly = true)
    @Query("SELECT l FROM LemmaEntity l WHERE l.lemma IN :lemmas AND l.site = " +
           ":siteEntity")
    List<LemmaEntity> findByLemmaInAndSite(@Param("lemmas") List<String> lemmas,
                                           @Param("siteEntity") SiteEntity siteEntity);

    void deleteAll();
}
