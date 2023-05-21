package querqy;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BoostConfig {

    private static final BoostConfig DEFAULT = BoostConfig.builder().build();

    public enum BoostMode {
        PARAM_ONLY, ADDITIVE, MULTIPLICATIVE
    }

    @Builder.Default private final BoostMode boostMode = BoostMode.PARAM_ONLY;

    public static BoostConfig defaultConfig() {
        return DEFAULT;
    }

}
