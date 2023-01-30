package querqy;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BoostConfig {

    private static final BoostConfig DEFAULT = BoostConfig.builder().build();

    public enum BoostMode {
        BOOST_SCORE_ONLY, ADDITIVE, MULTIPLICATIVE
    }

    @Builder.Default private final BoostMode boostMode = BoostMode.BOOST_SCORE_ONLY;

    public static BoostConfig defaultConfig() {
        return DEFAULT;
    }

}
