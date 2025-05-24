package searchengine.model;

import lombok.Getter;

@Getter
public enum SiteStatus
{
    INDEXING(0), INDEXED(1), FAILED(2);
    private final int statusIndex;

    SiteStatus(int statusIndex) {
        this.statusIndex = statusIndex;
    }
}
