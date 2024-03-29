package solr.rewriting;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.junit.BeforeClass;
import org.junit.Test;
import querqy.BoostConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.QuerqyConfig;
import querqy.converter.solr.map.SolrMapConverterFactory;
import querqy.solr.rewriter.commonrules.CommonRulesConfigRequestBuilder;
import solr.SolrTestRequest;
import solr.SolrTestResult;

import java.util.Map;

import static querqy.rewriter.builder.RewriterSupport.createRewriterFactory;
import static solr.StandaloneSolrTestSupport.withCommonRulesRewriter;

public class ComparingRewritingTests extends SolrTestCaseJ4 {

    private static SolrClient SOLR_CLIENT;

    private static final String USER_QUERY = "apple";

    // TODO: down boost
    private static final Map<String, String> RULES = Map.of(
            "boost_common_rules", "apple => \n  UP: iphone",

            "negated_boost_common_rules", "apple => \n UP(100): -smartphone",

            "multiple_term_boost_common_rules", "apple => \n  UP: apple iphone",

            "weighted_boost_common_rules", "apple => \n  UP(50.0): iphone",

            "raw_boost_common_rules", "apple => \n  UP: * name:iphone",

            "weighted_raw_boost_common_rules", "apple => \n  UP(50.0): * type:iphone",

            "filter_common_rules", "apple => \n  FILTER: smartphone",

            "raw_filter_common_rules", "apple => \n  FILTER: * type:smartphone",

            "two_raw_filter_common_rules", "apple => \n  FILTER: * name:iphone \n apple => \n FILTER: * type:iphone",

            "weighted_synonym_common_rules", "apple => \n  SYNONYM(0.5): iphone"

    );

    @BeforeClass
    public static void setupIndex() throws Exception {
        System.setProperty("solr.install.dir", "resources");

        initCore("solrconfig.xml", "schema-bm25-similarity.xml");
        assertU(adoc("id", "1", "name", "apple iphone", "type", "iphone"));
        assertU(adoc("id", "2", "name", "apple iphone", "type", "smartphone"));
        assertU(adoc("id", "3", "name", "apple", "type", "iphone"));
        assertU(commit());

        RULES.forEach(
                (rewriterName, rules) ->
                        withCommonRulesRewriter(h.getCore(),
                                rewriterName,
                                new CommonRulesConfigRequestBuilder().rules(rules))
        );

        SOLR_CLIENT = new EmbeddedSolrServer(h.getCoreContainer(), "collection1") {
            public void close() {
            }
        };
    }

    private final QueryConfig queryConfig = QueryConfig.builder()
            .field("name", 40.0f)
            .field("type", 20.0f)
            .minimumShouldMatch("100%")
            .tie(0.5f)
            .boostConfig(BoostConfig.builder().queryScoreConfig(BoostConfig.QueryScoreConfig.CLASSIC).build())
            .build();

    @Test
    public void testThat_resultsAreIdentical_forBoostUpRule() throws Exception {
        final String rewriterName = "boost_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName).print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName)).print();
        assertEquals(paramResult, jsonResult);
    }

    // Score is not included here as the ranking is as expected, but the score varies
    @Test
    public void testThat_rankingsAreIdentical_forNegatedBoostRule() throws Exception {
        final String rewriterName = "negated_boost_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName, "id,name,type").print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName), "id,name,type").print();
        assertEquals(paramResult, jsonResult);
    }

    @Test
    public void testThat_resultsAreIdentical_forMultipleTermBoostUpRule() throws Exception {
        final String rewriterName = "multiple_term_boost_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName).print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName)).print();
        assertEquals(paramResult, jsonResult);
    }

    @Test
    public void testThat_resultsAreIdentical_forWeightedBoostUpRule() throws Exception {
        final String rewriterName = "weighted_boost_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName).print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName)).print();
        assertEquals(paramResult, jsonResult);
    }

    @Test
    public void testThat_resultsAreIdentical_forRawBoostUpRule() throws Exception {
        final String rewriterName = "raw_boost_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName).print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName)).print();
        assertEquals(paramResult, jsonResult);
    }

    @Test
    public void testThat_resultsAreIdentical_forWeightedRawBoostUpRule() throws Exception {
        final String rewriterName = "weighted_raw_boost_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName).print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName)).print();
        assertEquals(paramResult, jsonResult);
    }

    @Test
    public void testThat_resultsAreIdentical_forFilterRule() throws Exception {
        final String rewriterName = "filter_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName).print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName)).print();
        assertEquals(paramResult, jsonResult);
    }

    @Test
    public void testThat_resultsAreIdentical_forRawFilterRule() throws Exception {
        final String rewriterName = "raw_filter_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName).print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName)).print();
        assertEquals(paramResult, jsonResult);
    }

    @Test
    public void testThat_resultsAreIdentical_forTwoRawFilterRules() throws Exception {
        final String rewriterName = "two_raw_filter_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName).print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName)).print();
        assertEquals(paramResult, jsonResult);
    }

    @Test
    public void testThat_resultsAreIdentical_forWeightedSynonymRule() throws Exception {
        final String rewriterName = "weighted_synonym_common_rules";

        final SolrTestResult paramResult = applyParamRequest(rewriterName).print();
        final SolrTestResult jsonResult = applyJsonRequest(RULES.get(rewriterName)).print();
        assertEquals(paramResult, jsonResult);
    }

    private SolrTestResult applyJsonRequest(final String rules) throws Exception {
        return applyJsonRequest(rules, "id,name,type,score");
    }

    private SolrTestResult applyJsonRequest(final String rules, final String fieldList) throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(queryConfig)
                .querqyConfig(singleRewriterConfig(rules))
                .converterFactory(SolrMapConverterFactory.create())
                .build();

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery(USER_QUERY).getConvertedQuery();

        return SolrTestRequest.builder()
                .solrClient(SOLR_CLIENT)
                .query(query)
                .param("fl", fieldList)
                .build()
                .applyRequest();
    }

    private SolrTestResult applyParamRequest(final String rewriterName) throws Exception {
        return applyParamRequest(rewriterName, "id,name,type,score");
    }

    private SolrTestResult applyParamRequest(final String rewriterName, final String fieldList) throws Exception {
        return SolrTestRequest.builder()
                .solrClient(SOLR_CLIENT)
                .param("q", USER_QUERY)
                .param("defType", "querqy")
                .param("qf", "name^40 type^20")
                .param("tie", "0.5")
                .param("mm", "100%")
                .param("uq.similarityScore", "off")
                .param("qboost.similarityScore", "off")
                .param("qboost.fieldBoost", "on")
                .param("querqy.rewriters", rewriterName)
                .param("fl", fieldList)
                .build()
                .applyRequest();
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
