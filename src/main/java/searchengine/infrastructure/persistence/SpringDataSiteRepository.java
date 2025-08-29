package searchengine.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.model.SiteStatus;

import java.time.LocalDateTime;
import java.util.Set;

public interface SpringDataSiteRepository extends JpaRepository<SiteEntity, Integer> {
    void deleteAll();

    boolean existsByStatus(SiteStatus status);

    @Modifying
    @Transactional
    @Query(
        "UPDATE SiteEntity s SET s.status = :toStatus, s.statusTime = :time " +
        "WHERE s.status IN :from")
    void updateStatuses(@Param("from") Set<SiteStatus> from,
        @Param("toStatus") SiteStatus to,
        @Param("time") LocalDateTime time);

    @Transactional(readOnly = true)
    @Query("SELECT s FROM SiteEntity s WHERE s.url LIKE CONCAT('%', :pageUrl, '%')")
    SiteEntity findByUrlMatching(@Param("pageUrl") String pageUrl);
}