package querqy.converter.generic.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import querqy.FieldConfig;
import querqy.QueryTypeConfig;

@Builder
@Getter
public class TermQueryDefinition {

    @NonNull private final String term;
    @NonNull private final String fieldName;

    private final float termBoost;

    private final FieldConfig fieldConfig;

}
