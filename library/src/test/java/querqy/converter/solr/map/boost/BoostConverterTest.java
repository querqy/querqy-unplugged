package querqy.converter.solr.map.boost;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import querqy.converter.solr.map.QuerqyQueryConverter;
import querqy.model.BoostQuery;
import querqy.model.QuerqyQuery;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BoostConverterTest {

    @Mock private QuerqyQueryConverter querqyQueryConverter;
    @Mock private QuerqyQuery<?> querqyQuery;

    private BoostConverter boostConverter;

    @Before
    public void prepare() {
        when(querqyQueryConverter.convert(any())).thenReturn("query");

        boostConverter = BoostConverter.builder()
                .querqyQueryConverter(querqyQueryConverter)
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testThat_boostQueryFunctionIsSimpleReference_forBoostQueryWithBoostEqualsOne() {
        final BoostQuery boostQuery = new BoostQuery(querqyQuery, 1.0f);

        final ConvertedBoostQueries convertedBoostQueries = boostConverter
                .convertBoostQueries(List.of(boostQuery), List.of());

        assertThat(convertedBoostQueries.getBoostFunctionQueries()).hasSize(1);
        assertThat(convertedBoostQueries.getReferencedQueries()).hasSize(1);

        assertThat(convertedBoostQueries.getBoostFunctionQueries().get(0)).isInstanceOf(Map.class);
        final Map<String, Object> functionQuery = (Map<String, Object>) convertedBoostQueries.getBoostFunctionQueries().get(0);

        assertThat(functionQuery).containsKey("param");
        assertThat(convertedBoostQueries.getReferencedQueries()).containsKey((String) functionQuery.get("param"));
    }

    @Test
    public void testThat_boostQueryFunctionContainsFunctionString_forBoostQueryWithBoostGreaterThanOne() {
        final BoostQuery boostQuery = new BoostQuery(querqyQuery, 2.0f);

        final ConvertedBoostQueries convertedBoostQueries = boostConverter
                .convertBoostQueries(List.of(boostQuery), List.of());

        assertThat(convertedBoostQueries.getBoostFunctionQueries()).hasSize(1);
        assertThat(convertedBoostQueries.getReferencedQueries()).hasSize(1);

        assertThat(convertedBoostQueries.getBoostFunctionQueries().get(0)).isInstanceOf(String.class);
    }
}
