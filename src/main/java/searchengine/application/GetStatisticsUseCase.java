package searchengine.application;

import lombok.RequiredArgsConstructor;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.model.SiteStatus;
import searchengine.domain.repository.LemmaRepository;
import searchengine.domain.repository.PageRepository;
import searchengine.domain.repository.SiteRepository;
import searchengine.web.dto.StatisticsResponse;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GetStatisticsUseCase {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    public StatisticsResponse execute() {
        List<SiteEntity> allSites = siteRepository.findAll();
        int totalSites = allSites.size();

        int totalPages = 0;
        int totalLemmas = 0;
        boolean indexing = false;

        List<StatisticsResponse.DetailedStatisticsItem> detailedList = new ArrayList<>();

        for (SiteEntity site : allSites) {
            int pageCount = pageRepository.countBySite(site);
            int lemmaCount = lemmaRepository.countBySite(site);
            totalPages += pageCount;
            totalLemmas += lemmaCount;

            if (site.getStatus() == SiteStatus.INDEXING) {
                indexing = true;
            }

            StatisticsResponse.DetailedStatisticsItem item =
                new StatisticsResponse.DetailedStatisticsItem();
            item.setUrl(site.getUrl());
            item.setName(site.getName());
            item.setStatus(site.getStatus().name());
            item.setStatusTime(
                site.getStatusTime().toEpochSecond(java.time.ZoneOffset.UTC));
            item.setError(site.getLastError());
            item.setPages(pageCount);
            item.setLemmas(lemmaCount);

            detailedList.add(item);
        }

        StatisticsResponse.TotalStatistics total = new StatisticsResponse.TotalStatistics();
        total.setSites(totalSites);
        total.setPages(totalPages);
        total.setLemmas(totalLemmas);
        total.setIndexing(indexing);

        StatisticsResponse.StatisticsData data = new StatisticsResponse.StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailedList);

        StatisticsResponse response = new StatisticsResponse();
        response.setResult(true);
        response.setStatistics(data);
        return response;
    }
}