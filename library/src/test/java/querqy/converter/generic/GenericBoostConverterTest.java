package querqy.converter.generic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.BoostConfig;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;
import querqy.model.BoostQuery;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenericBoostConverterTest {

    @Mock private GenericQuerqyQueryConverter<String> genericQuerqyQueryConverter;
    @Mock private BoostQueryBuilder<String> boostQueryBuilder;

    @Mock private BooleanQueryBuilder<String> booleanQueryBuilder;
    @Mock private ConstantScoreQueryBuilder<String> constantScoreQueryBuilder;

    @Mock private BoostQuery boostQuery;

    @Before
    public void setup() {
        when(genericQuerqyQueryConverter.convert(any())).thenReturn("");
    }

    private GenericBoostConverter<String> createBoostConverter(final BoostConfig boostConfig) {
        return GenericBoostConverter.<String>builder()
                .genericQuerqyQueryConverter(genericQuerqyQueryConverter)
                .boostQueryBuilder(boostQueryBuilder)
                .booleanQueryBuilder(booleanQueryBuilder)
                .constantScoreQueryBuilder(constantScoreQueryBuilder)
                .boostConfig(boostConfig)
                .build();
    }

    @Test
    public void testThat_constantScoreQueryBuilderIsCalledTwice_forIgnoreBoostScoresConfigAndTwoGivenQueries() {
        final GenericBoostConverter<String> genericBoostConverter = createBoostConverter(
                BoostConfig.builder().queryScoreConfig(BoostConfig.QueryScoreConfig.IGNORE_QUERY_SCORE).build()
        );

        genericBoostConverter.convert(
                List.of(boostQuery, boostQuery), List.of()
        );

        verify(constantScoreQueryBuilder, times(2)).build(any(), anyFloat());
    }

    @Test
    public void testThat_constantScoreQueryBuilderIsCalled_forIgnoreBoostScoresConfig() {
        final GenericBoostConverter<String> genericBoostConverter = createBoostConverter(
                BoostConfig.builder().queryScoreConfig(BoostConfig.QueryScoreConfig.IGNORE_QUERY_SCORE).build()
        );

        genericBoostConverter.convert(
                List.of(boostQuery), List.of()
        );

        verify(constantScoreQueryBuilder).build(any(), anyFloat());
    }

    @Test
    public void testThat_constantScoreQueryBuilderAndBooleanBuilderAreCalled_forBoostDownQuery() {
        final GenericBoostConverter<String> genericBoostConverter = createBoostConverter(
                BoostConfig.builder().queryScoreConfig(BoostConfig.QueryScoreConfig.IGNORE_QUERY_SCORE).build()
        );

        genericBoostConverter.convert(
                List.of(), List.of(boostQuery)
        );

        verify(constantScoreQueryBuilder).build(any(), anyFloat());
        verify(booleanQueryBuilder).build(any());
    }

}
