package querqy.converter.solr.map.builder;

import org.junit.Test;
import querqy.model.Clause;
import querqy.model.StringRawQuery;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SolrMapRawQueryBuilderTest {

    @Test
    public void testBuildFromStringRawQuery() {
        SolrMapQueryReferenceBuilder referenceBuilder = SolrMapQueryReferenceBuilder.create();
        final SolrMapRawQueryBuilder rawQueryBuilder = SolrMapRawQueryBuilder.of(referenceBuilder);
        final Map<String, Object> params = rawQueryBuilder.build(
                new StringRawQuery(null, "f1:v1 v2", Clause.Occur.MUST, true));

        assertThat(params).containsEntry("param", "_querqy_ref0");
        assertThat(referenceBuilder.getReferences()).containsEntry("_querqy_ref0", "f1:v1 v2");


    }
}