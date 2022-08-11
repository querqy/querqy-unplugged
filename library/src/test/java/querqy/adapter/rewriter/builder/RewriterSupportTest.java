package querqy.adapter.rewriter.builder;

import org.junit.Test;
import querqy.rewrite.RewriterFactory;
import querqy.rewrite.commonrules.SimpleCommonRulesRewriterFactory;
import querqy.rewrite.contrib.ReplaceRewriterFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RewriterSupportTest {

    @Test
    public void testThat_commonRulesRewriterIsCreatedProperly_forDefinitionFromArgs() {
        final RewriterFactory rewriterFactory = RewriterSupport.createRewriterFactory(
                "common",
                "id", "1",
                "rules", "apple =>\n  SYNONYM: iphone"
        );

        assertThat(rewriterFactory).isInstanceOf(SimpleCommonRulesRewriterFactory.class);
    }
    @Test
    public void testThat_commonRulesRewriterIsCreatedProperly_forDefinitionFromMapAndArgs() {
        final RewriterFactory rewriterFactory = RewriterSupport.createRewriterFactory(
                "common",
                Map.of("id", "1"),
                "rules", "apple =>\n  SYNONYM: iphone"
        );

        assertThat(rewriterFactory).isInstanceOf(SimpleCommonRulesRewriterFactory.class);
    }

    @Test
    public void testThat_commonRulesRewriterIsCreatedProperly_forDefinitionFromMapAndArgsWithOverlappingAttribute() {
        final RewriterFactory rewriterFactory = RewriterSupport.createRewriterFactory(
                "common",
                Map.of(
                        "id", "1",
                        "rules", "invalid"
                ),
                "rules", "apple =>\n  SYNONYM: iphone"
        );

        assertThat(rewriterFactory).isInstanceOf(SimpleCommonRulesRewriterFactory.class);
    }

    @Test
    public void testThat_commonRulesRewriterIsCreatedProperly_forDefinitionFromMap() {
        final RewriterFactory rewriterFactory = RewriterSupport.createRewriterFactory(
                "common",
                Map.of(
                        "id", "1",
                        "rules", "apple =>\n  SYNONYM: iphone"
                )
        );

        assertThat(rewriterFactory).isInstanceOf(SimpleCommonRulesRewriterFactory.class);
    }

    @Test
    public void testThat_commonRulesRewriterIsCreatedProperly_forDefinitionFromMapAndRulesFromString() {
        final RewriterFactory rewriterFactory = RewriterSupport.createRewriterFactory(
                "common",
                Map.of(
                        "id", "1",
                        "rules", "apple =>\n  SYNONYM: iphone"
                )
        );

        assertThat(rewriterFactory).isInstanceOf(SimpleCommonRulesRewriterFactory.class);
    }

    @Test
    public void testThat_replaceRulesRewriterIsCreatedProperly_forDefinitionFromMap() {
        final RewriterFactory rewriterFactory = RewriterSupport.createRewriterFactory(
                "replace",
                Map.of(
                        "id", "1",
                        "rules", "aple => apple"
                )
        );

        assertThat(rewriterFactory).isInstanceOf(ReplaceRewriterFactory.class);
    }
}
