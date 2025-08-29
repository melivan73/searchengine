package searchengine.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import searchengine.application.html.HtmlPage;
import searchengine.application.html.PageFetcher;
import searchengine.application.services.PageIndexService;
import searchengine.domain.model.SiteEntity;
import searchengine.domain.repository.PageRepository;
import searchengine.domain.repository.SiteRepository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class IndexPageUseCase {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final PageFetcher pageFetcher;
    private final PageIndexService pageIndexService;

    public boolean execute(String pageUrl) {
        SiteEntity siteEntity = siteRepository.findByUrlMatching(pageUrl);
        if (siteEntity == null) {
            return false;
        }
        Optional<HtmlPage> optPage = pageFetcher.fetch(pageUrl);
        if (optPage.isEmpty()) {
            return false;
        }
        HtmlPage htmlPage = optPage.get();
        if (htmlPage.getStatusCode() >= HttpStatus.BAD_REQUEST.value()) {
            return false;
        }

        try {
            pageIndexService.indexPage(htmlPage);
        } catch (Exception e) {
            log.error("Невозможно проиндексировать страницу {} : {}", pageUrl,
                e.getMessage());
            return false;
        }
        log.info("Страница {} проиндексирована", pageUrl);
        return true;
    }
}