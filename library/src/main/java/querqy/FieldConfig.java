package querqy;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Builder
public class FieldConfig {

    @Getter private final String fieldName;
    @Getter private final float weight;

    private final QueryTypeConfig queryTypeConfig;

    public Optional<QueryTypeConfig> getQueryTypeConfig() {
        return Optional.of(queryTypeConfig);
    }
}
