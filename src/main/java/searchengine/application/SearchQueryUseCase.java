package searchengine.application;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import searchengine.web.dto.OffsetBasedPageRequest;
import searchengine.web.dto.SearchResponse;
import searchengine.web.dto.SearchResult;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class SearchQueryUseCase {
    public static final int LEMMA_EXCL_PAGE_COUNT = 300;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public SearchResponse execute(String query, String siteUrl, int offset, int limit) {
        long globalOffset = offset;
        int remaining = limit;
        long totalResultsCount = 0;
        List<SearchResult> resultList = new LinkedList<>();
        List<SiteEntity> sitesToSearches = new ArrayList<>();

        if (siteUrl == null || siteUrl.isBlank()) {
            sitesToSearches = siteRepository.findAll();
        } else {
            sitesToSearches.add(siteRepository.findByUrlMatching(siteUrl));
        }
        PageLanguage lang = PageLanguage.RUSSIAN;

        PageLemmaProcessor queryProcessor = new PageLemmaProcessor(query, lang,
            TextAnalyzeMode.LEMMA_AND_FREQUENCY);
        if (queryProcessor.process()) {
            for (SiteEntity site : sitesToSearches) {
                if (!site.getStatus().equals(SiteStatus.INDEXED)) {
                    continue;
                }
                List<String> searchLemmas = queryProcessor.getLemmas().keySet()
                    .stream().toList();
                if (searchLemmas.isEmpty()) {
                    continue;
                }
                List<LemmaEntity> searchLemmasEntities =
                    lemmaRepository.findByLemmaInAndSite(searchLemmas, site)
                        .stream()
                        .filter(l -> l.getFrequency() < LEMMA_EXCL_PAGE_COUNT)
                        .sorted(Comparator.comparingInt(LemmaEntity::getFrequency))
                        .toList();
                if (searchLemmasEntities.isEmpty()) {
                    continue;
                }

                long siteTotal = pageRepository.countPagesByLemmasIn(searchLemmasEntities);
                totalResultsCount += siteTotal;

                if (globalOffset >= siteTotal) {
                    globalOffset -= siteTotal;
                    continue;
                }
                long siteStart = globalOffset;
                int siteTake = (int) Math.min(remaining, siteTotal - siteStart);
                Pageable pa = new OffsetBasedPageRequest(siteStart, siteTake, null);

                Page<Tuple> p = indexRepository.findPagesWithAbsRelevance(
                    searchLemmasEntities,
                    searchLemmasEntities.size(), pa);

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
                    searchResult.setTitle(document.title());
                    document.getElementsByTag("head").remove();
                    searchResult.setSnippet(createSnippet(document.text(), searchLemmas));
                    searchResult.setRelevance(relRel);
                    resultList.add(searchResult);
                }
                remaining -= p.getNumberOfElements();
                globalOffset = 0; // после первого «захода» по offset дальше берём с начала
                if (remaining == 0) {
                    break;
                }
            }
        }

        SearchResponse resp = new SearchResponse();
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
            String snippet = SnippetBuilder.buildSnippetSimple(text,
                pageLemmaProcessor.getWordInfoList(),
                queryLemmasSet,
                35, 60, 3);
            return snippet;
        }
        return text.substring(50, Math.min(200, text.length()));
    }
}