package querqy.converter.solr.map;

import lombok.Builder;
import lombok.Getter;
import querqy.QueryTypeConfig;

import java.util.Map;

@Getter
@Builder
public class SolrQueryTypeConfig implements QueryTypeConfig {

    private static SolrQueryTypeConfig DEFAULT_CONFIG = SolrQueryTypeConfig.builder().build();

    @Builder.Default private final String typeName = "field";
    @Builder.Default private final String queryParamName = "query";
    @Builder.Default private final String fieldParamName = "f";
    @Builder.Default private final Map<String, Object> constantParams = Map.of();

    public static QueryTypeConfig defaultConfig() {
        return DEFAULT_CONFIG;
    }
}
