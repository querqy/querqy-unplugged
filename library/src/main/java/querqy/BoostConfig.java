package querqy;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BoostConfig {

    private static final BoostConfig DEFAULT = BoostConfig.builder().build();

    public enum QueryScoreConfig {
        IGNORE, ADD_TO_BOOST_PARAM, MULTIPLY_WITH_BOOST_PARAM
    }

    @Builder.Default private final QueryScoreConfig queryScoreConfig = QueryScoreConfig.IGNORE;

    public static BoostConfig defaultConfig() {
        return DEFAULT;
    }

}
