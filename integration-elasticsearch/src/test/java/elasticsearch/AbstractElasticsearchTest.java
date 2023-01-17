package elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
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
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
import java.util.List;

public abstract class AbstractElasticsearch7Test {

    @Rule public ElasticsearchContainer elasticsearchContainer = new Elasticsearch7Container();

    protected ElasticsearchClient client;

    protected abstract List<Product> getProducts();

    @Before
    public void setup() {
        elasticsearchContainer.start();

        createClient();
        indexProducts(getProducts());
    }

    private void createClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost(elasticsearchContainer.getHost(), elasticsearchContainer.getFirstMappedPort())).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        client = new ElasticsearchClient(transport);
    }

    @After
    public void shutDown() {
        elasticsearchContainer.stop();
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
