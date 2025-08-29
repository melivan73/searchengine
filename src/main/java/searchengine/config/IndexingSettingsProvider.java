package searchengine.config;

import java.util.List;

public interface IndexingSettingsProvider {
    List<ConfigSite> getSites();

    ConfigConnection getConnectionData();
}
