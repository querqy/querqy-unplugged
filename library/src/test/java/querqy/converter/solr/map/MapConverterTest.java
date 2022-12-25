package querqy.converter.solr.map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.model.ExpandedQuery;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static querqy.converter.solr.map.MapConverterTestUtils.bqMapSingleMust;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;

@RunWith(MockitoJUnitRunner.class)
public class MapConverterTest {

    @Mock private QuerqyQueryConverter querqyQueryConverter;
    @Mock private FilterConverter filterConverter;

    private final Map<String, Object> queryNode = Map.of("queryParser", "query");

    // TODO: simplify with @Before
    // TODO: mock ExpandedQuery?
    // TODO: test wrapping string from querqyQueryMapConverter

    private MapConverter mapConverter;

    @Before
    public void prepare() {
        when(querqyQueryConverter.convert(any())).thenReturn(queryNode);
        mapConverter = MapConverter.builder()
                .querqyQueryConverter(querqyQueryConverter)
                .filterConverter(filterConverter)
                .build();
    }

    @Test
    public void testThat_userQueryIsOnlyWrapped_forNoBoostAndNoFilterQuery() {
        final ExpandedQuery expandedQuery = expanded(bq("")).build();
        final Map<String, Object> convertedQuery = mapConverter.convert(expandedQuery);

        assertThat(convertedQuery).isEqualTo(
                Map.of("query", queryNode)
        );
    }

    @Test
    public void testThat_filterQueryIsPutInOuterQueryNode_forGivenFilterQuery() {
        final ExpandedQuery expandedQuery = expanded(bq(""), bq("")).build();

        when(filterConverter.convertFilterQueries(any())).thenReturn(List.of("filter"));

        final Map<String, Object> convertedQuery = mapConverter.convert(expandedQuery);

        assertThat(convertedQuery).isEqualTo(
                Map.of(
                        "query", bqMapSingleMust(queryNode),
                        "filter", List.of("filter")
                )

        );
    }

    @Test
    @Ignore
    public void testBoosts() {
    }
}
