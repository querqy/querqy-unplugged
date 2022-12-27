package querqy.converter.solr.map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.QueryConfig;
import querqy.model.Query;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.DisjunctionMaxQueryBuilder.dmq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;
import static querqy.model.convert.builder.MatchAllQueryBuilder.matchall;
import static querqy.model.convert.builder.StringRawQueryBuilder.raw;

@RunWith(MockitoJUnitRunner.class)
public class QuerqyQueryConverterTest {

    @Mock private TermConverter termConverter;

    QuerqyQueryConverter defaultConverter;
    QueryConfig queryConfig;

    @Before
    public void prepare() {
        queryConfig = QueryConfig.empty();
        defaultConverter = QuerqyQueryConverter.builder()
                .queryConfig(queryConfig)
                .termConverter(termConverter)
                .build();
    }

    @Test
    public void testThat_queryIsParsedProperly_forGivenMatchAllQuery() {
        assertThat(defaultConverter.convert(matchall().build())).isEqualTo(
                Map.of(
                        "lucene", Map.of(
                                "v", "*:*"
                        )
                )
        );
    }

    @Test
    public void testThat_queryIsParsedProperly_forGivenRawQuery() {
        assertThat(defaultConverter.convert(raw("type:iphone").build())).isEqualTo(
                "type:iphone"
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testThat_tieIsAddedToDmq_forDmqAndDefinedTie() {
        when(termConverter.createTermQueries(any())).thenReturn(List.of("term"));

        final QuerqyQueryConverter converter = defaultConverter.toBuilder()
                .queryConfig(QueryConfig.builder().tie(0.5f).build())
                .build();

        final Map<String, Object> convertedQuery = (Map<String, Object>) converter.convert(dmq("").build());

        assertThat(convertedQuery).containsKey(queryConfig.getDismaxNodeName());
        assertThat(convertedQuery.get(queryConfig.getDismaxNodeName())).isInstanceOf(Map.class);
        assertThat((Map<String, Object>) convertedQuery.get(queryConfig.getDismaxNodeName())).containsEntry("tie", 0.5f);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testThat_minimumShouldMatchIsAdded_forGivenUserQuery() {
        when(termConverter.createTermQueries(any())).thenReturn(List.of("term1"));

        final QuerqyQueryConverter converter = defaultConverter.toBuilder()
                .queryConfig(QueryConfig.builder().minimumShouldMatch("100%").build())
                .build();

        final Query userQuery = (Query) expanded(bq("")).build().getUserQuery();

        final Map<String, Object> convertedQuery = (Map<String, Object>) converter.convert(userQuery);
        assertThat(convertedQuery.get(queryConfig.getBoolNodeName())).isInstanceOf(Map.class);
        assertThat((Map<String, Object>) convertedQuery.get(queryConfig.getBoolNodeName())).containsEntry("mm", "100%");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testThat_minimumShouldMatchIsNotAdded_forGivenBooleanQuery() {
        when(termConverter.createTermQueries(any())).thenReturn(List.of("term1"));

        final QuerqyQueryConverter converter = defaultConverter.toBuilder()
                .queryConfig(QueryConfig.builder().minimumShouldMatch("100%").build())
                .build();

        final Map<String, Object> convertedQuery = (Map<String, Object>) converter.convert(bq("").build());
        assertThat(convertedQuery.get(queryConfig.getBoolNodeName())).isInstanceOf(Map.class);
        assertThat((Map<String, Object>) convertedQuery.get(queryConfig.getBoolNodeName())).doesNotContainKey("mm");
    }
}
