package searchengine.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import searchengine.application.crawler.CrawlerSiteTask;
import searchengine.application.crawler.SiteCrawler;
import searchengine.application.services.IndexingManager;
import searchengine.config.ConfigSite;
import searchengine.config.IndexingSettingsProvider;
import searchengine.domain.model.SiteStatus;
import searchengine.domain.repository.IndexRepository;
import searchengine.domain.repository.LemmaRepository;
import searchengine.domain.repository.PageRepository;
import searchengine.domain.repository.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Slf4j
@RequiredArgsConstructor
public class StartIndexingUseCase {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    private final IndexingSettingsProvider indexingSettingsProvider;
    private final IndexingManager indexingManager;
    private final SiteCrawler crawler;

    @Async
    public Future<Boolean> executeAsync() {
        execute();
        return CompletableFuture.completedFuture(
            siteRepository.existsByStatus(SiteStatus.INDEXING));
    }

    public void execute() {
        clearDatabase();
        indexingManager.reset();
        List<CrawlerSiteTask> siteTasks = new ArrayList<>();

        for (ConfigSite config : indexingSettingsProvider.getSites()) {
            CrawlerSiteTask siteTask = crawler.crawl(config);
            siteTasks.add(siteTask);
        }

        // ожидание завершения задач, очистка коллекций
        for (CrawlerSiteTask siteTask : siteTasks) {
            siteTask.join();
            siteTask.getVisitedUrls().clear();
            siteTask.getInvalidUrls().clear();
        }
        // ожидание остановки всех ForkJoinPool
        indexingManager.awaitTermination();
        siteRepository.updateStatusWhereIn(Set.of(SiteStatus.INDEXING), SiteStatus.INDEXED);
        log.info("Обход всех сайтов завершен!");
    }

    @Transactional
    private void clearDatabase() {
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();
    }
}