package querqy.converter.generic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.QueryConfig;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.builder.DismaxQueryBuilder;
import querqy.converter.generic.builder.ExpandedQueryBuilder;
import querqy.converter.generic.builder.MatchAllQueryBuilder;
import querqy.converter.generic.builder.RawQueryBuilder;
import querqy.converter.generic.builder.TermQueryBuilder;

@RunWith(MockitoJUnitRunner.class)
public class GenericConverterFactoryTest {

    @Mock private ExpandedQueryBuilder<String> expandedQueryBuilder;

    @Mock private BooleanQueryBuilder<String> booleanQueryBuilder;
    @Mock private DismaxQueryBuilder<String> dismaxQueryBuilder;
    @Mock private TermQueryBuilder<String> termQueryBuilder;

    @Mock private MatchAllQueryBuilder<String> matchAllQueryBuilder;
    @Mock private RawQueryBuilder<String> rawQueryBuilder;

    @Mock private QueryConfig queryConfig;

    @Test
    public void testThat_factoryIsCreatedWithoutException() {
        GenericConverterFactory.<String>builder()
                .expandedQueryBuilder(expandedQueryBuilder)
                .booleanQueryBuilder(booleanQueryBuilder)
                .dismaxQueryBuilder(dismaxQueryBuilder)
                .termQueryBuilder(termQueryBuilder)
                .matchAllQueryBuilder(matchAllQueryBuilder)
                .rawQueryBuilder(rawQueryBuilder)
                .build()
                .createConverter(queryConfig);
    }
}
