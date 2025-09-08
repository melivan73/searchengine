package searchengine.domain.repository;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;

import java.util.List;
import java.util.Map;

public interface IndexRepository {
    List<LemmaEntity> getLemmasByPage(PageEntity page);

    void deleteByPage(PageEntity page);

    long countPagesByLemmaInAndSiteIn(List<String> lemmas, int lemmasCount,
                                      List<SiteEntity> sites);

    Page<Tuple> findPagesWithAbsRelevanceAndSiteIn(List<String> lemmas, int lemmasCount,
                                                   List<SiteEntity> sites, Pageable pageable);

    Page<Tuple> findPagesWithAbsRelevance(List<LemmaEntity> lemmas, int lemmasCount,
                                          Pageable pageable);

    void batchInsertIndexData(PageEntity pageEntity, Map<String, Integer> mapLemmaRank,
                              List<LemmaEntity> lemmas);

    void deleteAll();
}
