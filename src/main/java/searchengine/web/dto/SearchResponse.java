package searchengine.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class SearchResponse {
    List<SearchResult> items;
    long total;
}
