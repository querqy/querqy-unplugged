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
import querqy.rewriter.builder.FieldBoost;
import querqy.rewriter.builder.PhraseBoostDefinition;
import querqy.rewriter.builder.PhraseConfig;
import solr.SolrTestRequest;
import solr.SolrTestResult;

import java.util.Map;

public class PhraseBoostTests extends SolrTestCaseJ4 {

    private static SolrClient SOLR_CLIENT;

    private final ConverterFactory<Map<String, Object>> converterFactory = SolrMapConverterFactory.create();

    @BeforeClass
    public static void setupIndex() throws Exception {
        initCore("solrconfig.xml", "schema-boolean-similarity.xml");

        // All docs contain both query terms so they all match with minimumShouldMatch=100%.
        // Doc 1: adjacent phrase → matches slop=0 and slop=1.
        // Doc 2, 3: one word between the terms (distance=1) → match only slop=1, not slop=0.
        assertU(adoc("id", "1", "name", "apple smartphone"));
        assertU(adoc("id", "2", "name", "apple great smartphone"));
        assertU(adoc("id", "3", "name", "apple new smartphone"));
        assertU(commit());

        SOLR_CLIENT = new EmbeddedSolrServer(h.getCoreContainer(), "collection1") {
            public void close() {
            }
        };
    }

    @Test
    public void testThat_exactPhraseMatchScoresHigher_thanNonAdjacentTerms() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewriting = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(QueryConfig.builder()
                        .field("name", 40.0f)
                        .minimumShouldMatch("100%")
                        .tie(0.0f)
                        .boostConfig(BoostConfig.builder()
                                .queryScoreConfig(BoostConfig.QueryScoreConfig.IGNORE_QUERY_SCORE)
                                .build())
                        .build())
                .querqyConfig(QuerqyConfig.builder()
                        .phraseBoost(PhraseBoostDefinition.builder()
                                .rewriterId("phrase-boost")
                                .bigram(PhraseConfig.builder()
                                        .field(FieldBoost.builder().field("name").boost(100.0f).build())
                                        .slop(0)
                                        .build())
                                .build())
                        .build())
                .converterFactory(converterFactory)
                .build();

        final Map<String, Object> query = queryRewriting.rewriteQuery("apple smartphone").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id,score")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        // doc 1 scores 81.0: base (40+40) + phrase boost (1.0, IGNORE_QUERY_SCORE wraps in constantScore)
        // docs 2 and 3 score 80.0: base only, no adjacent phrase match at slop=0
        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "score")
                        .doc("1", 81.0f)
                        .doc("2", 80.0f)
                        .doc("3", 80.0f)
                        .build()
        );
    }

    @Test
    public void testThat_slopAllowsNearbyPhraseToScore() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewriting = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(QueryConfig.builder()
                        .field("name", 40.0f)
                        .minimumShouldMatch("100%")
                        .tie(0.0f)
                        .boostConfig(BoostConfig.builder()
                                .queryScoreConfig(BoostConfig.QueryScoreConfig.IGNORE_QUERY_SCORE)
                                .build())
                        .build())
                .querqyConfig(QuerqyConfig.builder()
                        .phraseBoost(PhraseBoostDefinition.builder()
                                .rewriterId("phrase-boost")
                                .bigram(PhraseConfig.builder()
                                        .field(FieldBoost.builder().field("name").boost(100.0f).build())
                                        .slop(1)
                                        .build())
                                .build())
                        .build())
                .converterFactory(converterFactory)
                .build();

        final Map<String, Object> query = queryRewriting.rewriteQuery("apple smartphone").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id,score")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        // All docs match "apple smartphone"~1 (each has at most 1 word between the terms),
        // so all three receive the phrase boost → 80.0 base + 1.0 constantScore boost = 81.0
        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "score")
                        .doc("1", 81.0f)
                        .doc("2", 81.0f)
                        .doc("3", 81.0f)
                        .build()
        );
    }
}
