package searchengine.application.crawler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.application.IndexingManager;
import searchengine.application.html.HtmlPage;
import searchengine.application.html.PageFetcher;
import searchengine.application.services.PageIndexService;
import searchengine.config.ConfigSite;
import searchengine.config.IndexingSettingsProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

@Slf4j
@RequiredArgsConstructor
@Component
@Scope("prototype")
@Getter
@Setter
public class CrawlerSiteTask extends RecursiveTask<Void> {
    private static final int TASK_LIMIT = 10000;
    private static final String DEF_SCHEME = "https";
    private final PageFetcher pageFetcher;
    private final IndexingManager indexingManager;
    private final PageIndexService pageIndexService;
    private final IndexingSettingsProvider settingsProvider;
    private String url;
    private String domain;
    private Set<String> visitedUrls;
    private Set<String> invalidUrls;

    public void init(String url, Set<String> visitedUrls, Set<String> invalidUrls) {
        URI uri = URI.create(
            URLDecoder.decode(url.strip().toLowerCase(), StandardCharsets.UTF_8));
        this.domain = uri.getHost();
        this.url = url;
        this.visitedUrls = visitedUrls;
        this.invalidUrls = invalidUrls;
    }

    @Override
    protected Void compute() {
        if (indexingManager.isStopped() || this.isCancelled() ||
            indexingManager.getIndexedCount().get() > TASK_LIMIT) {
            return null;
        }
        if (invalidUrls.contains(url) || !visitedUrls.add(url)) {
            return null;
        }
        runIndexing();
        return null;
    }

    private void runIndexing() {
        Optional<HtmlPage> optPage = pageFetcher.fetch(url);
        if (optPage.isEmpty()) {
            invalidUrls.add(url);
            return;
        }

        HtmlPage htmlPage = optPage.get();
        Document document;
        try {
            document = pageIndexService.indexPage(htmlPage);
            if (document != null) {
                int indexedCount = indexingManager.getIndexedCount().incrementAndGet();
                log.info("Страниц проиндексировано {}", indexedCount);
            }
        } catch (Exception e) {
            log.warn("Невозможно проиндексировать страницу {} : {}", url, e.getMessage());
            invalidUrls.add(url);
            return;
        }

        List<CrawlerSiteTask> tasks = new ArrayList<>();
        for (Element link : document.select("a[href]")) {
            String next = link.absUrl("href");
            if (next.equals(url)) {
                continue;
            }
            next = normalizeUrl(next);
            if (isValidUrl(next)) {
                CrawlerSiteTask task = indexingManager.createTask(next, visitedUrls,
                    invalidUrls);
                task.fork();
                tasks.add(task);
            } else {
                invalidUrls.add(next);
            }
        }
        tasks.forEach(CrawlerSiteTask::join);
    }

    private boolean isValidUrl(String url) {
        if (url.matches(
            ".*[?=#%а-яА-ЯёЁ].*|.*\\.(php|pdf|xml|jp?g|png|tiff|doc?|rtf|xlx?|xls?)$")) {
            return false;
        }

        url = normalizeUrl(url);
        if (invalidUrls.contains(url) || visitedUrls.contains(url)) {
            return false;
        }
        if ((url.endsWith("/") && visitedUrls.contains(url.substring(0, url.length() - 1))) ||
            (!url.endsWith("/") && visitedUrls.contains(url + "/"))) {
            return false;
        }
        try {
            URI uri = new URI(URLDecoder.decode(url.toLowerCase(), StandardCharsets.UTF_8));
            return uri.getHost() != null && uri.getHost().equals(domain);
        } catch (URISyntaxException e) {
            log.warn("Invalid URL format: {}", url);
            return false;
        }
    }

    private String normalizeUrl(String url) {
        try {
            URI uri = new URI(
                URLDecoder.decode(url.strip().toLowerCase(), StandardCharsets.UTF_8));
            String host = uri.getHost();
            if (host == null) {
                return url;
            }

            String scheme = DEF_SCHEME;
            for (ConfigSite site : settingsProvider.getSites()) {
                if (site.getUrl().contains(host)) {
                    scheme = site.getScheme();
                    break;
                }
            }

            String path = uri.getPath() != null ? uri.getPath() : "";
            return scheme + "://" + host + path;
        } catch (URISyntaxException e) {
            return url;
        }
    }
}