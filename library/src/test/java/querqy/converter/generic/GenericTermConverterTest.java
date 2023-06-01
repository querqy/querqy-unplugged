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
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;
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
    @Mock private ConstantScoreQueryBuilder<String> constantScoreQueryBuilder;

    @Mock private Term term;
    @Mock private BoostedTerm boostedTerm;
    @Mock private ComparableCharSequence charSequence;

    @Captor private ArgumentCaptor<TermQueryDefinition> termDefinitionCaptor;
    @Captor private ArgumentCaptor<String> queryCaptor;
    @Captor private ArgumentCaptor<Float> weightCaptor;

    private final QueryConfig twoFieldsQueryConfig = QueryConfig.builder()
            .field("f1", 5.0f)
            .field("f2", 5.0f)
            .build();

    private GenericTermConverter<String> genericTermConverter;

    @Before
    public void prepare() {
        when(boostedTerm.getValue()).thenReturn(charSequence);

        when(termQueryBuilder.build(any())).thenReturn("term");

        genericTermConverter = GenericTermConverter.<String>builder()
                .termQueryBuilder(termQueryBuilder)
                .constantScoreQueryBuilder(constantScoreQueryBuilder)
                .queryConfig(twoFieldsQueryConfig)
                .build();
    }

    @Test
    public void testThat_boostIsMultipliedWithFieldWeight_forBoostedTerm() {
        when(boostedTerm.getBoost()).thenReturn(3.0f);

        genericTermConverter.convert(boostedTerm);
        verify(constantScoreQueryBuilder, times(2)).build(queryCaptor.capture(), weightCaptor.capture());

        assertThat(queryCaptor.getAllValues()).isEqualTo(List.of("term", "term"));
        assertThat(weightCaptor.getAllValues()).isEqualTo(List.of(15.0f, 15.0f));
    }


}
