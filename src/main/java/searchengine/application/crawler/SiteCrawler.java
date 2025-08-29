package searchengine.application.crawler;

import searchengine.config.ConfigSite;

public interface SiteCrawler {
    CrawlerSiteTask crawl(ConfigSite config);
}