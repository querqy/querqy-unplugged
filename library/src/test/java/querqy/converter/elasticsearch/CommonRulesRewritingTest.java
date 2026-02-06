package querqy.converter.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.junit.Test;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterConfig;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;
import querqy.domain.RewrittenQuery;
import querqy.rewriter.builder.CommonRulesDefinition;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static querqy.QuerqyMatchers.bq;
import static querqy.QuerqyMatchers.dmq;
import static querqy.QuerqyMatchers.term;

public class CommonRulesRewritingTest {

    @Test
    public void testDecorationsWithoutKeys() {

        final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.of(
                ESJavaClientConverterConfig.builder()
                        .rawQueryInputType(ESJavaClientConverterConfig.RawQueryInputType.JSON)
                        .build()
        );

        final QuerqyConfig querqyConfig = QuerqyConfig.builder()

                .commonRules(
                        CommonRulesDefinition.builder()
                                .rewriterId("id1")
                                .rules("""
                                        iphone =>\s
                                         SYNONYM: apple smartphone
                                         FILTER: apple
                                         DECORATE: REDIRECT https://example.com/apple
                                        
                                        4 inch =>\s
                                         DECORATE: TEASER <small_smartphones>""")
                                .build()
                )
                .build();

        final QueryConfig queryConfig = QueryConfig.builder()
                .field("name", 40.0f)
                .field("type", 20.0f)
                .minimumShouldMatch("100%")
                .tie(0.0f)
                .build();

        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .querqyConfig(querqyConfig)
                .queryConfig(queryConfig)
                .converterFactory(converterFactory)
                .build();

        final RewrittenQuery<Query> query = queryRewriting.rewriteQuery("iphone 4 inch ");

        // test decorations
        org.assertj.core.api.Assertions.assertThat(query.getDecorations())
                .hasSize(2)
                .contains("REDIRECT https://example.com/apple")
                .contains("TEASER <small_smartphones>")

        ;

        // test that main query is still there
        final Query convertedQuery = query.getConvertedQuery();
        assertTrue(convertedQuery._get() instanceof BoolQuery);
        final BoolQuery boolQuery = (BoolQuery) convertedQuery._get();
        org.assertj.core.api.Assertions.assertThat(boolQuery.filter()).isNotEmpty(); // we just test whether the filter was created

    }



    @Test
    public void testThatRuleFiltersAreApplied() {

        final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.of(
                ESJavaClientConverterConfig.builder()
                        .rawQueryInputType(ESJavaClientConverterConfig.RawQueryInputType.JSON)
                        .build()
        );

        final QuerqyConfig querqyConfig = QuerqyConfig.builder()

                .commonRules(
                        CommonRulesDefinition.builder()
                                .rewriterId("id1")
                                .rules("""
                                        A =>\s
                                         SYNONYM: B
                                         @{
                                          "tenant": "t1"
                                         }@
                                        
                                        A =>\s
                                         SYNONYM: C
                                         @{
                                          "tenant": "t2"
                                         }@
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


        // test all synonyms are applied when there is no filter
        final RewrittenQuery<Query> query = queryRewriting.rewriteQuery("A");

        assertThat((querqy.model.Query) query.getRewrittenQuerqyQuery().getQuery().getUserQuery(),
        bq(
                dmq(
                        term("A", false),
                        term("B", true),
                        term("C", true)
                )
        ));


        final QueryConfig queryConfigWithFilter = QueryConfig.builder()
                .field("name", 40.0f)
                .minimumShouldMatch("100%")
                .tie(0.0f)
                .addRewriterParam("id1", "criteria.filter", "$[?(@.tenant == 't1')]" )
                .build();

        final QueryRewriting<Query> queryRewritingWithFilter = QueryRewriting.<Query>builder()
                .querqyConfig(querqyConfig)
                .queryConfig(queryConfigWithFilter)
                .converterFactory(converterFactory)
                .build();



        // now add the filter for tenant t1
        final RewrittenQuery<Query> queryWithAppliedFilter = queryRewritingWithFilter.rewriteQuery("A");
        assertThat((querqy.model.Query) queryWithAppliedFilter.getRewrittenQuerqyQuery().getQuery().getUserQuery(),
                bq(
                        dmq(
                                term("A", false),
                                term("B", true)
                        )
                ));

    }

    @Test
    public void testThatRuleOrderingAndLimitsAreApplied() {
        final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.of(
                ESJavaClientConverterConfig.builder()
                        .rawQueryInputType(ESJavaClientConverterConfig.RawQueryInputType.JSON)
                        .build()
        );

        final QuerqyConfig querqyConfig = QuerqyConfig.builder()

                .commonRules(
                        CommonRulesDefinition.builder()
                                .rewriterId("id1")
                                .rules("""
                                        A =>\s
                                         SYNONYM: B
                                         @{
                                          "prio": 2
                                         }@
                                        
                                        A =>\s
                                         SYNONYM: C
                                         @{
                                          "prio": 1
                                         }@
                                         """
                                )
                                .build()
                )
                .build();

        final QueryConfig queryConfig = QueryConfig.builder()
                .field("name", 40.0f)
                .minimumShouldMatch("100%")
                .tie(0.0f)
                .addRewriterParam("id1", "criteria.sort", "prio asc")
                .addRewriterParam("id1", "criteria.limit", 1)
                .addRewriterParam("id1", "criteria.limitByLevel", true)

                .build();

        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .querqyConfig(querqyConfig)
                .queryConfig(queryConfig)
                .converterFactory(converterFactory)
                .build();

        final RewrittenQuery<Query> queryWithAppliedOrder = queryRewriting.rewriteQuery("A");

        assertThat((querqy.model.Query) queryWithAppliedOrder.getRewrittenQuerqyQuery().getQuery().getUserQuery(),
                bq(
                        dmq(
                                term("A", false),
                                term("C", true)
                        )
                ));

    }


}
