package querqy.converter.solr.map;

import org.junit.Test;
import querqy.QueryConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static querqy.converter.solr.map.MapConverterTestUtils.termMap;
import static querqy.model.convert.builder.TermBuilder.term;

public class TermConverterTest {

    @Test
    public void testThat_converterCreatesTermQueries_forAllGivenFields() {
        final QueryConfig queryConfig = QueryConfig.builder()
                .field("a", 1.0f)
                .field("b", 2.0f)
                .build();

        final TermConverter converter = TermConverter.builder()
                .queryConfig(queryConfig)
                .converterConfig(MapConverterConfig.defaultConfig())
                .build();

        assertThat(converter.createTermQueries(term("t").build())).hasSize(2);
    }

    @Test
    public void testThat_converterCreatesConstantScoreTermQuery_forGivenField() {
        final QueryConfig queryConfig = QueryConfig.builder()
                .field("f", 1.0f)
                .build();

        final TermConverter converter = TermConverter.builder()
                .queryConfig(queryConfig)
                .converterConfig(MapConverterConfig.defaultConfig())
                .build();

        assertThat(converter.createTermQueries(term("term").build()))
                .isEqualTo(
                        List.of(
                                termMap("f", "term", 1.0f)
                        )
                );
    }

    @Test
    public void testThat_converterCreatesConstantScoreTermQuery_forTwoGivenFields() {
        final QueryConfig queryConfig = QueryConfig.builder()
                .field("f1", 1.0f)
                .field("f2", 2.0f)
                .build();

        final TermConverter converter = TermConverter.builder()
                .queryConfig(queryConfig)
                .converterConfig(MapConverterConfig.defaultConfig())
                .build();

        assertThat(converter.createTermQueries(term("term").build()))
                .isEqualTo(
                        List.of(
                                termMap("f1", "term", 1.0f),
                                termMap("f2", "term", 2.0f)
                        )
                );
    }
}
