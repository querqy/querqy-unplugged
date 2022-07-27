package querqy.converter.map;

import org.junit.Test;
import querqy.QueryConfig;
import querqy.model.ExpandedQuery;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static querqy.converter.map.ConverterTestUtils.bqMap;
import static querqy.converter.map.ConverterTestUtils.dmqMap;
import static querqy.converter.map.ConverterTestUtils.termMap;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.DisjunctionMaxQueryBuilder.dmq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;
import static querqy.model.convert.builder.MatchAllQueryBuilder.matchall;
import static querqy.model.convert.builder.StringRawQueryBuilder.raw;
import static querqy.model.convert.builder.TermBuilder.term;
import static querqy.model.convert.model.Occur.MUST;

public class QuerqyQueryMapConverterTest {

    private final QueryConfig baseQueryConfig = QueryConfig.builder()
            .boolNodeName("bool")
            .disMaxNodeName("dis_max")
            .scoringNodeName("constant_score")
            .matchingNodeName("field")
            .build();

    @Test
    public void testThat_queryIsParsedProperly_forGivenMatchAllQuery() {
        final QuerqyQueryMapConverter converter = QuerqyQueryMapConverter.builder()
                .queryConfig(
                        baseQueryConfig.toBuilder()
                                .field("f", 1.0f)
                                .build()
                )
                .node(matchall().build())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert()).isEqualTo("*:*");
    }

    @Test
    public void testThat_queryIsParsedProperly_forGivenRawQuery() {
        final QuerqyQueryMapConverter converter = QuerqyQueryMapConverter.builder()
                .queryConfig(
                        baseQueryConfig.toBuilder()
                                .field("f", 1.0f)
                                .build()
                )
                .node(raw("type:iphone").build())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert()).isEqualTo(
                "type:iphone"
        );
    }

    @Test
    public void testThat_tieIsAddedToDmq_forDmqAndDefinedTie() {
        final QuerqyQueryMapConverter converter = QuerqyQueryMapConverter.builder()
                .queryConfig(
                        baseQueryConfig.toBuilder()
                                .tie(0.5f)
                                .field("f", 1.0f)
                                .build()
                )
                .node(dmq("iphone").build())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert()).isEqualTo(
                dmqMap(
                        0.5f,
                        termMap("f", "iphone", 1.0f)
                )
        );
    }

    @Test
    public void testThat_termsAreExpanded_forDmqAndTwoFields() {
        final QuerqyQueryMapConverter converter = QuerqyQueryMapConverter.builder()
                .queryConfig(
                        baseQueryConfig.toBuilder()
                                .field("brand", 30.0f)
                                .field("type", 50.0f)
                                .build()
                )
                .node(dmq("iphone").build())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert()).isEqualTo(
                dmqMap(
                        termMap("brand", "iphone", 30.0f),
                        termMap("type", "iphone", 50.0f)
                )
        );
    }

    @Test
    public void testThat_termsAreExpandedWithinEachDmq_forTwoFieldsAndTwoQueryTerms() {
        final ExpandedQuery expandedQuery = expanded(bq("iphone", "12")).build();
        final QuerqyQueryMapConverter converter = QuerqyQueryMapConverter.builder()
                .queryConfig(
                        baseQueryConfig.toBuilder()
                                .field("brand", 30.0f)
                                .field("type", 50.0f)
                                .build()
                )
                .node(expandedQuery.getUserQuery())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert()).isEqualTo(
                bqMap(
                        "should",
                        dmqMap(
                                termMap("brand", "iphone", 30.0f),
                                termMap("type", "iphone", 50.0f)
                        ),
                        dmqMap(
                                termMap("brand", "12", 30.0f),
                                termMap("type", "12", 50.0f)
                        )
                )
        );
    }

    @Test
    public void testThat_minimumShouldMatchIsOnlyAddedToRootBq_forQueryWithAdditionalNestedBq() {
        final ExpandedQuery expandedQuery = expanded(
                bq(
                        dmq(
                                term("iphone"),
                                bq(
                                        dmq(
                                                List.of(term("apple")),
                                                MUST,
                                                true
                                        ),
                                        dmq(
                                                List.of(term("smartphone")),
                                                MUST,
                                                true
                                        )
                                )
                        ),
                        dmq("12")
                )
        ).build();


        final QuerqyQueryMapConverter converter = QuerqyQueryMapConverter.builder()
                .queryConfig(
                        baseQueryConfig.toBuilder()
                                .field("f", 1.0f)
                                .minimumShouldMatch("100%")
                                .build()
                )
                .node(expandedQuery.getUserQuery())
                .parseAsUserQuery(true)
                .build();

        assertThat(converter.convert()).isEqualTo(
                bqMap(
                        "should",
                        "100%",
                        dmqMap(
                                termMap("f", "iphone", 1.0f),
                                bqMap(
                                        0.5f,
                                        "must",
                                        dmqMap(termMap("f", "apple", 1.0f)),
                                        dmqMap(termMap("f", "smartphone", 1.0f))
                                )
                        ),
                        dmqMap(
                                termMap("f", "12", 1.0f)
                        )
                )
        );
    }

}
