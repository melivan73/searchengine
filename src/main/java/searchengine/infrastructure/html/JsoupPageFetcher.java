package searchengine.infrastructure.html;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.springframework.stereotype.Component;
import searchengine.application.html.HtmlPage;
import searchengine.application.html.PageFetcher;
import searchengine.config.IndexingSettingsProvider;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class JsoupPageFetcher implements PageFetcher {
    private static final int TIMEOUT = 60_000;
    private static final int DELAY_MS = 120;
    private final IndexingSettingsProvider settingsProvider;

    public Optional<HtmlPage> fetch(String url) {
        try {
            Connection conn = Jsoup.connect(url)
                .timeout(TIMEOUT)
                .header("Accept", "text/html")
                .header("Referer", settingsProvider.getConnectionData().getReferrer())
                .userAgent(settingsProvider.getConnectionData().getAgent())
                .ignoreHttpErrors(true)
                .followRedirects(false);
            Thread.sleep(DELAY_MS);

            Connection.Response res = conn.execute();
            int status = res.statusCode();
            String contentType = res.contentType();

            if (contentType == null || !contentType.contains("text/html")) {
                log.warn("Неподдерживаемый формат страницы {}, ({})", url, contentType);
                return Optional.empty();
            }

            String content = "";
            if (status == 200 || status == 500) {
                content = res.parse().html();
            }
            log.info("Страница скачана! {}, http код: {}", url, res.statusCode());
            return Optional.of(new HtmlPage(url, content, res.statusCode()));
        } catch (UnsupportedMimeTypeException e) {
            log.warn("Недопустимый формат страницы {}, ({})", url, e.getMimeType());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Страница не может быть скачана {}, {}", url, e.getMessage());
            return Optional.empty();
        }
    }
}