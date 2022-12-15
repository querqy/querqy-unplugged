package querqy.rewriter;

import org.junit.Test;
import querqy.QuerqyConfig;
import querqy.model.MatchAllQuery;
import querqy.rewrite.RewriteLoggingConfig;
import querqy.rewriter.builder.RewriterSupport;
import querqy.domain.RewrittenQuerqyQuery;
import querqy.model.convert.builder.ExpandedQueryBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.DisjunctionMaxQueryBuilder.dmq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;
import static querqy.model.convert.builder.TermBuilder.term;

public class QueryRewritingExecutorTest {

    @Test
    public void testThat_matchAllQueryIsReturned_forFieldValueMatchAllInputString() {
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder().build();

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .queryInput("*:*")
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery();

        assertThat(rewrittenQuery.getQuery().getUserQuery()).isInstanceOf(MatchAllQuery.class);
    }

    @Test
    public void testThat_matchAllQueryIsReturned_forWildcardInputString() {
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder().build();

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .queryInput("*")
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery();

        assertThat(rewrittenQuery.getQuery().getUserQuery()).isInstanceOf(MatchAllQuery.class);
    }

    @Test
    public void testThat_replacementsAreApplied_forGivenReplaceRulesRewriter() {
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder()
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "replace",
                                "id", "1",
                                "rules", "aple; applee => apple",
                                "inputDelimiter", ";"
                        )
                )
                .build();

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .queryInput("aple applee")
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery();

        final ExpandedQueryBuilder expanded = expanded(rewrittenQuery.getQuery());

        assertThat(expanded.getUserQuery()).isEqualTo(
                bq("apple", "apple")
        );
    }


    @Test
    public void testThat_synonymsAreApplied_forGivenCommonRulesRewriter() {
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder()
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "common",
                                "id", "1",
                                "rules", "apple smartphone =>\n  SYNONYM: iphone"
                        )
                )
                .build();

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .queryInput("apple smartphone")
                .querqyConfig(rewritingConfig)
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
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder()
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

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .queryInput("apple smartphone")
                .querqyConfig(rewritingConfig)
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
    public void testThat_rewriteLoggingIsNotEmpty_forActiveRewriteLoggingConfig() {
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder()
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "common",
                                "id", "1",
                                "rules", "apple smartphone =>\n  SYNONYM: iphone\n"
                        )
                )
                .rewriteLoggingConfig(RewriteLoggingConfig.details())
                .build();

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .queryInput("apple smartphone")
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery();

        assertThat(rewrittenQuery.getRewriteLogging()).isNotEmpty();
    }
}
