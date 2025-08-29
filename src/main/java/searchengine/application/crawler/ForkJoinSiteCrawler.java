package searchengine.application.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.application.IndexingManager;
import searchengine.config.ConfigSite;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.model.SiteStatus;
import searchengine.domain.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@RequiredArgsConstructor
@Component
public class ForkJoinSiteCrawler implements SiteCrawler {
    private final SiteRepository siteRepository;
    private final IndexingManager indexingManager;
    private final int parallelism;

    @Override
    public CrawlerSiteTask crawl(ConfigSite config) {
        log.info("Начало обработки сайта: {}", config.getUrl());
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setUrl(config.getUrl());
        siteEntity.setName(config.getName());
        siteEntity.setStatus(SiteStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setLastError("");
        siteRepository.save(siteEntity);

        // Общие коллекции для всего дерева задач каждого сайта
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Set<String> invalid = ConcurrentHashMap.newKeySet();

        ForkJoinPool pool = new ForkJoinPool(parallelism);
        CrawlerSiteTask rootTask =
            indexingManager.createTask(siteEntity.getUrl(), visited, invalid);
        indexingManager.register(pool);
        return (CrawlerSiteTask) pool.submit(rootTask);
    }
}