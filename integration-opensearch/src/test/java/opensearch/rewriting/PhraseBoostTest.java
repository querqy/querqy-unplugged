package opensearch.rewriting;

import opensearch.AbstractOpenSearchTest;
import opensearch.OpenSearch2Container;
import org.junit.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.testcontainers.OpenSearchContainer;
import querqy.BoostConfig;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.opensearch.javaclient.OSJavaClientConverterFactory;
import querqy.rewriter.builder.FieldBoost;
import querqy.rewriter.builder.PhraseBoostDefinition;
import querqy.rewriter.builder.PhraseConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PhraseBoostTest extends AbstractOpenSearchTest {

    private static final String INDEX_NAME = "phrase-boost-products";

    // All docs contain both query terms so they all match with minimumShouldMatch=100%.
    // Doc 1: adjacent phrase → matches slop=0 and slop=1.
    // Doc 2, 3: one word between the terms (distance=1) → match only slop=1, not slop=0.
    private final List<Product> products = List.of(
            product("1", "apple smartphone", "device"),
            product("2", "apple great smartphone", "device"),
            product("3", "apple new smartphone", "device")
    );

    private final ConverterFactory<Query> converterFactory = OSJavaClientConverterFactory.create();

    @Test
    public void testThat_exactPhraseMatchScoresHigher_thanNonAdjacentTerms() {
        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .queryConfig(QueryConfig.builder()
                        .field("name", 40.0f)
                        .minimumShouldMatch("100%")
                        .tie(0.0f)
                        .boostConfig(BoostConfig.builder()
                                .queryScoreConfig(BoostConfig.QueryScoreConfig.IGNORE_QUERY_SCORE)
                                .build())
                        .build())
                .querqyConfig(QuerqyConfig.builder()
                        .phraseBoost(PhraseBoostDefinition.builder()
                                .rewriterId("phrase-boost")
                                .bigram(PhraseConfig.builder()
                                        .field(FieldBoost.builder().field("name").boost(100.0f).build())
                                        .slop(0)
                                        .build())
                                .build())
                        .build())
                .converterFactory(converterFactory)
                .build();

        final Query query = queryRewriting.rewriteQuery("apple smartphone").getConvertedQuery();

        final List<Product> results = search(query);

        // doc 1 scores 81.0: base (40+40) + phrase boost (1.0, constantScore)
        // docs 2 and 3 score 80.0: base only, no adjacent phrase match at slop=0
        assertThat(toIdAndScoreMaps(results)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 81.0),
                idAndScoreMap("2", 80.0),
                idAndScoreMap("3", 80.0)
        );
    }

    @Test
    public void testThat_slopAllowsNearbyPhraseToScore() {
        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .queryConfig(QueryConfig.builder()
                        .field("name", 40.0f)
                        .minimumShouldMatch("100%")
                        .tie(0.0f)
                        .boostConfig(BoostConfig.builder()
                                .queryScoreConfig(BoostConfig.QueryScoreConfig.IGNORE_QUERY_SCORE)
                                .build())
                        .build())
                .querqyConfig(QuerqyConfig.builder()
                        .phraseBoost(PhraseBoostDefinition.builder()
                                .rewriterId("phrase-boost")
                                .bigram(PhraseConfig.builder()
                                        .field(FieldBoost.builder().field("name").boost(100.0f).build())
                                        .slop(1)
                                        .build())
                                .build())
                        .build())
                .converterFactory(converterFactory)
                .build();

        final Query query = queryRewriting.rewriteQuery("apple smartphone").getConvertedQuery();

        final List<Product> results = search(query);

        // All docs match "apple smartphone" with slop=1 (each has at most 1 word between the terms),
        // so all three receive the phrase boost → 80.0 base + 1.0 constantScore boost = 81.0
        assertThat(toIdAndScoreMaps(results)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 81.0),
                idAndScoreMap("2", 81.0),
                idAndScoreMap("3", 81.0)
        );
    }

    @Override
    protected List<Product> getProducts() {
        return products;
    }

    @Override
    protected String getIndexName() {
        return INDEX_NAME;
    }

    @Override
    protected OpenSearchContainer<?> getOpenSearchContainer() {
        return OpenSearch2Container.createOpenSearchContainer();
    }
}
