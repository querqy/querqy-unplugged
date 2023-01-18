package querqy.converter.generic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.ComparableCharSequence;
import querqy.QueryConfig;
import querqy.converter.generic.builder.TermQueryBuilder;
import querqy.converter.generic.model.TermQueryDefinition;
import querqy.model.BoostedTerm;
import querqy.model.Term;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenericTermConverterTest {

    @Mock private TermQueryBuilder<String> termQueryBuilder;

    @Mock private Term term;
    @Mock private BoostedTerm boostedTerm;
    @Mock private ComparableCharSequence charSequence;

    @Captor private ArgumentCaptor<TermQueryDefinition> termDefinitionCaptor;

    private final QueryConfig twoFieldsQueryConfig = QueryConfig.builder()
            .field("f1", 10.0f)
            .field("f2", 5.0f)
            .build();

    private GenericTermConverter<String> genericTermConverter;

    @Before
    public void prepare() {
        when(term.getValue()).thenReturn(charSequence);
        when(boostedTerm.getValue()).thenReturn(charSequence);

        when(termQueryBuilder.build(any())).thenReturn("term");

        genericTermConverter = GenericTermConverter.<String>builder()
                .termQueryBuilder(termQueryBuilder)
                .queryConfig(twoFieldsQueryConfig)
                .build();
    }

    @Test
    public void testThat_builderIsCalledTwice_forTwoGivenFields() {
        final List<String> convertedTerms = genericTermConverter.convert(term);
        assertThat(convertedTerms).hasSize(2);
        assertThat(convertedTerms).containsExactlyInAnyOrder("term", "term");
    }

    @Test
    public void testThat_boostIsTaken_fromBoostedTerm() {
        when(boostedTerm.getBoost()).thenReturn(3.0f);

        genericTermConverter.convert(boostedTerm);
        verify(termQueryBuilder, times(2)).build(termDefinitionCaptor.capture());

        assertThat(termDefinitionCaptor.getValue().getTermBoost()).isEqualTo(3.0f);
    }


}
