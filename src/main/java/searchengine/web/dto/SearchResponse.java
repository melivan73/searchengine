package searchengine.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public final class SearchResponse {
    List<SearchResult> items;
    long total;

    static public SearchResponse empty() {
        SearchResponse result = new SearchResponse();
        result.setTotal(0);
        result.setItems(Collections.emptyList());
        return result;
    }
}
