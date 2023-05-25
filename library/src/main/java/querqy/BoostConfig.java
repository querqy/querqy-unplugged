package querqy;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BoostConfig {

    private static final BoostConfig DEFAULT = BoostConfig.builder().build();

    public enum QueryScoreConfig {
        IGNORE_QUERY_SCORE, ADD_TO_BOOST_PARAM, MULTIPLY_WITH_BOOST_PARAM, CLASSIC
    }

    @Builder.Default private final QueryScoreConfig queryScoreConfig = QueryScoreConfig.IGNORE_QUERY_SCORE;

    public static BoostConfig defaultConfig() {
        return DEFAULT;
    }

}
