package solr.qparser;

import org.apache.solr.SolrTestCaseJ4;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import solr.SolrTestRequest;
import solr.SolrTestResult;

@SolrTestCaseJ4.SuppressSSL
public class NestedDisMaxQParserTest extends SolrTestCaseJ4 {

    @BeforeClass
    public static void setupIndex() throws Exception {
        initCore("solrconfig.xml", "schema-boolean-similarity.xml");
        assertU(adoc("id", "1", "name", "iphone"));
        assertU(adoc("id", "2", "name", "apple"));
        assertU(adoc("id", "3", "name", "apple smartphone"));
        assertU(commit());
    }

    @Test
    public void testThat_scoresAreSummed_forTieIsOne() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .testHarness(h)
                .handler("/select")
                .param("q", "{!dis_max queries=$queries1 queries=$queries2 tie=1.0f}")
                .param("fl", "id,name,score")
                .param("queries1", "{!field f=name v=apple}")
                .param("queries2", "{!field f=name v=smartphone}")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name", "score")
                        .doc("2", "apple", 1.0f)
                        .doc("3", "apple smartphone", 2.0f)
                        .build()
        );
    }

    @Test
    public void testThat_scoresArePartiallySummed_forTieIsZeroDotFive() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .testHarness(h)
                .handler("/select")
                .param("q", "{!dis_max queries=$queries1 queries=$queries2 tie=0.5f}")
                .param("fl", "id,name,score")
                .param("queries1", "{!field f=name v=apple}")
                .param("queries2", "{!field f=name v=smartphone}")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name", "score")
                        .doc("2", "apple", 1.0f)
                        .doc("3", "apple smartphone", 1.5f)
                        .build()
        );
    }

    @Test
    public void testThat_scoresAreMaxed_forTieIsZero() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .testHarness(h)
                .handler("/select")
                .param("q", "{!dis_max queries=$queries1 queries=$queries2 tie=0.0f}")
                .param("fl", "id,name,score")
                .param("queries1", "{!field f=name v=apple}")
                .param("queries2", "{!field f=name v=smartphone}")
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

}
