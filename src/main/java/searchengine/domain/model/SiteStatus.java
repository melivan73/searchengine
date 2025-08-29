package searchengine.domain.model;

import lombok.Getter;

@Getter
public enum SiteStatus {
    INDEXING, INDEXED, FAILED
}
