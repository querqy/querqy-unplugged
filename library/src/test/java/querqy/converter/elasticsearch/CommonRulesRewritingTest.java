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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

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
        assertThat(query.getDecorations())
                .hasSize(2)
                .contains("REDIRECT https://example.com/apple")
                .contains("TEASER <small_smartphones>")

        ;

        // test that main query is still there
        final Query convertedQuery = query.getConvertedQuery();
        assertTrue(convertedQuery._get() instanceof BoolQuery);
        final BoolQuery boolQuery = (BoolQuery) convertedQuery._get();
        assertThat(boolQuery.filter()).isNotEmpty(); // we just test whether the filter was created

    }


}
