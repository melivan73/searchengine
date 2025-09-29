package searchengine.application.services;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.application.html.HtmlPage;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.model.SiteStatus;
import searchengine.domain.repository.IndexRepository;
import searchengine.domain.repository.LemmaRepository;
import searchengine.domain.repository.PageRepository;
import searchengine.domain.repository.SiteRepository;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageIndexService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final EntityManager entityManager;

    @Transactional
    public Document indexPage(HtmlPage htmlPage) {
        URI uri = URI.create(URLDecoder.decode(htmlPage.getUrl(), StandardCharsets.UTF_8));
        SiteEntity site = siteRepository.findByUrlMatching(uri.getHost());
        if (site == null) {
            throw new IllegalArgumentException(
                "SiteEntity not found for URL: " + htmlPage.getUrl());
        }

        if (htmlPage.getUrl().equals(site.getUrl()) &&
            htmlPage.getStatusCode() != HttpStatus.OK.value()) {
            site.setStatus(SiteStatus.FAILED);
            site.setStatusTime(LocalDateTime.now());
            site.setLastError(ErrorMessage.SITE_UNAVAILABLE.getErrorMessage());
            return null;
        }

        PageEntity page = pageRepository.exists(uri.getPath(), site.getId());
        if (page != null) {
            List<LemmaEntity> lemmas = indexRepository.getLemmasByPage(page);
            indexRepository.deleteByPage(page);
            lemmaRepository.deleteByLemmasIn(lemmas);
            pageRepository.delete(page);
            log.info("Страница id = {}, path = {} удалена!", page.getId(), page.getPath());
        }

        PageEntity pageEntity = new PageEntity();
        pageEntity.setSite(site);
        pageEntity.setCode(htmlPage.getStatusCode());
        pageEntity.setPath(uri.getPath());
        pageEntity.setContent(htmlPage.getContent());
        pageEntity = pageRepository.save(pageEntity);

        Document document = Jsoup.parse(pageEntity.getContent(), htmlPage.getUrl());

        PageLemmaProcessor lemmaProcessor = new PageLemmaProcessor(pageEntity.getContent(),
            TextAnalyzeMode.LEMMA_AND_FREQUENCY);

        if (lemmaProcessor.process()) {
            Map<String, Integer> lemmas = lemmaProcessor.getLemmas();

            List<String> lemmaValues = new ArrayList<>(lemmas.keySet());
            lemmaRepository.batchInsertLemmas(lemmaValues, site);
            List<LemmaEntity> lemmaEntities = lemmaRepository.findByLemmaInAndSite(
                lemmaValues, site);
            if (!lemmaEntities.isEmpty()) {
                indexRepository.batchInsertIndexData(pageEntity, lemmas, lemmaEntities);
            }
        }
        return document;
    }
}
