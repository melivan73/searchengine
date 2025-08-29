package searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "indexing-settings")
@Data
public class YamlIndexingSettingsProvider implements IndexingSettingsProvider {
    private List<ConfigSite> sites = new ArrayList<>();
    private ConfigConnection connection;

    @Override
    public List<ConfigSite> getSites() {
        return sites;
    }

    @Override
    public ConfigConnection getConnectionData() {
        return connection;
    }
}