package solr.rewriting;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryExpansionConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.solr.map.SolrMapConverterFactory;
import solr.SolrTestRequest;
import solr.SolrTestResult;

import java.util.Map;

import static querqy.rewriter.builder.RewriterSupport.createRewriterFactory;

public class QueryExpansionTests extends SolrTestCaseJ4 {
    private static SolrClient SOLR_CLIENT;

    private final ConverterFactory<Map<String, Object>> converterFactory = SolrMapConverterFactory.create();

    @BeforeClass
    public static void setupIndex() throws Exception {
        initCore("solrconfig.xml", "schema-boolean-similarity.xml");
        assertU(adoc("id", "1", "name", "apple", "type", "smartphone"));
        assertU(adoc("id", "2", "name", "huawei", "type", "case"));
        assertU(adoc("id", "3", "name", "samsung", "type", "smartphone"));
        assertU(commit());

        SOLR_CLIENT = new EmbeddedSolrServer(h.getCoreContainer(), "collection1") {
            public void close() {
            }
        };
    }

    @Test
    public void testThat_resultsAreExpanded_forAlternativeMatchingStringQuery() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(createQueryConfig())
                .queryExpansionConfig(
                        QueryExpansionConfig.<Map<String, Object>>builder()
                                .addAlternativeMatchingStringQuery("name:huawei", 50f)
                                .build()
                )
                .converterFactory(converterFactory)
                .build();

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
                        .doc("2", 50.0f)
                        .doc("1", 40.0f)
                        .build()
        );
    }

    @Test
    public void testThat_productsAreBoosted_forBoostStringQuery() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(createQueryConfig())
                .queryExpansionConfig(
                        QueryExpansionConfig.<Map<String, Object>>builder()
                                .addBoostUpStringQuery("name:apple", 50f)
                                .build()
                )
                .converterFactory(converterFactory)
                .build();

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery("smartphone").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id,score")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "score")
                        .doc("1", 70.0f)
                        .doc("3", 20.0f)
                        .build()
        );
    }

    @Test
    public void testThat_resultsAreExpanded_forAlternativeMatchingQuery() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(createQueryConfig())
                .queryExpansionConfig(
                        QueryExpansionConfig.<Map<String, Object>>builder()
                                .addAlternativeMatchingQuery(
                                        Map.of(
                                                "field", Map.of(
                                                        "f", "name",
                                                        "v", "huawei"
                                                )
                                        ), 50f)
                                .build()
                )
                .converterFactory(converterFactory)
                .build();

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
                        .doc("2", 50.0f)
                        .doc("1", 40.0f)
                        .build()
        );
    }

    private QueryConfig createQueryConfig() {
        return QueryConfig.builder()
                .field("name", 40.0f)
                .field("type", 20.0f)
                .minimumShouldMatch("100%")
                .tie(0.0f)
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
