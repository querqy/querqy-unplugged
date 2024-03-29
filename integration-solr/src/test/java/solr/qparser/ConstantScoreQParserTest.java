package solr.qparser;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import solr.SolrTestRequest;
import solr.SolrTestResult;

@SolrTestCaseJ4.SuppressSSL
public class ConstantScoreQParserTest extends SolrTestCaseJ4 {

    private static SolrClient SOLR_CLIENT;

    @BeforeClass
    public static void setupIndex() throws Exception {
        initCore("solrconfig.xml", "schema-bm25-similarity.xml");
        assertU(adoc("id", "1", "name", "iphone"));
        assertU(adoc("id", "2", "name", "apple"));
        assertU(adoc("id", "3", "name", "apple smartphone"));
        assertU(commit());

        SOLR_CLIENT = new EmbeddedSolrServer(h.getCoreContainer(), "collection1") {
            public void close() {
            }
        };
    }

    @Test
    public void testThat_scoreIsOne_forConstantScoreQueryWithoutBoost() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .solrClient(SOLR_CLIENT)
                .param("q", "{!constantScore filter=$filter1}")
                .param("fl", "id,name,score")
                .param("filter1", "{!term f=name v=apple}")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name", "score")
                        .doc("2", "apple", 1.0f)
                        .doc("3", "apple smartphone", 1.0f)
                        .build()
        );
    }

    @Test
    public void testThat_scoreIsTen_forConstantScoreQueryWithBoostOfTen() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .solrClient(SOLR_CLIENT)
                .param("q", "{!constantScore filter=$filter1 boost=10.0f}")
                .param("fl", "id,name,score")
                .param("filter1", "{!term f=name v=apple}")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name", "score")
                        .doc("2", "apple", 10.0f)
                        .doc("3", "apple smartphone", 10.0f)
                        .build()
        );
    }
}
