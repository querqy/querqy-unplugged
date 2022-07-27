package querqy.converter.solr.map;

import org.junit.Test;
import querqy.QueryConfig;
import querqy.model.convert.builder.ExpandedQueryBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static querqy.converter.solr.map.ConverterTestUtils.bqMap;
import static querqy.converter.solr.map.ConverterTestUtils.dmqMap;
import static querqy.converter.solr.map.ConverterTestUtils.termMap;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;

public class FilterMapConverterTest {

    private final QueryConfig baseQueryConfig = QueryConfig.builder()
            .boolNodeName("bool")
            .disMaxNodeName("dis_max")
            .scoringNodeName("constant_score")
            .matchingNodeName("field")
            .field("f", 1.0f)
            .build();

    @Test
    public void testThat_filterQueriesAreParsed_forTwoGivenFilterQueries() {
        final ExpandedQueryBuilder expanded = expanded(bq(List.of()), bq("a"), bq("b"));

        final FilterMapConverter converter = FilterMapConverter.builder()
                .queryConfig(baseQueryConfig)
                .filterQueries(expanded.build().getFilterQueries())
                .build();

        assertThat(converter.hasFilters()).isTrue();
        assertThat(converter.convertFilterQueries()).isEqualTo(
                List.of(
                        bqMap(
                                "should",
                                dmqMap(
                                        termMap("f", "a", 1.0f)
                                )
                        ),
                        bqMap(
                                "should",
                                dmqMap(
                                        termMap("f", "b", 1.0f)
                                )
                        )
                )
        );
    }

    @Test
    public void testThat_filterQueryIsParsed_forSingleGivenFilterQuery() {
        final ExpandedQueryBuilder expanded = expanded(bq(List.of()), bq("a"));

        final FilterMapConverter converter = FilterMapConverter.builder()
                .queryConfig(baseQueryConfig)
                .filterQueries(expanded.build().getFilterQueries())
                .build();

        assertThat(converter.hasFilters()).isTrue();
        assertThat(converter.convertFilterQueries()).isEqualTo(
                List.of(
                        bqMap(
                                "should",
                                dmqMap(
                                        termMap("f", "a", 1.0f)
                                )
                        )
                )
        );
    }

    @Test
    public void testThat_emptyListIsReturned_forGivenEmptyList() {
        final FilterMapConverter converter = FilterMapConverter.builder()
                .queryConfig(baseQueryConfig)
                .filterQueries(List.of())
                .build();

        assertThat(converter.hasFilters()).isFalse();
        assertThat(converter.convertFilterQueries()).isEqualTo(
                List.of()
        );
    }

    @Test
    public void testThat_emptyListIsReturned_forGivenNull() {
        final FilterMapConverter converter = FilterMapConverter.builder()
                .queryConfig(baseQueryConfig)
                .filterQueries(null)
                .build();

        assertThat(converter.hasFilters()).isFalse();
        assertThat(converter.convertFilterQueries()).isEqualTo(
                List.of()
        );
    }

    @Test
    public void testThat_filterIsParsedProperly_() {
        final FilterMapConverter converter = FilterMapConverter.builder()
                .queryConfig(baseQueryConfig)
                .filterQueries(null)
                .build();

        assertThat(converter.hasFilters()).isFalse();
        assertThat(converter.convertFilterQueries()).isEqualTo(
                List.of()
        );
    }
}
