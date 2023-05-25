package solr.rewriting;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import querqy.BoostConfig;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.solr.map.SolrMapConverterFactory;
import solr.SolrTestRequest;
import solr.SolrTestResult;

import java.util.Map;

import static querqy.rewriter.builder.RewriterSupport.createRewriterFactory;

public class BoostTests extends SolrTestCaseJ4 {

    private static final String RULE = "apple =>\n  UP(100): iphone";

    private static SolrClient SOLR_CLIENT;

    private ConverterFactory<Map<String, Object>> converterFactory = SolrMapConverterFactory.create();

    @BeforeClass
    public static void setupIndex() throws Exception {
        initCore("solrconfig.xml", "schema-boolean-similarity.xml");
        assertU(adoc("id", "1", "name", "apple", "type", "iphone"));
        assertU(adoc("id", "2", "name", "apple", "type", "case"));
        assertU(commit());

        SOLR_CLIENT = new EmbeddedSolrServer(h.getCoreContainer(), "collection1") {
            public void close() {
            }
        };
    }

    @Test
    public void testThat_queryScoreIsIgnored() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = createQueryRewriting(
                BoostConfig.builder()
                        .queryScoreConfig(BoostConfig.QueryScoreConfig.IGNORE_QUERY_SCORE)
                        .build()
        );

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery("apple").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id,score")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "score")
                        .doc("1", 140.0f)
                        .doc("2", 40.0f)
                        .build()
        );
    }

    @Test
    public void testThat_queryScoreIsAddedToParam() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = createQueryRewriting(
                BoostConfig.builder()
                        .queryScoreConfig(BoostConfig.QueryScoreConfig.ADD_TO_BOOST_PARAM)
                        .build()
        );

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery("apple").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id,score")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "score")
                        .doc("1", 160.0f)
                        .doc("2", 40.0f)
                        .build()
        );
    }

    @Test
    public void testThat_queryScoreIsMultipliedWithParam() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = createQueryRewriting(
                BoostConfig.builder()
                        .queryScoreConfig(BoostConfig.QueryScoreConfig.MULTIPLY_WITH_BOOST_PARAM)
                        .build()
        );

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery("apple").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id,score")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "score")
                        .doc("1", 2040.0f)
                        .doc("2", 40.0f)
                        .build()
        );
    }

    private QueryRewriting<Map<String, Object>> createQueryRewriting(final BoostConfig boostConfig) {
        return QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(
                        QueryConfig.builder()
                                .field("name", 40.0f)
                                .field("type", 20.0f)
                                .minimumShouldMatch("100%")
                                .tie(0.5f)
                                .boostConfig(boostConfig)
                        .build()
                )
                .querqyConfig(
                        singleRewriterConfig(RULE)
                )
                .converterFactory(converterFactory)
                .build();
    }

    private QuerqyConfig singleRewriterConfig(final String rules) {
        return QuerqyConfig.builder()
                .rewriterFactory(
                        createRewriterFactory(
                                "common",
                                "id", "1",
                                "rules", rules
                        )
                )
                .build();
    }
}
