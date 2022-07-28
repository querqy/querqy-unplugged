package querqy.converter.solr.map;

import org.junit.Test;
import querqy.QueryConfig;
import querqy.model.ExpandedQuery;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static querqy.converter.solr.map.ConverterTestUtils.bqMap;
import static querqy.converter.solr.map.ConverterTestUtils.dmqMap;
import static querqy.converter.solr.map.ConverterTestUtils.termMap;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;

public class MapConverterTest {

    private final QueryConfig baseQueryConfig = QueryConfig.builder()
            .boolNodeName("bool")
            .disMaxNodeName("dis_max")
            .scoringNodeName("constant_score")
            .matchingNodeName("field")
            .build();

    @Test
    public void testThat_userQueryIsOnlyWrapped_forNoBoostOrFilterQuery() {
        final ExpandedQuery expandedQuery = expanded(bq("iphone")).build();

        final MapConverter converter = MapConverter.builder()
                .queryConfig(
                        baseQueryConfig.toBuilder()
                                .tie(0.5f)
                                .field("f", 1.0f)
                                .build()
                )
                .expandedQuery(expandedQuery)
                .build();

        final Map<String, Object> convertedQuery = converter.convert();

        assertThat(convertedQuery).isEqualTo(
                Map.of(
                        "query", bqMap(
                                "should",
                                dmqMap(
                                        0.5f,
                                        termMap("f", "iphone", 1.0f)
                                )

                        )
                )

        );
    }


}
