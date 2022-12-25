package querqy.converter.solr.map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.QueryConfig;
import querqy.model.BoostedTerm;
import querqy.model.DisjunctionMaxQuery;
import querqy.model.ExpandedQuery;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static querqy.converter.solr.map.MapConverterTestUtils.bqMap;
import static querqy.converter.solr.map.MapConverterTestUtils.dmqMap;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.DisjunctionMaxQueryBuilder.dmq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;
import static querqy.model.convert.builder.MatchAllQueryBuilder.matchall;
import static querqy.model.convert.builder.StringRawQueryBuilder.raw;
import static querqy.model.convert.builder.TermBuilder.term;
import static querqy.model.convert.model.Occur.MUST;

@RunWith(MockitoJUnitRunner.class)
public class QuerqyQueryConverterTest {

    @Mock private TermConverter termConverter;

    @Test
    public void testThat_queryIsParsedProperly_forGivenMatchAllQuery() {
        final QuerqyQueryConverter converter = QuerqyQueryConverter.builder()
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert(matchall().build())).isEqualTo(
                Map.of(
                        "lucene", Map.of(
                                "v", "*:*"
                        )
                )
        );
    }

    @Test
    public void testThat_queryIsParsedProperly_forGivenRawQuery() {
        final QuerqyQueryConverter converter = QuerqyQueryConverter.builder()
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert(raw("type:iphone").build())).isEqualTo(
                "type:iphone"
        );
    }

    @Test
    public void testThat_fieldScoreIsAdjusted_forWeightedTerm() {
        final DisjunctionMaxQuery dmq = dmq(List.of()).build();
        final BoostedTerm boostedTerm = new BoostedTerm(dmq, "iphone", 0.5f);
        dmq.getClauses().add(boostedTerm);

        when(termConverter.createTermQueries(any())).thenReturn(List.of("term"));

        final QuerqyQueryConverter converter = QuerqyQueryConverter.builder()
                .queryConfig(QueryConfig.empty())
                .termConverter(termConverter)
                .converterConfig(MapConverterConfig.defaultConfig())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert(dmq)).isEqualTo(
                dmqMap("term")
        );
    }

    @Test
    public void testThat_tieIsAddedToDmq_forDmqAndDefinedTie() {
        when(termConverter.createTermQueries(any())).thenReturn(List.of("term"));

        final QuerqyQueryConverter converter = QuerqyQueryConverter.builder()
                .queryConfig(QueryConfig.builder().tie(0.5f).build())
                .termConverter(termConverter)
                .converterConfig(MapConverterConfig.defaultConfig())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert(dmq("iphone").build())).isEqualTo(
                dmqMap(0.5f, "term")
        );
    }

    @Test
    public void testThat_allTermsAreAdded_forDmqAndTwoFields() {
        when(termConverter.createTermQueries(any())).thenReturn(List.of("term1", "term2"));

        final QuerqyQueryConverter converter = QuerqyQueryConverter.builder()
                .queryConfig(QueryConfig.empty())
                .termConverter(termConverter)
                .converterConfig(MapConverterConfig.defaultConfig())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert(dmq("iphone").build())).isEqualTo(
                dmqMap("term1", "term2")
        );
    }

    @Test
    public void testThat_minimumShouldMatchIsOnlyAddedToRootBq_forQueryWithAdditionalNestedBq() {
        final ExpandedQuery expandedQuery = expanded(
                bq(
                        dmq(
                                term(""),
                                bq(
                                        dmq(List.of(term("")), MUST, true),
                                        dmq(List.of(term("")), MUST, true)
                                )
                        ),
                        dmq("")
                )
        ).build();

        when(termConverter.createTermQueries(any())).thenReturn(List.of("term"));

        final QuerqyQueryConverter converter = QuerqyQueryConverter.builder()
                .queryConfig(
                        QueryConfig.builder()
                                .field("f", 1.0f)
                                .minimumShouldMatch("100%")
                                .build()
                )
                .termConverter(termConverter)
                .converterConfig(MapConverterConfig.defaultConfig())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert(expandedQuery.getUserQuery())).isEqualTo(
                bqMap(
                        "should",
                        "100%",
                        dmqMap(
                                "term",
                                bqMap(
                                        0.5f,
                                        "must",
                                        dmqMap("term"),
                                        dmqMap("term")
                                )
                        ),
                        dmqMap("term")
                )
        );
    }

}
