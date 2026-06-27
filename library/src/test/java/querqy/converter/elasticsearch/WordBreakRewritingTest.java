package querqy.converter.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.DisMaxQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.junit.Test;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterConfig;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;
import querqy.domain.RewrittenQuery;
import querqy.rewriter.builder.WordBreakDefinition;
import querqy.rewriter.wordbreak.TsvDfCoocTermCorpus;

import java.io.IOException;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static querqy.QuerqyMatchers.bq;
import static querqy.QuerqyMatchers.dmq;
import static querqy.QuerqyMatchers.must;
import static querqy.QuerqyMatchers.term;

public class WordBreakRewritingTest {

    @Test
    public void testThat_compoundWordIsDecompoundedIntoAlternatives() throws IOException {

        // saturated bloom filters (all bits set) ensure verifyDecompoundCollation always confirms co-occurrence
        final TsvDfCoocTermCorpus corpus = TsvDfCoocTermCorpus.builder()
                .reader(new StringReader("arbeit\t100\tffffffffffffffff\njacke\t100\tffffffffffffffff\n"))
                .hashFunctions(1)
                .build();

        final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.of(
                ESJavaClientConverterConfig.builder()
                        .rawQueryInputType(ESJavaClientConverterConfig.RawQueryInputType.JSON)
                        .build()
        );

        final QuerqyConfig querqyConfig = QuerqyConfig.builder()
                .wordBreak(WordBreakDefinition.builder()
                        .rewriterId("word-break")
                        .termCorpus(corpus)
                        .verifyDecompoundCollation(true)
                        .decompoundMorphology("german")
                        .compoundMorphology("german")
                        .build())
                .build();

        final QueryConfig queryConfig = QueryConfig.builder()
                .field("title", 1.0f)
                .minimumShouldMatch("100%")
                .tie(0.0f)
                .build();

        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .querqyConfig(querqyConfig)
                .queryConfig(queryConfig)
                .converterFactory(converterFactory)
                .build();

        // "arbeitsjacke" decompounds via German morphology (Fugen-S) into "arbeit" + "jacke"
        final RewrittenQuery<Query> result = queryRewriting.rewriteQuery("arbeitsjacke");

        // verify the querqy model: original compound + decompound alternative (both parts must match)
        assertThat((querqy.model.Query) result.getRewrittenQuerqyQuery().getQuery().getUserQuery(),
                bq(
                        dmq(
                                term("arbeitsjacke", false),
                                bq(
                                        dmq(must(), term("arbeit", true)),
                                        dmq(must(), term("jacke", true))
                                )
                        )
                )
        );

        // verify the converted ES query: outer bool → dis_max with two alternatives
        final Query convertedQuery = result.getConvertedQuery();
        assertTrue(convertedQuery._get() instanceof BoolQuery);
        final BoolQuery outerBool = (BoolQuery) convertedQuery._get();
        assertThat(outerBool.should()).hasSize(1);
        assertTrue(outerBool.should().get(0)._get() instanceof DisMaxQuery);
        final DisMaxQuery dmq = (DisMaxQuery) outerBool.should().get(0)._get();
        // two alternatives: original compound term + decompound bool
        assertThat(dmq.queries()).hasSize(2);
        assertTrue(dmq.queries().get(1)._get() instanceof BoolQuery);
        final BoolQuery decompoundBool = (BoolQuery) dmq.queries().get(1)._get();
        assertThat(decompoundBool.must()).hasSize(2);
    }
}
