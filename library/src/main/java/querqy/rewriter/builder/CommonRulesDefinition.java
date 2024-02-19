package querqy.rewriter.builder;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import querqy.rewrite.commonrules.QuerqyParserFactory;
import querqy.rewrite.commonrules.WhiteSpaceQuerqyParserFactory;
import querqy.rewrite.commonrules.model.BoostInstruction;

// TODO: termCache, selectionStrategyFactories

@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"boostMethod", "querqyParserFactory"})
@ToString
public class CommonRulesDefinition {

    @JsonAlias("id") @NonNull private String rewriterId;
    @NonNull private String rules;

    @Builder.Default private boolean ignoreCase = true;
    @Builder.Default private boolean allowBooleanInput = false;
    @Builder.Default private boolean buildTermCache = false;
    @Builder.Default private BoostInstruction.BoostMethod boostMethod = BoostInstruction.BoostMethod.ADDITIVE;
    @Builder.Default private QuerqyParserFactory querqyParserFactory = new WhiteSpaceQuerqyParserFactory();

}
