package querqy.converter.solr.map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.model.QuerqyQuery;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilterConverterTest {

    private final Map<String, Object> queryNode = Map.of("queryParser", "query");

    @Mock private QuerqyQueryConverter querqyQueryConverter;
    private FilterConverter converter;

    @Mock private QuerqyQuery<?> querqyQuery;

    @Before
    public void prepare() {
        converter = FilterConverter.builder()
                .querqyQueryConverter(querqyQueryConverter)
                .build();

        when(querqyQueryConverter.convert(any())).thenReturn(queryNode);
    }

    @Test
    public void testThat_filterQueriesAreParsed_forTwoGivenFilterQueries() {
        final List<Object> convertedFilterQueries = converter.convertFilterQueries(List.of(querqyQuery, querqyQuery));
        assertThat(convertedFilterQueries).isEqualTo(List.of(queryNode, queryNode));
    }

    @Test
    public void testThat_filterQueryIsParsed_forSingleGivenFilterQuery() {
        final List<Object> convertedFilterQuery = converter.convertFilterQueries(List.of(querqyQuery));
        assertThat(convertedFilterQuery).isEqualTo(List.of(queryNode));
    }

    @Test
    public void testThat_emptyListIsReturned_forGivenEmptyList() {
        assertThat(converter.convertFilterQueries(List.of())).isEqualTo(List.of());
    }

    @Test
    public void testThat_emptyListIsReturned_forGivenNull() {
        assertThat(converter.convertFilterQueries(null)).isEqualTo(List.of());
    }

}
