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
                .param("defType", "bool")
                .param("fl", "id,name")
                .param("must", "{!term f=name v=apple}")
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
                .param("defType", "bool")
                .param("fl", "id,name")
                .param("must", "{!field f=name v=apple}")
                .param("must", "{!field f=name v=smartphone}")
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
                .param("defType", "bool")
                .param("fl", "id,name")
                .param("should", "{!field f=name v=apple}")
                .param("should", "{!field f=name v=smartphone}")
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
                .param("defType", "bool")
                .param("fl", "id,name")
                .param("mm", "2")
                .param("should", "{!field f=name v=apple}")
                .param("should", "{!field f=name v=smartphone}")
                .build()
                .applyRequest();

        Assertions.assertThat(result).containsExactlyInAnyOrderElementsOf(
                SolrTestResult.builder()
                        .fields("id", "name")
                        .doc("3", "apple smartphone")
                        .build()
        );
    }

}
