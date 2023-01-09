package querqy;

import lombok.Builder;

import java.util.Optional;

@Builder
public class QueryNodesConfig {

    private static final QueryNodesConfig EMPTY_CONFIG = QueryNodesConfig.builder().build();

    private final QueryTypeConfig boolNodeConfig;
    private final QueryTypeConfig dismaxNodeConfig;
    private final QueryTypeConfig constantScoreNodeConfig;

    public Optional<QueryTypeConfig> getBoolNodeConfig() {
        return Optional.ofNullable(boolNodeConfig);
    }

    public Optional<QueryTypeConfig> getDismaxNodeConfig() {
        return Optional.ofNullable(dismaxNodeConfig);
    }

    public Optional<QueryTypeConfig> getConstantScoreNodeConfig() {
        return Optional.ofNullable(constantScoreNodeConfig);
    }

    public static QueryNodesConfig empty() {
        return EMPTY_CONFIG;
    }

}
