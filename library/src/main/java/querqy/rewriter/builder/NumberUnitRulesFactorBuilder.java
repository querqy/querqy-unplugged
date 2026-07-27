package querqy.rewriter.builder;

import lombok.RequiredArgsConstructor;
import querqy.rewriter.numberunit.NumberUnitRewriterFactory;

import java.io.IOException;

@RequiredArgsConstructor(staticName = "of")
public class NumberUnitRulesFactorBuilder {

    private final NumberUnitRulesDefinition definition;

    public NumberUnitRewriterFactory build() {
        try {
            return new NumberUnitRewriterFactory(
                    "id2", definition.getRules(), definition.getNumberUnitQueryCreator());
        } catch (IOException e) {
            throw new RewriterFactoryBuilderException(e);
        }
    }

}