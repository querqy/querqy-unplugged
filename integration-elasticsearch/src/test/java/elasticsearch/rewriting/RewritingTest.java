package elasticsearch.rewriting;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import elasticsearch.AbstractElasticsearchTest;
import elasticsearch.Elasticsearch7Container;
import org.junit.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;
import querqy.rewriter.builder.CommonRulesDefinition;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RewritingTest extends AbstractElasticsearchTest {

    private static final String INDEX_NAME = "products";

    private static final Map<String, String> RULES = Map.of(
            "synonym_repeated", "apple smartphone => \n  SYNONYM: iphone",
            "synonym_nested", "iphone => \n SYNONYM: apple smartphone",
            "synonym_weighted", "iphone => \n SYNONYM(0.5): smartphone"
    );

    private final List<Product> products = List.of(
            product("1", "iphone", "smartphone"),
            product("2", "apple", "smartphone"),
            product("3", "apple smartphone", "smartphone"),
            product("4", "apple", "case"),
            product("5", "iphone", "case"),
            product("6", "samsung", "case"),
            product("7", "samsung", "smartphone")
    );

    private final QueryConfig queryConfig = QueryConfig.builder()
            .field("name", 40.0f)
            .field("type", 20.0f)
            .minimumShouldMatch("100%")
            .tie(0.0f)
            .build();


    private final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.create();

    @Test
    public void testThat_allProductsAreFound_forRuleThatIncludesWeightedSynonym() {

        final QueryRewriting<Query> queryRewriting = queryRewriting("synonym_weighted");
        final Query query = queryRewriting.rewriteQuery("iphone").getConvertedQuery();

        final List<Product> products = search(query);

        System.out.println(toIdAndScoreMaps(products));
        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 40.0),
                idAndScoreMap("2", 10.0),
                idAndScoreMap("3", 20.0),
                idAndScoreMap("5", 40.0),
                idAndScoreMap("7", 10.0)
        );
    }

    @Test
    public void testThat_allProductsAreFound_forRuleThatLeadsToRepeatedTerm() {

        final QueryRewriting<Query> queryRewriting = queryRewriting("synonym_repeated");
        final Query query = queryRewriting.rewriteQuery("apple smartphone").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 80.0),
                idAndScoreMap("2", 60.0),
                idAndScoreMap("3", 80.0),
                idAndScoreMap("5", 80.0)
        );
    }

    @Test
    public void testThat_allProductsAreFound_forRuleThatLeadsToNestedBooleanClause() {

        final QueryRewriting<Query> queryRewriting = queryRewriting("synonym_nested");
        final Query query = queryRewriting.rewriteQuery("iphone").getConvertedQuery();

        final List<Product> products = search(query);

        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 40.0),
                idAndScoreMap("2", 30.0),
                idAndScoreMap("3", 40.0),
                idAndScoreMap("5", 40.0)
        );
    }

    private QueryRewriting<Query> queryRewriting(final String rulesKey) {
        return QueryRewriting.<Query>builder()
                .queryConfig(queryConfig)
                .querqyConfig(
                        querqyConfig(RULES.get(rulesKey))
                )
                .converterFactory(converterFactory)
                .build();
    }

    @Override
    protected List<AbstractElasticsearchTest.Product> getProducts() {
        return products;
    }

    @Override
    protected String getIndexName() {
        return INDEX_NAME;
    }

    @Override
    protected ElasticsearchContainer getElasticsearchContainer() {
        return Elasticsearch7Container.createElasticsearchContainer();
    }
}
