package querqy.converter.generic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.BoostConfig;
import querqy.QueryConfig;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.BoostQueryBuilder;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;
import querqy.converter.generic.builder.DismaxQueryBuilder;
import querqy.converter.generic.builder.ExpandedQueryBuilder;
import querqy.converter.generic.builder.MatchAllQueryBuilder;
import querqy.converter.generic.builder.RawQueryBuilder;
import querqy.converter.generic.builder.TermQueryBuilder;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenericConverterFactoryTest {

    @Mock private ExpandedQueryBuilder<String> expandedQueryBuilder;

    @Mock private BooleanQueryBuilder<String> booleanQueryBuilder;
    @Mock private DismaxQueryBuilder<String> dismaxQueryBuilder;
    @Mock private ConstantScoreQueryBuilder<String> constantScoreQueryBuilder;
    @Mock private TermQueryBuilder<String> termQueryBuilder;
    @Mock private BoostQueryBuilder<String> boostQueryBuilder;

    @Mock private MatchAllQueryBuilder<String> matchAllQueryBuilder;
    @Mock private RawQueryBuilder<String> rawQueryBuilder;

    @Mock private QueryConfig queryConfig;
    @Mock private BoostConfig boostConfig;

    @Before
    public void setup() {
        when(queryConfig.getBoostConfig()).thenReturn(boostConfig);
    }

    @Test
    public void testThat_factoryIsCreatedWithoutException() {
        GenericConverterFactory.<String>builder()
                .expandedQueryBuilder(expandedQueryBuilder)
                .booleanQueryBuilder(booleanQueryBuilder)
                .dismaxQueryBuilder(dismaxQueryBuilder)
                .constantScoreQueryBuilder(constantScoreQueryBuilder)
                .termQueryBuilder(termQueryBuilder)
                .matchAllQueryBuilder(matchAllQueryBuilder)
                .rawQueryBuilder(rawQueryBuilder)
                .boostQueryBuilder(boostQueryBuilder)
                .build()
                .createConverter(queryConfig);
    }
}
