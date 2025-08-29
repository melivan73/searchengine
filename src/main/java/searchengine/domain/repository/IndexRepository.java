package searchengine.domain.repository;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.PageEntity;

import java.util.List;
import java.util.Map;

@Repository
public interface IndexRepository {

    Page<Tuple> findPagesWithAbsRelevance(List<LemmaEntity> lemmas, int lemmasCount,
                                          Pageable pageable);

    void batchInsertIndexData(PageEntity pageEntity, Map<String, Integer> mapLemmaRank,
                              List<LemmaEntity> lemmas);

    void deleteAll();
}
