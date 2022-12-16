package querqy.converter;

import org.junit.Test;
import querqy.QueryConfig;
import querqy.model.ExpandedQuery;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;

public class TermListConverterTest {

    @Test
    public void testThat_converterCollectsTerms_forGivenQuery() {
        final ExpandedQuery expandedQuery = expanded(bq("iphone", "8")).build();
        final List<String> terms =
                TermListConverterFactory.create().createConverter(expandedQuery).convert();

        assertThat(terms).isEqualTo(List.of("iphone", "8"));
    }
}
