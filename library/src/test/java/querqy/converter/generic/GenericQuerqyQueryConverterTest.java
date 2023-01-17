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
import querqy.converter.generic.model.BooleanQueryDefinition;
import querqy.converter.generic.model.DismaxQueryDefinition;
import querqy.model.BooleanQuery;
import querqy.model.Clause;
import querqy.model.DisjunctionMaxQuery;
import querqy.model.Query;
import querqy.model.Term;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenericQuerqyQueryConverterTest {

    @Mock private BooleanQueryBuilder<String> booleanQueryBuilder;
    @Mock private DismaxQueryBuilder<String> dismaxQueryBuilder;
    @Mock private GenericTermConverter<String> genericTermConverter;

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
        when(query.getOccur()).thenReturn(Clause.Occur.SHOULD);
        when(booleanQuery.getOccur()).thenReturn(Clause.Occur.SHOULD);

        genericQuerqyQueryConverter = GenericQuerqyQueryConverter.<String>builder()
                .booleanQueryBuilder(booleanQueryBuilder)
                .dismaxQueryBuilder(dismaxQueryBuilder)
                .genericTermConverter(genericTermConverter)
                .queryConfig(queryConfig)
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
    public void testThat_booleanQueryDividesBoostByNumberOfDismaxClauses_forTwoGivenClauses() {
        when(dismaxQuery.accept(any())).thenReturn("dismax");
        when(booleanQuery.getClauses()).thenReturn(List.of(dismaxQuery, dismaxQuery));

        genericQuerqyQueryConverter.visit(booleanQuery);
        verify(booleanQueryBuilder).build(booleanDefinitionCaptor.capture());

        assertThat(booleanDefinitionCaptor.getValue().getBoost()).isEqualTo(0.5f);
    }

    @Test
    public void testThat_booleanQueryBoostIsOne_forSingleDismaxClause() {
        when(dismaxQuery.accept(any())).thenReturn("dismax");
        when(booleanQuery.getClauses()).thenReturn(List.of(dismaxQuery));

        genericQuerqyQueryConverter.visit(booleanQuery);
        verify(booleanQueryBuilder).build(booleanDefinitionCaptor.capture());

        assertThat(booleanDefinitionCaptor.getValue().getBoost()).isEqualTo(1.0f);
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
}
