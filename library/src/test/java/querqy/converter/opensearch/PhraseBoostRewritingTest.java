package querqy.converter.opensearch;

import org.junit.Test;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.ConstantScoreQuery;
import org.opensearch.client.opensearch._types.query_dsl.DisMaxQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.opensearch.javaclient.OSJavaClientConverterConfig;
import querqy.converter.opensearch.javaclient.OSJavaClientConverterFactory;
import querqy.domain.RewrittenQuery;
import querqy.rewriter.builder.FieldBoost;
import querqy.rewriter.builder.PhraseBoostDefinition;
import querqy.rewriter.builder.PhraseConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class PhraseBoostRewritingTest {

    @Test
    public void testPhraseBoostQueryIsAddedAsShould() {

        final ConverterFactory<Query> converterFactory = OSJavaClientConverterFactory.of(
                OSJavaClientConverterConfig.builder()
                        .rawQueryInputType(OSJavaClientConverterConfig.RawQueryInputType.JSON)
                        .build()
        );

        final QuerqyConfig querqyConfig = QuerqyConfig.builder()
                .phraseBoost(PhraseBoostDefinition.builder()
                        .rewriterId("phrase-boost")
                        .bigram(PhraseConfig.builder()
                                .field(FieldBoost.builder().field("title").boost(1.5f).build())
                                .slop(0)
                                .build())
                        .full(PhraseConfig.builder()
                                .field(FieldBoost.builder().field("title").boost(1.0f).build())
                                .slop(1)
                                .build())
                        .tieBreaker(0.0f)
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

        // two-term query: bigram "apple iphone" + full phrase "apple iphone" boosts are generated
        final RewrittenQuery<Query> result = queryRewriting.rewriteQuery("apple iphone");
        final Query convertedQuery = result.getConvertedQuery();

        // outer query is a bool with must=userQuery and should=phraseBoosts
        assertTrue(convertedQuery._get() instanceof BoolQuery);
        final BoolQuery outerBool = (BoolQuery) convertedQuery._get();
        assertThat(outerBool.should()).isNotEmpty();

        // the phrase boost is wrapped in a constant_score (IGNORE_QUERY_SCORE mode)
        final Query shouldClause = outerBool.should().get(0);
        assertTrue(shouldClause._get() instanceof ConstantScoreQuery);
        final ConstantScoreQuery constantScore = (ConstantScoreQuery) shouldClause._get();

        // inside the constant_score: a bool wrapping a dis_max of phrase queries
        final Query filterQuery = constantScore.filter();
        assertTrue(filterQuery._get() instanceof BoolQuery);
        final BoolQuery phraseBool = (BoolQuery) filterQuery._get();
        assertThat(phraseBool.should()).hasSize(1);

        final Query dmqClause = phraseBool.should().get(0);
        assertTrue(dmqClause._get() instanceof DisMaxQuery);
        final DisMaxQuery disMaxQuery = (DisMaxQuery) dmqClause._get();

        // bigram "apple iphone" on title^1.5  +  full "apple iphone" on title^1.0
        assertThat(disMaxQuery.queries()).hasSize(2);

        final Query bigramQuery = disMaxQuery.queries().get(0);
        assertTrue(bigramQuery._get() instanceof MatchPhraseQuery);
        final MatchPhraseQuery bigramPhrase = (MatchPhraseQuery) bigramQuery._get();
        assertThat(bigramPhrase.field()).isEqualTo("title");
        assertThat(bigramPhrase.query()).isEqualTo("apple iphone");
        assertThat(bigramPhrase.boost()).isEqualTo(1.5f);
        assertThat(bigramPhrase.slop()).isNull(); // slop=0 means not set

        final Query fullPhraseQuery = disMaxQuery.queries().get(1);
        assertTrue(fullPhraseQuery._get() instanceof MatchPhraseQuery);
        final MatchPhraseQuery fullPhrase = (MatchPhraseQuery) fullPhraseQuery._get();
        assertThat(fullPhrase.field()).isEqualTo("title");
        assertThat(fullPhrase.query()).isEqualTo("apple iphone");
        assertThat(fullPhrase.boost()).isEqualTo(1.0f);
        assertThat(fullPhrase.slop()).isEqualTo(1);
    }
}
