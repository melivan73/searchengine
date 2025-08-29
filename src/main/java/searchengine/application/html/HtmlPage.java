package searchengine.application.html;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class HtmlPage {
    private final String url;
    private final String content;
    private final int statusCode;
}