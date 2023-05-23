package querqy;

import lombok.Builder;

import java.util.Optional;

@Builder
@Deprecated
// TODO: purely Solr-related
public class QueryNodesConfig {

    // TODO: QueryTypeConfig is overkill; only typeName is actually used

    private static final QueryNodesConfig EMPTY_CONFIG = QueryNodesConfig.builder().build();

    private final QueryTypeConfig boolNodeConfig;
    private final QueryTypeConfig dismaxNodeConfig;
    private final QueryTypeConfig constantScoreNodeConfig;

    @Deprecated
    public Optional<QueryTypeConfig> getBoolNodeConfig() {
        return Optional.ofNullable(boolNodeConfig);
    }

    @Deprecated
    public Optional<QueryTypeConfig> getDismaxNodeConfig() {
        return Optional.ofNullable(dismaxNodeConfig);
    }

    @Deprecated
    public Optional<QueryTypeConfig> getConstantScoreNodeConfig() {
        return Optional.ofNullable(constantScoreNodeConfig);
    }

    public static QueryNodesConfig empty() {
        return EMPTY_CONFIG;
    }

}
