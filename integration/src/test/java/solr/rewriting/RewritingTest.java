package solr.rewriting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.junit.BeforeClass;
import org.junit.Test;
import querqy.converter.json.BoolQueryJsonNode;
import solr.SolrTestJsonRequest;
import solr.SolrTestResult;

import java.util.Map;

import static querqy.converter.json.BoolQueryJsonNode.boolJson;
import static querqy.converter.json.DisMaxQueryJsonNode.dismaxJson;
import static querqy.converter.json.FieldQueryJsonNode.fieldJson;

public class RewritingTest extends SolrTestCaseJ4 {

    private static SolrClient SOLR_CLIENT;

    @BeforeClass
    public static void setupIndex() throws Exception {
        initCore("solrconfig.xml", "schema-boolean-similarity.xml");
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
    public void testThat_scoresAreSummed_forTieIsOne() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);


        BoolQueryJsonNode bq = BoolQueryJsonNode.boolJson(
                dismaxJson(
                        fieldJson("name", "iphone"),
                        boolJson(BoolQueryJsonNode.BoolOperator.MUST,
                                dismaxJson(fieldJson("name", "apple")),
                                dismaxJson(fieldJson("name", "smartphone"))
                        )
                )
        );


        SolrTestResult result = SolrTestJsonRequest.builder()
                .param("qt", "/select")
                .param("fl", "id,name,score")
                .query(objectMapper.convertValue(bq, Map.class))
                .solrClient(SOLR_CLIENT)
                .build()
                .applyRequest();

    }
}
