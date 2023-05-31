package elasticsearch.rewriting;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import elasticsearch.AbstractElasticsearchTest;
import elasticsearch.Elasticsearch7Container;
import org.junit.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import querqy.QueryConfig;
import querqy.QueryExpansionConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryExpansionTest extends AbstractElasticsearchTest {

    private static final String INDEX_NAME = "products";

    private static final Map<String, String> RULES = Map.of(
            "synonym_repeated", "apple smartphone => \n  SYNONYM: iphone",
            "synonym_nested", "iphone => \n SYNONYM: apple smartphone",
            "synonym_weighted", "iphone => \n SYNONYM(0.5): smartphone"
    );

    private final List<Product> products = List.of(
            product("1", "apple", "smartphone"),
            product("2", "huawei", "case"),
            product("3", "samsung", "smartphone")
    );

    private final QueryConfig queryConfig = QueryConfig.builder()
            .field("name", 40.0f)
            .field("type", 20.0f)
            .minimumShouldMatch("100%")
            .tie(0.0f)
            .build();


    private final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.create();

    @Test
    public void testThat_resultsAreExpanded_forAlternativeMatchingStringQuery() {
        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .queryConfig(queryConfig)
                .queryExpansionConfig(
                        QueryExpansionConfig.<Query>builder()
                                .addAlternativeMatchingStringQuery("name:huawei", 50f)
                        .build()
                )
                .converterFactory(converterFactory)
                .build();
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);

        System.out.println(toIdAndScoreMaps(products));
        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 40.0),
                idAndScoreMap("2", 50.0)
        );
    }

    @Test
    public void testThat_resultsAreExpanded_forBoostStringQuery() {
        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .queryConfig(queryConfig)
                .queryExpansionConfig(
                        QueryExpansionConfig.<Query>builder()
                                .addBoostUpStringQuery("name:apple", 50f)
                        .build()
                )
                .converterFactory(converterFactory)
                .build();
        final Query query = queryRewriting.rewriteQuery("smartphone").getConvertedQuery();

        final List<Product> products = search(query);

        System.out.println(toIdAndScoreMaps(products));
        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 70.0),
                idAndScoreMap("3", 20.0)
        );
    }

    @Test
    public void testThat_resultsAreExpanded_forAlternativeMatchingQuery() {
        final QueryRewriting<Query> queryRewriting = QueryRewriting.<Query>builder()
                .queryConfig(queryConfig)
                .queryExpansionConfig(
                        QueryExpansionConfig.<Query>builder()
                                .addAlternativeMatchingQuery(
                                        new Query(
                                                new TermQuery.Builder()
                                                        .field("name")
                                                        .value("huawei")
                                                        .build()
                                        ),
                                        50f)
                        .build()
                )
                .converterFactory(converterFactory)
                .build();
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);

        System.out.println(toIdAndScoreMaps(products));
        assertThat(toIdAndScoreMaps(products)).containsExactlyInAnyOrder(
                idAndScoreMap("1", 40.0),
                idAndScoreMap("2", 50.0)
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
    protected ElasticsearchContainer getElasticsearchContainer() {
        return Elasticsearch7Container.createElasticsearchContainer();
    }

}
