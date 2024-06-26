package opensearch;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpHost;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.testcontainers.OpensearchContainer;
import org.testcontainers.utility.DockerImageName;
import querqy.QuerqyConfig;
import querqy.rewriter.builder.CommonRulesDefinition;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractOpenSearchTest {

    public static final String INDEX_NAME = "products";

    @Rule
    public OpensearchContainer<?> opensearchContainer = new OpensearchContainer<>(
            DockerImageName.parse("opensearchproject/opensearch:2.11.0"));

    protected RestClient restClient;
    protected OpenSearchClient client;

    protected abstract List<Product> createProducts();


    @Before
    public void setup() throws IOException {
        opensearchContainer.start();

        restClient = RestClient
                .builder(HttpHost.create(opensearchContainer.getHttpHostAddress()))
                .build();
        final OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        client = new OpenSearchClient(transport);

        createIndex();
    }

    @After
    public void shutDown() {
        opensearchContainer.stop();
    }


//    @Before
//    public void setup() {
//        getElasticsearchContainer().start();
//
//        createClient();
//        indexProducts(getProducts());
//    }
//
//    private void createClient() {
//        RestClient restClient = RestClient.builder(
//                new HttpHost(getElasticsearchContainer().getHost(), getElasticsearchContainer().getFirstMappedPort())).build();
//
//        ElasticsearchTransport transport = new RestClientTransport(
//                restClient, new JacksonJsonpMapper());
//
//        client = new ElasticsearchClient(transport);
//    }
//
//    @After
//    public void shutDown() {
//        getElasticsearchContainer().stop();
//    }
//

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
                            .index(INDEX_NAME)
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

    private void createIndex() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(INDEX_NAME).build();
        client.indices().create(createIndexRequest);

        createProducts().forEach(this::indexProduct);
    }

    private void indexProduct(final Product product) {

        final IndexRequest<Product> indexRequest = new IndexRequest.Builder<Product>()
                .index(INDEX_NAME)
                .id(product.getId())
                .document(product)
                .refresh(Refresh.True)
                .build();

        try {
            client.index(indexRequest);
        } catch (IOException e) {
            e.printStackTrace();
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
