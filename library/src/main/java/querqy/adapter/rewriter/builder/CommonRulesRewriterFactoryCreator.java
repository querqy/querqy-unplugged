package querqy.adapter.rewriter.builder;

import lombok.Builder;
import lombok.NonNull;
import querqy.rewrite.commonrules.QuerqyParserFactory;
import querqy.rewrite.commonrules.SimpleCommonRulesRewriterFactory;
import querqy.rewrite.commonrules.WhiteSpaceQuerqyParserFactory;
import querqy.rewrite.commonrules.model.BoostInstruction;
import querqy.rewrite.commonrules.select.ExpressionCriteriaSelectionStrategyFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

@Builder(buildMethodName = "_create", builderClassName = "Creator", builderMethodName = "creator")
public class CommonRulesRewriterFactoryCreator {

    @NonNull private String rewriterId;
    @NonNull private String rules;

    @Builder.Default private boolean ignoreCase = true;
    @Builder.Default private boolean allowBooleanInput = false;
    @Builder.Default private BoostInstruction.BoostMethod boostMethod = BoostInstruction.BoostMethod.ADDITIVE;
    @Builder.Default private QuerqyParserFactory querqyParserFactory = new WhiteSpaceQuerqyParserFactory();

    // TODO: termCache, selectionStrategyFactories

    public static class Creator {

        public SimpleCommonRulesRewriterFactory createFactory() throws IOException {
            final CommonRulesRewriterFactoryCreator creator = this._create();

            return new SimpleCommonRulesRewriterFactory(
                    creator.rewriterId,
                    new StringReader(creator.rules),
                    creator.allowBooleanInput,
                    creator.boostMethod,
                    creator.querqyParserFactory,
                    creator.ignoreCase,
                    Collections.emptyMap(),
                    new ExpressionCriteriaSelectionStrategyFactory(),
                    false
            );
        }
    }
}
