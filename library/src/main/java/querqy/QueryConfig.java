package querqy;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.Map;

@Builder(toBuilder = true)
@Getter
public class QueryConfig {

    // TODO: must not be empty
    @Singular private final Map<String, Float> fields;

    private final Float tie;
    private final String minimumShouldMatch;

    // TODO: @Builder.Default private final boolean qboostFieldBoost = false;

    public boolean hasMinimumShouldMatch() {
        return minimumShouldMatch != null;
    }

    public boolean hasTie() {
        return tie != null;
    }

    public static QueryConfig empty() {
        return QueryConfig.builder().build();
    }

}
