package querqy.converter.generic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.QueryConfig;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.DismaxQueryBuilder;
import querqy.converter.generic.builder.MatchAllQueryBuilder;
import querqy.converter.generic.builder.RawQueryBuilder;
import querqy.converter.generic.model.BooleanQueryDefinition;
import querqy.converter.generic.model.DismaxQueryDefinition;
import querqy.model.BooleanQuery;
import querqy.model.Clause;
import querqy.model.DisjunctionMaxQuery;
import querqy.model.MatchAllQuery;
import querqy.model.Query;
import querqy.model.StringRawQuery;
import querqy.model.Term;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(MockitoJUnitRunner.class)
public class GenericQuerqyQueryConverterTest {

    @Mock private BooleanQueryBuilder<String> booleanQueryBuilder;
    @Mock private DismaxQueryBuilder<String> dismaxQueryBuilder;
    @Mock private GenericTermConverter<String> genericTermConverter;

    @Mock private MatchAllQueryBuilder<String> matchAllQueryBuilder;
    @Mock private RawQueryBuilder<String> rawQueryBuilder;

    @Captor private ArgumentCaptor<BooleanQueryDefinition<String>> booleanDefinitionCaptor;
    @Captor private ArgumentCaptor<DismaxQueryDefinition<String>> dismaxDefinitionCaptor;

    @Mock private Query query;
    @Mock private BooleanQuery booleanQuery;
    @Mock private DisjunctionMaxQuery dismaxQuery;
    @Mock private Term term;

    private final QueryConfig queryConfig = QueryConfig.builder()
            .minimumShouldMatch("100%")
            .tie(0.5f)
            .build();

    private GenericQuerqyQueryConverter<String> genericQuerqyQueryConverter;

    @Before
    public void setup() {
        genericQuerqyQueryConverter = GenericQuerqyQueryConverter.<String>builder()
                .queryConfig(queryConfig)
                .booleanQueryBuilder(booleanQueryBuilder)
                .dismaxQueryBuilder(dismaxQueryBuilder)
                .genericTermConverter(genericTermConverter)
                .matchAllQueryBuilder(matchAllQueryBuilder)
                .rawQueryBuilder(rawQueryBuilder)
                .build();
    }

    @Test
    public void testThat_booleanQueryDefinitionIncludesMM_forVisitingTypeQuery() {
        genericQuerqyQueryConverter.visit(query);
        verify(booleanQueryBuilder).build(booleanDefinitionCaptor.capture());

        assertThat(booleanDefinitionCaptor.getValue().getMinimumShouldMatch()).isPresent();
        assertThat(booleanDefinitionCaptor.getValue().getMinimumShouldMatch().get()).isEqualTo("100%");
    }

    @Test
    public void testThat_booleanQueryDefinitionNotIncludesMM_forVisitingTypeBooleanQuery() {
        genericQuerqyQueryConverter.visit(booleanQuery);
        verify(booleanQueryBuilder).build(booleanDefinitionCaptor.capture());

        assertThat(booleanDefinitionCaptor.getValue().getMinimumShouldMatch()).isNotPresent();
    }

    @Test
    public void testThat_booleanQueryDividesBoostByNumberOfMustClauses_forTwoGivenClauses() {
        when(dismaxQuery.accept(any())).thenReturn("dismax");
        when(booleanQuery.getClauses()).thenReturn(List.of(dismaxQuery, dismaxQuery));
        when(dismaxQuery.getOccur()).thenReturn(Clause.Occur.MUST);

        genericQuerqyQueryConverter.visit(booleanQuery);
        verify(booleanQueryBuilder).build(booleanDefinitionCaptor.capture());

        assertThat(booleanDefinitionCaptor.getValue().getBoost().get()).isEqualTo(0.5f);
    }

    @Test
    public void testThat_booleanQueryBoostIsOne_forSingleShouldClause() {
        when(dismaxQuery.accept(any())).thenReturn("dismax");
        when(booleanQuery.getClauses()).thenReturn(List.of(dismaxQuery));
        when(dismaxQuery.getOccur()).thenReturn(Clause.Occur.SHOULD);

        genericQuerqyQueryConverter.visit(booleanQuery);
        verify(booleanQueryBuilder).build(booleanDefinitionCaptor.capture());

        assertThat(booleanDefinitionCaptor.getValue().getBoost().get()).isEqualTo(1.0f);
    }

    @Test
    public void testThat_booleanQueryIncludesShouldClause_forOccurShould() {
        when(dismaxQuery.accept(any())).thenReturn("dismax");
        when(booleanQuery.getClauses()).thenReturn(List.of(dismaxQuery));
        when(dismaxQuery.getOccur()).thenReturn(Clause.Occur.SHOULD);

        genericQuerqyQueryConverter.visit(booleanQuery);
        verify(booleanQueryBuilder).build(booleanDefinitionCaptor.capture());

        assertThat(booleanDefinitionCaptor.getValue().getShouldClauses()).hasSize(1);
    }

    @Test
    public void testThat_booleanQueryIncludesMustClause_forOccurShould() {
        when(dismaxQuery.accept(any())).thenReturn("dismax");
        when(booleanQuery.getClauses()).thenReturn(List.of(dismaxQuery));
        when(dismaxQuery.getOccur()).thenReturn(Clause.Occur.MUST);

        genericQuerqyQueryConverter.visit(booleanQuery);
        verify(booleanQueryBuilder).build(booleanDefinitionCaptor.capture());

        assertThat(booleanDefinitionCaptor.getValue().getMustClauses()).hasSize(1);
    }

    @Test
    public void testThat_booleanQueryIncludesMustNotClause_forOccurShould() {
        when(dismaxQuery.accept(any())).thenReturn("dismax");
        when(booleanQuery.getClauses()).thenReturn(List.of(dismaxQuery));
        when(dismaxQuery.getOccur()).thenReturn(Clause.Occur.MUST_NOT);

        genericQuerqyQueryConverter.visit(booleanQuery);
        verify(booleanQueryBuilder).build(booleanDefinitionCaptor.capture());

        assertThat(booleanDefinitionCaptor.getValue().getMustNotClauses()).hasSize(1);
    }

    @Test
    public void testThat_tieIsIncludedInDismaxDefinition_forTieInQueryConfig() {
        genericQuerqyQueryConverter.visit(dismaxQuery);
        verify(dismaxQueryBuilder).build(dismaxDefinitionCaptor.capture());

        assertThat(dismaxDefinitionCaptor.getValue().getTie()).isPresent();
        assertThat(dismaxDefinitionCaptor.getValue().getTie().get()).isEqualTo(0.5f);
    }

    @Test
    public void testThat_allTermsOfDismaxClauseAreAggregated_forTwoTerms() {
        when(dismaxQuery.getClauses()).thenReturn(List.of(term, term));
        when(genericTermConverter.convert(any())).thenReturn(List.of("term", "term"));

        genericQuerqyQueryConverter.visit(dismaxQuery);
        verify(dismaxQueryBuilder).build(dismaxDefinitionCaptor.capture());

        assertThat(dismaxDefinitionCaptor.getValue().getDismaxClauses()).hasSize(4);
    }

    @Test
    public void testThat_matchAllBuilderIsUsed_forGivenMatchAllQuery() {
        genericQuerqyQueryConverter.convert(new MatchAllQuery());
        verify(matchAllQueryBuilder).build();
    }

    @Test
    public void testThat_rawQueryBuilderIsUsed_forGivenRawQuery() {
        genericQuerqyQueryConverter.convert(new StringRawQuery(null,"q", null, false));
        verify(rawQueryBuilder).buildFromString("q");
    }
}
