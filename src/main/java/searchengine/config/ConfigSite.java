package searchengine.config;

import lombok.Getter;

@Getter
public final class ConfigSite {
    private final String url;
    private final String name;
    private final String scheme;

    public ConfigSite(String url, String name) {
        this.url = url;
        this.name = name;
        this.scheme = url.startsWith("https") ? "https" : "http";
    }
}
