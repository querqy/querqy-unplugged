package querqy.rewriter.builder;

import org.junit.Test;
import querqy.rewriter.numberunit.NumberUnitQueryCreator;
import querqy.rewriter.numberunit.NumberUnitRewriterFactory;
import querqy.rewriter.numberunit.model.PerUnitNumberUnitDefinition;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NumberUnitRulesFactorBuilderTest {

    private static final String NUMBER_UNIT_CONFIG = "{\n" +
            "   \"numberUnitDefinitions\": [\n" +
            "       {\n" +
            "          \"units\": [ { \"term\": \"inch\" } ],\n" +
            "          \"fields\": [ { \"fieldName\": \"screen_size\" } ]\n" +
            "      }\n" +
            "   ]\n" +
            "}";

    @Test
    public void testThat_builtFactoryUsesTheConfiguredRewriterId() {
        final NumberUnitRulesDefinition definition = NumberUnitRulesDefinition.builder()
                .rewriterId("my-number-unit-rewriter")
                .rules(NUMBER_UNIT_CONFIG)
                .numberUnitQueryCreator(NoopNumberUnitQueryCreator::new)
                .build();

        final NumberUnitRewriterFactory factory = NumberUnitRulesFactorBuilder.of(definition).build();

        assertThat(factory.getRewriterId()).isEqualTo("my-number-unit-rewriter");
    }

    private static final class NoopNumberUnitQueryCreator extends NumberUnitQueryCreator {
        private NoopNumberUnitQueryCreator(final int scale) {
            super(scale);
        }

        @Override
        public querqy.model.QuerqyQuery<querqy.model.BooleanParent> createFilterQuery(
                final BigDecimal value, final List<PerUnitNumberUnitDefinition> perUnitNumberUnitDefinitions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public querqy.model.BoostQuery createBoostQuery(
                final BigDecimal value, final List<PerUnitNumberUnitDefinition> perUnitNumberUnitDefinitions) {
            throw new UnsupportedOperationException();
        }
    }
}