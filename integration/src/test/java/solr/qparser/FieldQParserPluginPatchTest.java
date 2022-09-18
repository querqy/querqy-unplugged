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
public class FieldQParserPluginPatchTest extends SolrTestCaseJ4 {

    private static SolrClient SOLR_CLIENT;

    private static final String SHORT_TERM = "123";

    @BeforeClass
    public static void setupIndex() throws Exception {
        initCore("solrconfig.xml", "schema-boolean-similarity.xml");
        assertU(adoc("id", "1", "length_filtered", SHORT_TERM));
        assertU(commit());

        SOLR_CLIENT = new EmbeddedSolrServer(h.getCoreContainer(), "collection1") {
            public void close() {
            }
        };
    }

    @Test
    public void testThat_noExceptionIsThrown_forNestedFieldQueryAndNoAnalyzerOutput() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .solrClient(SOLR_CLIENT)
                .param("q", "{!bool must=$field_query1}")
                .param("field_query1", String.format("{!field f=length_filtered v=%s}", SHORT_TERM))
                .param("fl", "id,length_filtered")
                .build()
                .applyRequest();

        Assertions.assertThat(result).isEmpty();
    }
}
