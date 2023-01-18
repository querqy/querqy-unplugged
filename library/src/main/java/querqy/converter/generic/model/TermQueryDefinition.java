package querqy.converter.generic.model;

import lombok.Builder;
import lombok.Getter;
import querqy.FieldConfig;

@Builder
@Getter
public class TermQueryDefinition {

    private boolean isConstantScoreQuery;

    private final String term;
    private final float termBoost;
    private final FieldConfig fieldConfig;

}
