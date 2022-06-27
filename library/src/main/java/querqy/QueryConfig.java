package querqy;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Builder(toBuilder = true)
@Getter
public class QueryConfig {

    @Builder.Default private final String boolNodeName = "bool";
    @Builder.Default private final String disMaxNodeName = "dis_max";
    @Builder.Default private final String scoringNodeName = "constant_score";
    @Builder.Default private final String matchingNodeName = "field";

    // TODO: must not be empty
    @Singular private final Map<String, Float> fields;

    private final Float tie;
    private final String minimumShouldMatch;

    public boolean hasMinimumShouldMatch() {
        return minimumShouldMatch != null;
    }

    public boolean hasTie() {
        return tie != null;
    }

}
