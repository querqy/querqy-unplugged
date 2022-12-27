package querqy.converter.solr.map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.converter.solr.map.boost.BoostConverter;
import querqy.converter.solr.map.boost.ConvertedBoostQueries;
import querqy.model.BoostQuery;
import querqy.model.ExpandedQuery;
import querqy.model.QuerqyQuery;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static querqy.converter.solr.map.MapConverterTestUtils.bqMapSingleMust;

@RunWith(MockitoJUnitRunner.class)
public class MapConverterTest {

    @Mock private QuerqyQueryConverter querqyQueryConverter;
    @Mock private FilterConverter filterConverter;
    @Mock private BoostConverter boostConverter;

    @Mock private ExpandedQuery expandedQuery;
    @Mock private QuerqyQuery<?> querqyQuery;

    private final Map<String, Object> queryNode = Map.of("queryParser", "query");

    // TODO: test wrapping string from querqyQueryMapConverter

    private MapConverter mapConverter;

    @Before
    public void prepare() {
        when(querqyQueryConverter.convert(any())).thenReturn(queryNode);
        mapConverter = MapConverter.builder()
                .querqyQueryConverter(querqyQueryConverter)
                .filterConverter(filterConverter)
                .boostConverter(boostConverter)
                .build();
    }

    @Test
    public void testThat_userQueryIsOnlyWrapped_forNoBoostAndNoFilterQuery() {
        final Map<String, Object> convertedQuery = mapConverter.convert(expandedQuery);

        assertThat(convertedQuery).isEqualTo(
                Map.of("query", queryNode)
        );
    }

    @Test
    public void testThat_filterQueryIsPutInOuterQueryNode_forGivenFilterQuery() {
        when(expandedQuery.getFilterQueries()).thenReturn(List.of(querqyQuery));
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
    public void testThat_boostFunctionsAreAddedAsShouldClausesAndReferencesAddedToQueriesNode_forGivenBoostQueries() {
        when(expandedQuery.getBoostUpQueries()).thenReturn(List.of(new BoostQuery(querqyQuery, 1.0f)));
        when(boostConverter.convertBoostQueries(any(), any())).thenReturn(
                ConvertedBoostQueries.builder()
                        .boostFunctionQueries(List.of("functionQuery"))
                        .referencedQueries(Map.of("ref", "query"))
                        .build()
        );

        final Map<String, Object> convertedQuery = mapConverter.convert(expandedQuery);

        assertThat(convertedQuery).isEqualTo(
                Map.of(
                        "query", Map.of(
                                "bool", Map.of(
                                        "must", queryNode,
                                        "should", List.of("functionQuery")
                                )
                        ),
                        "queries", Map.of("ref", "query")
                )
        );
    }
}
