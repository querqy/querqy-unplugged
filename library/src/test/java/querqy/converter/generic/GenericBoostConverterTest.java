package querqy.converter.generic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.BoostConfig;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.model.BoostQuery;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenericBoostConverterTest {

    @Mock private GenericQuerqyQueryConverter<String> genericQuerqyQueryConverter;
    @Mock private BoostQueryBuilder<String> boostQueryBuilder;

    @Mock private BoostConfig boostConfig;
    @Mock private BoostQuery boostQuery;

    private GenericBoostConverter<String> genericBoostConverter;

    @Before
    public void setup() {
        when(genericQuerqyQueryConverter.convert(any())).thenReturn("");

        genericBoostConverter = GenericBoostConverter.<String>builder()
                .genericQuerqyQueryConverter(genericQuerqyQueryConverter)
                .boostQueryBuilder(boostQueryBuilder)
                .boostConfig(boostConfig)
                .build();
    }

    @Test
    public void testThat_boostUpQueriesAreBuilt_forTwoGivenBoostUpQueries() {
        genericBoostConverter.convert(
                List.of(boostQuery, boostQuery), List.of()
        );

        verify(boostQueryBuilder, times(2)).convertBoostUp(any());
    }

    @Test
    public void testThat_boostDownQueriesAreBuilt_forTwoGivenBoostDownQueries() {
        genericBoostConverter.convert(
                List.of(), List.of(boostQuery, boostQuery)
        );

        verify(boostQueryBuilder, times(2)).convertBoostDown(any());
    }

    @Test
    public void testThat_boostUpAndDownQueriesAreBuilt_forOneBoostUpQueryAndOneBoostDownQuery() {
        genericBoostConverter.convert(
                List.of(boostQuery), List.of(boostQuery)
        );

        verify(boostQueryBuilder).convertBoostUp(any());
        verify(boostQueryBuilder).convertBoostDown(any());
    }

}
