package querqy.converter.solr.map;

import org.junit.Before;
import org.junit.Test;
import querqy.QueryConfig;
import querqy.model.BoostedTerm;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static querqy.converter.solr.map.MapConverterTestUtils.constantScoreTermMap;
import static querqy.model.convert.builder.TermBuilder.term;

public class TermConverterTest {

    private QueryConfig defaultQueryConfig;
    private TermConverter defaultConverter;

    @Before
    public void prepare() {
        defaultQueryConfig = QueryConfig.builder()
                .field("f", 10.0f)
                .build();

        defaultConverter = TermConverter.builder()
                .queryConfig(defaultQueryConfig)
                .converterConfig(MapConverterConfig.defaultConfig())
                .build();
    }

    @Test
    public void testThat_converterCreatesConstantScoreTermQuery_forGivenField() {
        assertThat(defaultConverter.createTermQueries(term("term").build()))
                .isEqualTo(
                        List.of(
                                constantScoreTermMap("f", "term", 10.0f)
                        )
                );
    }

    @Test
    public void testThat_fieldScoreIsAdjusted_forWeightedTerm() {
        final BoostedTerm boostedTerm = new BoostedTerm(null, "term", 0.5f);

        assertThat(defaultConverter.createTermQueries(boostedTerm))
                .isEqualTo(
                        List.of(constantScoreTermMap("f", "term", 5.0f))
                );
    }


    @Test
    public void testThat_converterCreatesConstantScoreTermQuery_forTwoGivenFields() {
        final QueryConfig queryConfig = QueryConfig.builder()
                .field("f1", 1.0f)
                .field("f2", 2.0f)
                .build();

        final TermConverter converter = defaultConverter.toBuilder()
                .queryConfig(queryConfig)
                .build();

        assertThat(converter.createTermQueries(term("term").build()))
                .isEqualTo(
                        List.of(
                                constantScoreTermMap("f1", "term", 1.0f),
                                constantScoreTermMap("f2", "term", 2.0f)
                        )
                );
    }


}
