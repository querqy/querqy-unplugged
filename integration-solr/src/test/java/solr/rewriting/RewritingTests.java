package solr.rewriting;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import querqy.FieldConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.QuerqyConfig;
import querqy.QueryTypeConfig;
import querqy.converter.solr.map.MapConverterFactory;
import solr.SolrTestRequest;
import solr.SolrTestResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static querqy.rewriter.builder.RewriterSupport.createRewriterFactory;

public class RewritingTests extends SolrTestCaseJ4 {

    private static SolrClient SOLR_CLIENT;

    private final QueryConfig queryConfig = QueryConfig.builder()
            .field("name", 40.0f)
            .field("type", 20.0f)
            .minimumShouldMatch("100%")
            .tie(0.0f)
            .build();

    @BeforeClass
    public static void setupIndex() throws Exception {
        initCore("solrconfig.xml", "schema-boolean-similarity.xml");
        assertU(adoc("id", "1", "name", "iphone", "type", "smartphone"));
        assertU(adoc("id", "2", "name", "apple", "type", "smartphone"));
        assertU(adoc("id", "3", "name", "apple smartphone", "type", "smartphone"));
        assertU(adoc("id", "4", "name", "apple", "type", "case"));
        assertU(adoc("id", "5", "name", "samsung", "type", "case"));
        assertU(commit());

        SOLR_CLIENT = new EmbeddedSolrServer(h.getCoreContainer(), "collection1") {
            public void close() {
            }
        };
    }

    @Test
    public void testThat_allDocsAreReturned_forGivenMatchAllQuery() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(queryConfig)
                .querqyConfig(emptyQuerqyConfig())
                .converterFactory(MapConverterFactory.create())
                .build();

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery("*:*").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        assertEquals(result.size(), 5);
    }

    @Test
    public void testThat_filterIsApplied_forBeingIncludedInCommonRules() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(queryConfig)
                .querqyConfig(
                        singleRewriterConfig("apple =>\n  FILTER: * type:case")
                )
                .converterFactory(MapConverterFactory.create())
                .build();

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery("apple").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id")
                        .doc("4")
                        .build()
        );
    }

    @Test
    public void testThat_disjunctionIsCreated_forTermThatIsSplitInLucenePipelineAndLuceneQueryParser() throws Exception {

        final List<FieldConfig> fieldConfigs = queryConfig.getFields().stream()
                .map(
                        fieldConfig -> fieldConfig.toBuilder().queryTypeConfig(
                                QueryTypeConfig.builder()
                                        .typeName("lucene")
                                        .queryParamName("query")
                                        .fieldParamName("df")
                                        .build()
                        )
                )
                .map(FieldConfig.FieldConfigBuilder::build)
                .collect(Collectors.toList());

        final QueryRewriting<Map<String, Object>> queryRewritingHandler = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(queryConfig.toBuilder().fields(fieldConfigs).build())
                .querqyConfig(emptyQuerqyConfig())
                .converterFactory(MapConverterFactory.create())
                .build();

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery("apple&&&smartphone").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id")
                        .doc("1")
                        .doc("2")
                        .doc("3")
                        .doc("4")
                        .build()
        );
    }

    @Test
    public void testThat_scoringIsFair_forSimpleRepeatedClause() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(queryConfig)
                .querqyConfig(
                        singleRewriterConfig("apple smartphone =>\n  SYNONYM: iphone")
                )
                .converterFactory(MapConverterFactory.create())
                .build();

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery("apple smartphone").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id,name,type,score")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name", "type", "score")
                        .doc("1", "iphone", "smartphone", 80.0f)
                        .doc("3", "apple smartphone", "smartphone", 80.0f)
                        .doc("2", "apple", "smartphone", 60.0f)
                        .build()
        );
    }

    @Test
    public void testThat_scoringIsFair_forSimpleNestedClause() throws Exception {
        final QueryRewriting<Map<String, Object>> queryRewritingHandler = QueryRewriting.<Map<String, Object>>builder()
                .queryConfig(queryConfig)
                .querqyConfig(
                        singleRewriterConfig("iphone =>\n  SYNONYM: apple smartphone")
                )
                .converterFactory(MapConverterFactory.create())
                .build();

        final Map<String, Object> query = queryRewritingHandler.rewriteQuery("iphone").getConvertedQuery();

        final SolrTestResult result = SolrTestRequest.builder()
                .param("fl", "id,name,type,score")
                .query(query)
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name", "type", "score")
                        .doc("1", "iphone", "smartphone", 40.0f)
                        .doc("3", "apple smartphone", "smartphone", 40.0f)
                        .doc("2", "apple", "smartphone", 30.0f)
                        .build()
        );
    }

    private QuerqyConfig emptyQuerqyConfig() {
        return QuerqyConfig.builder().build();
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
