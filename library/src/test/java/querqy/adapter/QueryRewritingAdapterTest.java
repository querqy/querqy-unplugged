package querqy.adapter;

import org.junit.Test;
import querqy.QueryRewritingConfig;
import querqy.adapter.rewriter.builder.RewriterSupport;
import querqy.model.convert.builder.ExpandedQueryBuilder;

import java.util.AbstractMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.DisjunctionMaxQueryBuilder.dmq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;
import static querqy.model.convert.builder.TermBuilder.term;

public class QueryRewritingAdapterTest {

    @Test
    public void testThat_replacementsAreApplied_forGivenReplaceRulesRewriter() {
        final QueryRewritingConfig rewritingConfig = QueryRewritingConfig.builder()
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "replace",
                                "id", "1",
                                "rules", "aple; applee => apple",
                                "inputDelimiter", ";"
                        )
                )
                .build();

        final RewrittenQuery rewrittenQuery = QueryRewritingAdapter.builder()
                .queryInput("aple applee")
                .queryRewritingConfig(rewritingConfig)
                .build()
                .rewriteQuery();

        final ExpandedQueryBuilder expanded = expanded(rewrittenQuery.getQuery());

        assertThat(expanded.getUserQuery()).isEqualTo(
                bq("apple", "apple")
        );
    }


    @Test
    public void testThat_synonymsAreApplied_forGivenCommonRulesRewriter() {
        final QueryRewritingConfig rewritingConfig = QueryRewritingConfig.builder()
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "common",
                                "id", "1",
                                "rules", "apple smartphone =>\n  SYNONYM: iphone"
                        )
                )
                .build();

        final RewrittenQuery rewrittenQuery = QueryRewritingAdapter.builder()
                .queryInput("apple smartphone")
                .queryRewritingConfig(rewritingConfig)
                .build()
                .rewriteQuery();

        final ExpandedQueryBuilder expanded = expanded(rewrittenQuery.getQuery());

        assertThat(expanded.getUserQuery()).isEqualTo(
                bq(
                        dmq(
                                term("apple"),
                                term("iphone", true)
                        ),
                        dmq(
                                term("smartphone"),
                                term("iphone", true)
                        )
                )
        );
    }

    @Test
    public void testThat_synonymsAreApplied_forMultipleGivenCommonRulesRewriters() {
        final QueryRewritingConfig rewritingConfig = QueryRewritingConfig.builder()
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "common",
                                "id", "1",
                                "rules", "apple smartphone =>\n  SYNONYM: iphone"
                        )
                )
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "common",
                                "id", "2",
                                "rules", "smartphone =>\n  SYNONYM: mobile"
                        )
                )
                .build();

        final RewrittenQuery rewrittenQuery = QueryRewritingAdapter.builder()
                .queryInput("apple smartphone")
                .queryRewritingConfig(rewritingConfig)
                .build()
                .rewriteQuery();

        final ExpandedQueryBuilder expanded = expanded(rewrittenQuery.getQuery());

        assertThat(expanded.getUserQuery()).isEqualTo(
                bq(
                        dmq(
                                term("apple"),
                                term("iphone", true)
                        ),
                        dmq(
                                term("smartphone"),
                                term("iphone", true),
                                term("mobile", true)
                        )
                )
        );

    }

    @Test
    public void testThat_rewritingActionsAreLoggedInCorrectOrder_forMultipleRewriters() {
        final QueryRewritingConfig rewritingConfig = QueryRewritingConfig.builder()
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "common",
                                "id", "1",
                                "rules", "apple smartphone =>\n  SYNONYM: iphone\n" +
                                        "smartphone =>\n SYNONYM: handy"
                        )
                )
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "common",
                                "id", "2",
                                "rules", "smartphone =>\n  SYNONYM: mobile"
                        )
                )
                .build();

        final RewrittenQuery rewrittenQuery = QueryRewritingAdapter.builder()
                .queryInput("apple smartphone")
                .queryRewritingConfig(rewritingConfig)
                .build()
                .rewriteQuery();

        assertThat(rewrittenQuery.getRewritingActions()).containsExactly(
                new AbstractMap.SimpleEntry<>("1", List.of("apple smartphone#0", "smartphone#1")),
                new AbstractMap.SimpleEntry<>("2", List.of("smartphone#0"))
        );
    }
}
