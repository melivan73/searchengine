package searchengine.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;

    @Getter
    @Setter
    public static class StatisticsData {
        private TotalStatistics total;
        private List<DetailedStatisticsItem> detailed;
    }

    @Getter
    @Setter
    public static class TotalStatistics {
        private int sites;
        private int pages;
        private int lemmas;
        private boolean indexing;
    }

    @Getter
    @Setter
    public static class DetailedStatisticsItem {
        private String url;
        private String name;
        private String status;
        private long statusTime;
        private String error;
        private int pages;
        private int lemmas;
    }
}
