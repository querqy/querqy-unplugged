package querqy;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;

@Builder(toBuilder = true)
@Getter
public class FieldConfig {

    @NonNull private final String fieldName;
    @Builder.Default private final float weight = 1.0f;

    private final QueryTypeConfig queryTypeConfig;

    public Optional<QueryTypeConfig> getQueryTypeConfig() {
        return Optional.ofNullable(queryTypeConfig);
    }

    public static FieldConfig fromFieldName(final String fieldName) {
        return FieldConfig.builder().fieldName(fieldName).build();
    }
}
