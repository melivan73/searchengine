package searchengine.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

@Getter
@Setter
public final class SearchResult {
    private String site;
    private String uri;
    private String title;
    private String snippet;
    private String siteName;
    private double relevance;

    public static class ResultComparator implements Comparator<SearchResult> {
        @Override
        public int compare(SearchResult o1, SearchResult o2) {
            return (int) Math.signum(o1.relevance - o2.relevance);
        }
    }
}
