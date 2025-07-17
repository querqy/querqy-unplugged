package querqy.rewriter.builder;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import querqy.rewrite.contrib.numberunit.NumberUnitQueryCreator;

@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"numberUnitQueryCreator"})
@ToString
public class NumberUnitRulesDefinition {
    @JsonAlias("id") @NonNull
    private String rewriterId;

    @NonNull private final String rules;
    @NonNull private final NumberUnitQueryCreator numberUnitQueryCreator;

    @Builder.Default private int defaultUnitMultiplier = 1;
    @Builder.Default private int defaultFieldScale = 0;
    @Builder.Default private float defaultBoostMaxScoreForExactMatch = 200;
    @Builder.Default private float defaultBoostMinScoreAtUpperBoundary = 100;
    @Builder.Default private float defaultBoostMinScoreAtLowerBoundary = 100;
    @Builder.Default private float defaultBoostAdditionalScoreForExactMatch = 100;
    @Builder.Default private float defaultBoostPercentageUpperBoundary = 20;
    @Builder.Default private float defaultBoostPercentageLowerBoundary = 20;
    @Builder.Default private float defaultBoostPercentageUpperBoundaryExactMatch = 0;
    @Builder.Default private float defaultBoostPercentageLowerBoundaryExactMatch = 0;
    @Builder.Default private float defaultFilterPercentageLowerBoundary = 20;
    @Builder.Default private float defaultFilterPercentageUpperBoundary = 20;


}
