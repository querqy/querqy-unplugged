package elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import querqy.QueryConfig;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterFactory;
import querqy.model.convert.builder.BooleanQueryBuilder;
import querqy.model.convert.model.Occur;

import java.io.IOException;
import java.util.List;

import static querqy.model.convert.builder.BooleanQueryBuilder.bq;
import static querqy.model.convert.builder.DisjunctionMaxQueryBuilder.dmq;
import static querqy.model.convert.builder.ExpandedQueryBuilder.expanded;
import static querqy.model.convert.builder.TermBuilder.term;


public class MyTests {

    @Rule public ElasticsearchContainer elasticsearchContainer = new Elasticsearch7Container();

    private final QueryConfig queryConfig = QueryConfig.builder()
            .field("name", 40.0f)
            .field("type", 20.0f)
            .minimumShouldMatch("100%")
            .tie(0.0f)
            .build();

    private final List<Product> products = List.of(
            product("1", "iphone", "smartphone"),
            product("2", "apple", "smartphone"),
            product("3", "apple smartphone", "smartphone"),
            product("4", "apple", "case"),
            product("5", "samsung", "case"),
            product("6", "samsung", "smartphone")
    );

    private ElasticsearchClient client;

    @Before
    public void setup() {
        elasticsearchContainer.start();

        createClient();
        indexProducts(products);
    }

    @After
    public void shutDown() {
        elasticsearchContainer.stop();
    }

    @Test
    public void test() throws IOException {

        final BooleanQueryBuilder booleanQuery = bq(
                dmq(
                        term("iphone"),
                        bq("apple", "smartphone").setOccur(Occur.MUST)
                )
        ).setOccur(Occur.SHOULD);


        final Query rewrittenQuery = ESJavaClientConverterFactory.create()
                .createConverter(queryConfig)
                .convert(expanded(booleanQuery).build());


        final SearchResponse<Product> search = client.search(s -> s
                        .index("products")
                        .query(rewrittenQuery),
                Product.class);

        for (Hit<Product> hit : search.hits().hits()) {
            System.out.println(hit.source() + " " + hit.score());
        }
    }

    private void createClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost(elasticsearchContainer.getHost(), elasticsearchContainer.getFirstMappedPort())).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        client = new ElasticsearchClient(transport);
    }

    private void indexProducts(final List<Product> products) {
        products.forEach(this::indexProduct);
    }

    private void indexProduct(final Product product) {
        try {
            client.index(i -> i
                    .index("products")
                    .id(product.getId())
                    .document(product)
                    .refresh(Refresh.True)
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Product product(final String id, final String name, final String type) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setType(type);

        return product;
    }

    @Data
    @NoArgsConstructor
    private static class Product {
        private String id;
        private String name;
        private String type;
    }




}
