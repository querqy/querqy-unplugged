package elasticsearch.rewriting;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import elasticsearch.AbstractElasticsearchTest;
import elasticsearch.Elasticsearch7Container;
import org.junit.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import querqy.QuerqyConfig;
import querqy.QueryConfig;
import querqy.QueryRewriting;
import querqy.converter.ConverterFactory;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;
import querqy.parser.FieldAwareWhiteSpaceQuerqyParser;
import querqy.rewriter.builder.CommonRulesDefinition;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpandedQueryTest extends AbstractElasticsearchTest {

    private static final String INDEX_NAME = "products";

    private static final Map<String, String> RULES = Map.of(
            "filter", "apple => \n  FILTER: smartphone",
            "filter_with_field", "apple => \n  FILTER: type:smartphone",
            "raw_filter", "apple => \n FILTER: * {\"term\":{\"type\":\"smartphone\"}}"
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


    private final ConverterFactory<Query> converterFactory = ESJavaClientConverterFactory.create();

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

        final QueryRewriting<Query> queryRewriting = queryRewriting(querqyConfig);
        final Query query = queryRewriting.rewriteQuery("apple").getConvertedQuery();

        final List<Product> products = search(query);
        assertThat(toIdList(products)).containsExactlyInAnyOrder("1");
    }

    private QueryRewriting<Query> queryRewriting() {
        return queryRewriting(QuerqyConfig.empty());
    }

    private QueryRewriting<Query> queryRewriting(final String rulesKey) {
        return queryRewriting(querqyConfig(RULES.get(rulesKey)));
    }

    private QueryRewriting<Query> queryRewriting(final QuerqyConfig querqyConfig) {
        return QueryRewriting.<Query>builder()
                .queryConfig(queryConfig)
                .querqyConfig(querqyConfig)
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
