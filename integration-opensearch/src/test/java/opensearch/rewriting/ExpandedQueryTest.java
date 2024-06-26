package opensearch.rewriting;

//import elasticsearch.AbstractElasticsearchTest;
//import elasticsearch.Elasticsearch7Container;
import opensearch.AbstractOpenSearchTest;
import org.junit.Test;
//import org.testcontainers.elasticsearch.ElasticsearchContainer;
//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import querqy.BoostConfig;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterConfig;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;
import querqy.parser.FieldAwareWhiteSpaceQuerqyParser;
import querqy.rewriter.builder.CommonRulesDefinition;
import querqy.rewriter.builder.ReplaceRulesDefinition;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpandedQueryTest extends AbstractOpenSearchTest {

    private static final String INDEX_NAME = "products";

    private static final Map<String, String> RULES = Map.of(
            "filter", "apple => \n  FILTER: smartphone",
            "filter_with_field", "apple => \n  FILTER: type:smartphone",
            "raw_filter", "apple => \n FILTER: * type:smartphone",
            "raw_filter_json", "apple => \n FILTER: * {\"term\":{\"type\":\"smartphone\"}}",
            "boost", "apple => \n UP(100): smartphone",
            "boost_additive", "apple => \n UP(10): smartphone",
            "boost_multiplicative", "apple => \n UP(1.5): smartphone",
            "down_boost", "apple => \n DOWN(10): case"
    );

    private final List<Product> products = List.of(
            product("1", "apple", "smartphone"),
            product("2", "apple smartphone", "case"),
            product("3", "apple", "case")
    );

    private final QueryConfig queryConfig = QueryConfig.builder()
            .field("name", 40.0f)
            .field("type", 20.0f)
            .minimumShouldMatch("100%")
            .tie(0.0f)
            .build();


//    private final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.create();
    private final ConverterFactory<Query> converterFactory = null;

//    private final ConverterFactory<Query> converterFactoryJson = ESJavaClientConverterFactory.of(
//            ESJavaClientConverterConfig.builder()
//                    .rawQueryInputType(ESJavaClientConverterConfig.RawQueryInputType.JSON)
//                    .build()
//    );
    private final ConverterFactory<Query> converterFactoryJson = null;

    @Test
    public void testThat_allDocumentsAreReturned_forGivenMatchAllQuery() {
        final QueryRewriting<Query> queryRewriting = queryRewriting();
        final Query query = queryRewriting.rewriteQuery("*").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(products).hasSize(3);
    }

    @Test
    public void testThat_documentsAreFiltered_forGivenFilterQuery() {
        final QueryRewriting<Query> queryRewriting = queryRewriting("filter");
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdList(products)).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    public void testThat_rewriterChainIsApplied_forGivenCommonRulesAndReplaceRewriter() {
        final QuerqyConfig querqyConfig = QuerqyConfig.builder()
                .replaceRules(
                        ReplaceRulesDefinition.builder()
                                .rewriterId("id1")
                                .rules("aple => apple")
                                .build()
                )
                .commonRules(
                        CommonRulesDefinition.builder()
                                .rewriterId("id2")
                                .rules(RULES.get("filter"))
                                .querqyParserFactory(FieldAwareWhiteSpaceQuerqyParser::new)
                                .build()
                )
                .build();

        final QueryRewriting<Query> queryRewriting = queryRewriting(querqyConfig, queryConfig);
        final Query query = queryRewriting.rewriteQuery("aple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdList(products)).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    public void testThat_documentsAreFiltered_forGivenRawFilterJsonQuery() {
        final QueryRewriting<Query> queryRewriting = queryRewriting("raw_filter_json", converterFactoryJson);
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdList(products)).containsExactlyInAnyOrder("1");
    }

    @Test
    public void testThat_documentsAreFiltered_forGivenRawFilterQuery() {
        final QueryRewriting<Query> queryRewriting = queryRewriting("raw_filter");
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdList(products)).containsExactlyInAnyOrder("1");
    }

    @Test
    public void testThat_documentsAreFiltered_forGivenFieldFilterQuery() {
        final QuerqyConfig querqyConfig = QuerqyConfig.builder()
                .commonRules(
                        CommonRulesDefinition.builder()
                                .rewriterId("id1")
                                .rules(RULES.get("filter_with_field"))
                                .querqyParserFactory(FieldAwareWhiteSpaceQuerqyParser::new)
                                .build()
                )
                .build();

        final QueryRewriting<Query> queryRewriting = queryRewriting(querqyConfig, queryConfig);
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdList(products)).containsExactlyInAnyOrder("1");
    }

    @Test
    public void testThat_documentsAreBoosted_forGivenConstantScoreBoostQuery() {
        final QueryRewriting<Query> queryRewriting = queryRewriting("boost");
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 140.0),
                idAndScoreMap("2", 140.0),
                idAndScoreMap("3", 40.0)
        );
    }

    @Test
    public void testThat_documentsAreBoosted_forGivenAdditiveBoostQuery() {
        final QueryRewriting<Query> queryRewriting = queryRewriting(
                "boost_additive",
                queryConfig.toBuilder()
                        .boostConfig(BoostConfig.builder()
                                .queryScoreConfig(BoostConfig.QueryScoreConfig.ADD_TO_BOOST_PARAM)
                                .build())
                        .build()
        );
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("2", 90.0),
                idAndScoreMap("1", 70.0),
                idAndScoreMap("3", 40.0)
        );
    }

    @Test
    public void testThat_documentsAreBoosted_forGivenMultiplicativeBoostQuery() {
        final QueryRewriting<Query> queryRewriting = queryRewriting(
                "boost_multiplicative",
                queryConfig.toBuilder()
                        .boostConfig(BoostConfig.builder()
                                .queryScoreConfig(BoostConfig.QueryScoreConfig.MULTIPLY_WITH_BOOST_PARAM)
                                .build())
                        .build()
        );
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("2", 100.0),
                idAndScoreMap("1", 70.0),
                idAndScoreMap("3", 40.0)
        );
    }

    @Test
    public void testThat_documentsArePunished_forGivenDownBoostQuery() {
        final QueryRewriting<Query> queryRewriting = queryRewriting(
                "down_boost",
                queryConfig.toBuilder()
                        .boostConfig(BoostConfig.builder()
                                .queryScoreConfig(BoostConfig.QueryScoreConfig.ADD_TO_BOOST_PARAM)
                                .build())
                        .build()
        );
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 50.0),
                idAndScoreMap("2", 40.0),
                idAndScoreMap("3", 40.0)
        );
    }

    private QueryRewriting<Query> queryRewriting() {
        return queryRewriting(QuerqyConfig.empty(), queryConfig);
    }

    private QueryRewriting<Query> queryRewriting(final String rulesKey) {
        return queryRewriting(querqyConfig(RULES.get(rulesKey)), queryConfig);
    }

    private QueryRewriting<Query> queryRewriting(final String rulesKey, final ConverterFactory<Query> converterFactory) {
        return queryRewriting(querqyConfig(RULES.get(rulesKey)), queryConfig, converterFactory);
    }

    private QueryRewriting<Query> queryRewriting(final String rulesKey, final QueryConfig queryConfig) {
        return queryRewriting(querqyConfig(RULES.get(rulesKey)), queryConfig);
    }

    private QueryRewriting<Query> queryRewriting(final QuerqyConfig querqyConfig, final QueryConfig queryConfig) {
        return queryRewriting(querqyConfig, queryConfig, converterFactory);
    }

    private QueryRewriting<Query> queryRewriting(final QuerqyConfig querqyConfig, final QueryConfig queryConfig, final ConverterFactory<Query> converterFactory) {
        return QueryRewriting.<Query>builder()
                .queryConfig(queryConfig)
                .querqyConfig(querqyConfig)
                .converterFactory(converterFactory)
                .build();
    }

    @Override
    protected List<Product> createProducts() {
        return products;
    }
}
