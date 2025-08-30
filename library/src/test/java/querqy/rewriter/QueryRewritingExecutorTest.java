package querqy.rewriter;

import org.junit.Test;
import querqy.QuerqyConfig;
import querqy.model.ExpandedQuery;
import querqy.model.MatchAllQuery;
import querqy.model.Query;
import querqy.parser.QuerqyParser;
import querqy.rewrite.RewriteLoggingConfig;
import querqy.rewriter.builder.ExpandedQueryParser;
import querqy.rewriter.builder.RewriterSupport;
import querqy.domain.RewrittenQuerqyQuery;
import querqy.model.convert.builder.ExpandedQueryBuilder;

import java.util.Set;

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
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery("*:*");

        assertThat(rewrittenQuery.getQuery().getUserQuery()).isInstanceOf(MatchAllQuery.class);
    }

    @Test
    public void testThat_matchAllQueryIsReturned_forWildcardInputString() {
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder().build();

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery("*");

        assertThat(rewrittenQuery.getQuery().getUserQuery()).isInstanceOf(MatchAllQuery.class);
    }

    @Test
    public void testThat_matchAllQueryIsReturned_forFieldValueMatchAllQuerqyquery() {
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder().build();
        String inputQuery = "*:*";

        ExpandedQuery expandedQuery =
                ExpandedQueryParser.create().parseQuery(rewritingConfig.getQuerqyParserFactory(), inputQuery);

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery(expandedQuery);

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
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery("aple applee");

        final ExpandedQueryBuilder expanded = expanded(rewrittenQuery.getQuery());

        assertThat(expanded.getUserQuery()).isEqualTo(
                bq("apple", "apple")
        );
    }

    @Test
    public void testThat_replacementsAreApplied_forGivenReplaceRulesRewriter_withQuerqyQuery() {
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

        QuerqyParser querqyParser = rewritingConfig.getQuerqyParserFactory().createParser();
        Query query = querqyParser.parse("aple applee");
        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery(query);

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
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery("apple smartphone");

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
    public void testThat_synonymsAreApplied_forGivenCommonRulesRewriter_withQuerqyQuery() {
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder()
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "common",
                                "id", "1",
                                "rules", "apple smartphone =>\n  SYNONYM: iphone"
                        )
                )
                .build();
        QuerqyParser querqyParser = rewritingConfig.getQuerqyParserFactory().createParser();
        Query query = querqyParser.parse("apple smartphone");

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery(query);

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
    public void testThat_rewriteLoggingIsNotEmpty_forActiveRewriteLoggingConfig() {
        final QuerqyConfig rewritingConfig = QuerqyConfig.builder()
                .rewriterFactory(
                        RewriterSupport.createRewriterFactory(
                                "common",
                                "id", "1",
                                "rules", "apple smartphone =>\n  SYNONYM: iphone\n"
                        )
                )
                .rewriteLoggingConfig(
                        RewriteLoggingConfig.builder()
                            .hasDetails(true).isActive(true)
                            .includedRewriters(Set.of("1")).build())
                .build();

        final RewrittenQuerqyQuery rewrittenQuery = QueryRewritingExecutor.builder()
                .querqyConfig(rewritingConfig)
                .build()
                .rewriteQuery("apple smartphone");

        assertThat(rewrittenQuery.getRewriteLogging()).isNotEmpty();
    }
}
