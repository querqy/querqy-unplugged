package solr.rewriting;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import querqy.QueryConfig;
import querqy.QueryGenerator;
import querqy.QueryRewritingConfig;
import querqy.adapter.rewriter.builder.CommonRulesRewriterFactoryCreator;
import querqy.converter.solr.map.MapConverterFactory;
import solr.SolrTestJsonRequest;
import solr.SolrTestResult;

import java.io.IOException;
import java.util.Map;

public class RewritingTest extends SolrTestCaseJ4 {

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
    public void testThat_filterIsApplied_forBeingIncludedInCommonRules() throws Exception {
        final QueryGenerator<Map<String, Object>> queryGenerator = QueryGenerator.<Map<String, Object>>builder()
                .queryConfig(queryConfig)
                .queryRewritingConfig(
                        singleRewriterConfig("apple =>\n  FILTER: * type:case")
                )
                .converterFactory(MapConverterFactory.create())
                .build();

        final Map<String, Object> query = queryGenerator.generateQuery("apple");

        final SolrTestResult result = SolrTestJsonRequest.builder()
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
    public void testThat_scoringIsFair_forSimpleRepeatedClause() throws Exception {
        final QueryGenerator<Map<String, Object>> queryGenerator = QueryGenerator.<Map<String, Object>>builder()
                .queryConfig(queryConfig)
                .queryRewritingConfig(
                        singleRewriterConfig("apple smartphone =>\n  SYNONYM: iphone")
                )
                .converterFactory(MapConverterFactory.create())
                .build();

        final Map<String, Object> query = queryGenerator.generateQuery("apple smartphone");

        final SolrTestResult result = SolrTestJsonRequest.builder()
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
        final QueryGenerator<Map<String, Object>> queryGenerator = QueryGenerator.<Map<String, Object>>builder()
                .queryConfig(queryConfig)
                .queryRewritingConfig(
                        singleRewriterConfig("iphone =>\n  SYNONYM: apple smartphone")
                )
                .converterFactory(MapConverterFactory.create())
                .build();

        final Map<String, Object> query = queryGenerator.generateQuery("iphone");

        final SolrTestResult result = SolrTestJsonRequest.builder()
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

    private QueryRewritingConfig singleRewriterConfig(final String rules) throws IOException {
        return QueryRewritingConfig.builder()
                .rewriterFactory(
                        CommonRulesRewriterFactoryCreator.creator()
                                .rewriterId("1")
                                .rules(rules)
                                .createFactory()
                )
                .build();
    }
}
