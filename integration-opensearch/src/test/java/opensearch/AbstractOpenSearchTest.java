package opensearch;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.hc.core5.http.HttpHost;
import org.junit.After;
import org.junit.Before;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.opensearch.testcontainers.OpenSearchContainer;
import querqy.QuerqyConfig;
import querqy.QueryRewriting;
import querqy.rewriter.builder.CommonRulesDefinition;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractOpenSearchTest {

    protected OpenSearchClient client;

    protected abstract OpenSearchContainer<?> getOpenSearchContainer();
    protected abstract List<Product> getProducts();
    protected abstract String getIndexName();

    @Before
    public void setup() {
        getOpenSearchContainer().start();

        createClient();
        indexProducts(getProducts());
    }

    private void createClient() {
        final HttpHost host = new HttpHost("http",
                getOpenSearchContainer().getHost(), getOpenSearchContainer().getMappedPort(9200));

        final OpenSearchTransport transport = ApacheHttpClient5TransportBuilder.builder(host)
                .setMapper(new JacksonJsonpMapper())
                .build();

        client = new OpenSearchClient(transport);
    }

    @After
    public void shutDown() {
        getOpenSearchContainer().stop();
    }


    protected Map<String, Object> idAndScoreMap(final String id, final Double score) {
        return Map.of("id", id,"_score", score);
    }

    protected List<Map<String, Object>> toIdAndScoreMaps(final List<Product> products) {
        return products.stream()
                .map(product -> idAndScoreMap(product.getId(), product.get_score()))
                .collect(Collectors.toList());
    }

    protected List<String> toIdList(final List<Product> products) {
        return products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());
    }

    protected List<Product> search(final Query query) {
        try {
            final SearchResponse<Product> response = client.search(s -> s
                            .index(getIndexName())
                            .query(query),
                    Product.class);

            return matches(response);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    protected List<Product> matches(final SearchResponse<Product> response) {
        return response.hits().hits().stream()
                .map(hit -> {
                    final Product product = hit.source();
                    product.set_score(hit.score());
                    return product;
                })
                .collect(Collectors.toList());
    }

    protected QuerqyConfig querqyConfig(final String rules) {
        return QuerqyConfig.builder()
                .commonRules(
                        CommonRulesDefinition.builder()
                                .rewriterId("id1")
                                .rules(rules)
                                .build()
                )
                .build();
    }

    private void indexProducts(final List<Product> products) {
        products.forEach(this::indexProduct);
    }

    private void indexProduct(final Product product) {
        try {
            client.index(i -> i
                    .index(getIndexName())
                    .id(product.getId())
                    .document(product)
                    .refresh(Refresh.True)
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Product product(final String id, final String name, final String type) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setType(type);

        return product;
    }

    @Data
    @NoArgsConstructor
    public static class Product {
        private String id;
        private String name;
        private String type;

        private Double _score;
    }
}
