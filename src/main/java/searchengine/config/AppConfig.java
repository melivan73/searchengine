package searchengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import searchengine.application.*;
import searchengine.application.crawler.ForkJoinSiteCrawler;
import searchengine.application.crawler.SiteCrawler;
import searchengine.application.html.PageFetcher;
import searchengine.application.services.PageIndexService;
import searchengine.domain.repository.IndexRepository;
import searchengine.domain.repository.LemmaRepository;
import searchengine.domain.repository.PageRepository;
import searchengine.domain.repository.SiteRepository;

@Configuration
@EnableRetry
public class AppConfig {

    @Bean
    public int forkJoinParallelism(IndexingSettingsProvider indexingSettingsProvider) {
        return Math.max(1, Runtime.getRuntime().availableProcessors() /
                           indexingSettingsProvider.getSites().size());
    }

    @Bean
    public SiteCrawler siteCrawler(SiteRepository siteRepository,
        IndexingManager indexingManager, int forkJoinParallelism) {
        return new ForkJoinSiteCrawler(siteRepository, indexingManager, forkJoinParallelism);
    }

    @Bean
    public StartIndexingUseCase startIndexingUseCase(SiteRepository siteRepository,
        PageRepository pageRepository, LemmaRepository lemmaRepository,
        IndexRepository indexRepository, IndexingSettingsProvider indexingSettingsProvider,
        IndexingManager indexingManager, SiteCrawler siteCrawler) {
        return new StartIndexingUseCase(siteRepository, pageRepository, lemmaRepository,
            indexRepository, indexingSettingsProvider, indexingManager, siteCrawler);
    }

    @Bean
    public StopIndexingUseCase stopIndexingUseCase(SiteRepository siteRepository,
        IndexingManager indexingManager) {
        return new StopIndexingUseCase(siteRepository, indexingManager);
    }

    @Bean
    public GetStatisticsUseCase getStatisticsUseCase(SiteRepository siteRepository,
        PageRepository pageRepository, LemmaRepository lemmaRepository) {
        return new GetStatisticsUseCase(siteRepository, pageRepository, lemmaRepository);
    }

    @Bean
    public IndexPageUseCase indexPageUseCase(SiteRepository siteRepository,
        PageRepository pageRepository, PageFetcher pageFetcher,
        PageIndexService pageIndexService) {
        return new IndexPageUseCase(siteRepository, pageRepository, pageFetcher,
            pageIndexService);
    }

    @Bean
    public SearchQueryUseCase searchQueryUseCase(SiteRepository siteRepository,
        PageRepository pageRepository, LemmaRepository lemmaRepository,
        IndexRepository indexRepository) {
        return new SearchQueryUseCase(siteRepository, pageRepository, lemmaRepository,
            indexRepository);
    }
}