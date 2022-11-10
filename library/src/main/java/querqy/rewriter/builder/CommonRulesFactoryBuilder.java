package querqy.rewriter.builder;

import lombok.RequiredArgsConstructor;
import querqy.rewrite.commonrules.SimpleCommonRulesRewriterFactory;
import querqy.rewrite.commonrules.select.ExpressionCriteriaSelectionStrategyFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

@RequiredArgsConstructor(staticName = "of")
public class CommonRulesFactoryBuilder {

    private final CommonRulesDefinition definition;

    public SimpleCommonRulesRewriterFactory build() {
        try (final StringReader rulesReader = new StringReader(definition.getRules())) {

            return new SimpleCommonRulesRewriterFactory(
                    definition.getRewriterId(),
                    rulesReader,
                    definition.isAllowBooleanInput(),
                    definition.getBoostMethod(),
                    definition.getQuerqyParserFactory(),
                    definition.isIgnoreCase(),
                    Collections.emptyMap(),
                    new ExpressionCriteriaSelectionStrategyFactory(),
                    false
            );

        } catch (IOException e) {
            throw new RewriterFactoryBuilderException(e);
        }
    }
}
