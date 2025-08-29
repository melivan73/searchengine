package searchengine.config;

import lombok.Data;

@Data
public final class ConfigConnection {
    private final String agent;
    private final String referrer;
}
