package searchengine.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;

import java.util.List;

public interface SpringDataPageRepository extends JpaRepository<PageEntity, Integer> {

    @Query("""
             SELECT COUNT(p)
             FROM PageEntity p
             WHERE p IN (
               SELECT i.page
               FROM IndexEntity i
               JOIN i.lemma l
               WHERE l IN :lemmas
               GROUP BY i.page
               HAVING COUNT(DISTINCT l.id) = :lemmasCount
             )
           """)
    int countPagesByLemmasIn(@Param("lemmas") List<LemmaEntity> lemmas,
                             @Param("lemmasCount") int lemmasCount);

    int countBySite(SiteEntity siteEntity);

    void deleteAll();
}
