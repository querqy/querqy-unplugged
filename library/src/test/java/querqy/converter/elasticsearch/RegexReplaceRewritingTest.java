package querqy.converter.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.junit.Assert;
import org.junit.Test;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterConfig;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;
import querqy.domain.RewrittenQuery;
import querqy.model.QuerqyQuery;
import querqy.rewriter.builder.CommonRulesDefinition;
import querqy.rewriter.builder.RegexReplaceRulesDefinition;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

import static querqy.QuerqyMatchers.bq;
import static querqy.QuerqyMatchers.dmq;
import static querqy.QuerqyMatchers.must;
import static querqy.QuerqyMatchers.term;

public class RegexReplaceRewritingTest {


    @Test
    public void testThat_RegexRewriterIsAppliedInChain() {

        final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.of(
                ESJavaClientConverterConfig.builder()
                        .rawQueryInputType(ESJavaClientConverterConfig.RawQueryInputType.JSON)
                        .build()
        );

            final QuerqyConfig querqyConfig = QuerqyConfig.builder()
                    .regexReplaceRules(
                            RegexReplaceRulesDefinition.builder()
                                    .rewriterId("id1")
                                    .rules("""
                                            iphone (\\d+) gb => apple smartphone ${1}gb
                                            (\\d+) ?x ?(\\d+) => ${1}x${2}
                                            """)
                                    .build()
                    )

                    .commonRules(
                            CommonRulesDefinition.builder()
                                    .rewriterId("id2")
                                    .rules("""
                                        apple =>\s
                                         FILTER: testcategory
                                        """
                                    )
                                    .build()
                    )
                    .build();

            final QueryConfig queryConfig = QueryConfig.builder()
                    .field("name", 40.0f)
                    .minimumShouldMatch("100%")
                    .tie(0.0f)
                    .build();

            final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                    .querqyConfig(querqyConfig)
                    .queryConfig(queryConfig)
                    .converterFactory(converterFactory)
                    .build();

            final RewrittenQuery<Query> rewrittenQuery = queryRewriting.rewriteQuery("iphone 128 gb");

            Assert.assertThat((querqy.model.Query) rewrittenQuery.getRewrittenQuerqyQuery().getQuery().getUserQuery(),
                    bq(
                            dmq(
                                    term("apple", false)
                            ),
                            dmq(
                                    term("smartphone", false)
                                    ),
                            dmq(
                                    term("128gb", false)
                                    )
                    ));

        final Collection<QuerqyQuery<?>> filterQueries = rewrittenQuery.getRewrittenQuerqyQuery().getQuery()
                .getFilterQueries();

        assertEquals(1, filterQueries.size());


        Assert.assertThat((querqy.model.Query) filterQueries.iterator().next(),
                bq(
                        dmq(
                            must(), term("testcategory", true)
                        )
                ));

    }
}
