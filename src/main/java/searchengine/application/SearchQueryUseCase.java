package searchengine.application;

import jakarta.persistence.Tuple;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import searchengine.application.services.PageLanguage;
import searchengine.application.services.PageLemmaProcessor;
import searchengine.application.services.SnippetBuilder;
import searchengine.application.services.TextAnalyzeMode;
import searchengine.domain.model.LemmaEntity;
import searchengine.domain.model.PageEntity;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.model.SiteStatus;
import searchengine.domain.repository.IndexRepository;
import searchengine.domain.repository.LemmaRepository;
import searchengine.domain.repository.PageRepository;
import searchengine.domain.repository.SiteRepository;
import searchengine.web.dto.SearchResponse;
import searchengine.web.dto.SearchResult;

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class SearchQueryUseCase {
    public static final int LEMMA_EXCL_PAGE_COUNT = 200;
    public static final int SNIPPET_CONTEXT_CHARS_COUNT = 50;
    public static final int SNIPPET_LEMMAS_DISTANCE_THRESHOLD = 100;
    public static final int SNIPPET_MAX_FRAGMENTS = 2;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    SearchResponse resp = new SearchResponse();
    
    public SearchResponse execute(String query, String siteUrl, int offset, int limit) {
        List<SearchResult> resultList = new LinkedList<>();
        List<SiteEntity> sitesToSearch = new ArrayList<>();
        long totalResultsCount = 0;

        if (siteUrl == null || siteUrl.isBlank()) {
            sitesToSearch = siteRepository.findAll();
        } else {
            sitesToSearch.add(siteRepository.findByUrlMatching(siteUrl));
        }
        sitesToSearch = sitesToSearch.stream().filter(
            siteEntity -> siteEntity.getStatus().equals(SiteStatus.INDEXED)).toList();
        if (sitesToSearch.isEmpty()) {
            return SearchResponse.empty();
        }
        PageLanguage lang = PageLanguage.RUSSIAN;

        PageLemmaProcessor queryProcessor = new PageLemmaProcessor(query, lang,
            TextAnalyzeMode.LEMMA_AND_FREQUENCY);
        if (queryProcessor.process()) {
            List<String> searchLemmas =
                queryProcessor.getLemmas().keySet().stream().distinct().toList();
            if (searchLemmas.isEmpty()) {
                return SearchResponse.empty();
            }
            // проверка на условие в ТЗ, исключение слишком частых лемма (объясните зачем)
            List<LemmaEntity> lemmaEntities = lemmaRepository.findByLemmaInAndSiteIn(
                searchLemmas, sitesToSearch);
            searchLemmas = lemmaEntities
                .stream()
                .filter(le -> le.getFrequency() < LEMMA_EXCL_PAGE_COUNT)
                .map(LemmaEntity::getLemma)
                .distinct()
                .toList();

            totalResultsCount = indexRepository.countPagesByLemmaInAndSiteIn(searchLemmas,
                searchLemmas.size(), sitesToSearch);

            Pageable pr = PageRequest.of(Math.max(0, offset / limit), limit, Sort.unsorted());
            Page<Tuple> p = indexRepository.findPagesWithAbsRelevanceAndSiteIn(searchLemmas,
                searchLemmas.size(), sitesToSearch, pr);

            long maxAbs = 0;
            for (Tuple t : p.getContent()) {
                PageEntity pageEntity = t.get("page", PageEntity.class);
                long absRel = t.get("absRel", Long.class);
                maxAbs = Math.max(maxAbs, absRel);
                double relRel = (double) Math.round(100 * absRel / (double) maxAbs) / 100;

                Document document = Jsoup.parse(pageEntity.getContent());
                SearchResult searchResult = new SearchResult();
                searchResult.setSite(pageEntity.getSite().getUrl());
                searchResult.setSiteName(pageEntity.getSite().getName());
                searchResult.setUri(pageEntity.getPath());
                if (searchResult.getSite().endsWith("/") && searchResult.getUri().startsWith(
                    "/")) {
                    searchResult.setUri(searchResult.getUri().substring(1));
                }
                searchResult.setTitle(document.title());
                document.getElementsByTag("head").remove();
                searchResult.setSnippet(createSnippet(document.text(), searchLemmas));
                searchResult.setRelevance(relRel);
                resultList.add(searchResult);
            }
        }
        resp.setItems(resultList);
        resp.setTotal(totalResultsCount);
        return resp;
    }

    private String createSnippet(String text, List<String> queryLemmas) {
        PageLemmaProcessor pageLemmaProcessor = new PageLemmaProcessor(text,
            PageLanguage.RUSSIAN, TextAnalyzeMode.LEMMA_WORD_AND_WORDPOS);
        if (pageLemmaProcessor.process()) {
            text = pageLemmaProcessor.getProcessedText();

            Set<String> queryLemmasSet = new HashSet<String>(queryLemmas);
            return SnippetBuilder.buildSnippet(text, pageLemmaProcessor.getWordInfoList(),
                queryLemmasSet, SNIPPET_CONTEXT_CHARS_COUNT, SNIPPET_LEMMAS_DISTANCE_THRESHOLD,
                SNIPPET_MAX_FRAGMENTS);
        }
        return text.substring(Math.min(350, text.length()));
    }
}