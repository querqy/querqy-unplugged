package solr.qparser;

import org.apache.solr.SolrTestCaseJ4;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import solr.SolrTestResult;
import solr.SolrTestRequest;

@SolrTestCaseJ4.SuppressSSL
public class BoolQParserWrapperPluginTest extends SolrTestCaseJ4 {

    @BeforeClass
    public static void setupIndex() throws Exception {
        initCore("solrconfig.xml", "schema-boolean-similarity.xml");
        assertU(adoc("id", "1", "name", "iphone"));
        assertU(adoc("id", "2", "name", "apple"));
        assertU(adoc("id", "3", "name", "apple smartphone"));
        assertU(commit());
    }

    @Test
    public void testThat_twoDocsAreReturned_forBoolQueryWithSingleMustClause() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .testHarness(h)
                .handler("/select")
                .param("fl", "id,name")
                .param("q", "{!bool must=$must1}")
                .param("must1", "{!term f=name v=apple}")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name")
                        .doc("2", "apple")
                        .doc("3", "apple smartphone")
                        .build()
        );
    }

    @Test
    public void testThat_oneDocIsReturned_forBoolQueryWithTwoMustClauses() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .testHarness(h)
                .handler("/select")
                .param("fl", "id,name")
                .param("q", "{!bool must=$must1 must=$must2}")
                .param("must1", "{!field f=name v=apple}")
                .param("must2", "{!field f=name v=smartphone}")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name")
                        .doc("3", "apple smartphone")
                        .build()
        );
    }

    @Test
    public void testThat_twoDocsAreReturned_forBoolQueryWithTwoShouldClauses() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .testHarness(h)
                .handler("/select")
                .param("fl", "id,name")
                .param("q", "{!bool should=$should1 should=$should2}")
                .param("should1", "{!field f=name v=apple}")
                .param("should2", "{!field f=name v=smartphone}")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name")
                        .doc("2", "apple")
                        .doc("3", "apple smartphone")
                        .build()
        );
    }

    @Test
    public void testThat_oneDocIsReturned_forBoolQueryWithTwoShouldClausesAndMinimumShouldMatch() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .testHarness(h)
                .handler("/select")
                .param("fl", "id,name")
                .param("q", "{!bool should=$should1 should=$should2 mm=$mm1}")
                .param("should1", "{!field f=name v=apple}")
                .param("should2", "{!field f=name v=smartphone}")
                .param("mm1", "2")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name")
                        .doc("3", "apple smartphone")
                        .build()
        );
    }

    @Test
    public void testThat_scoreIsDecreased_forBoostLowerThanOne() throws Exception {
        SolrTestResult result = SolrTestRequest.builder()
                .testHarness(h)
                .handler("/select")
                .param("fl", "id,score")
                .param("q", "{!bool should=$should1 should=$should2}")
                .param("should1", "{!bool should=\"{!field f=name v=iphone}\"}")
                .param("should2", "{!bool should=\"{!field f=name v=smartphone}\" boost=0.5}")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "score")
                        .doc("1", 1.0f)
                        .doc("3", 0.5f)
                        .build()
        );
    }

}
