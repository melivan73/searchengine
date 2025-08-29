package searchengine.application.html;

import java.util.Optional;

public interface PageFetcher {
    Optional<HtmlPage> fetch(String url);
}
