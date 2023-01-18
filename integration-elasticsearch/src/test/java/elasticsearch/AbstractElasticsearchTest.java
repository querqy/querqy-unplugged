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
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
import java.util.List;

public abstract class AbstractElasticsearchTest {

    protected ElasticsearchClient client;

    protected abstract ElasticsearchContainer getElasticsearchContainer();
    protected abstract List<Product> getProducts();
    protected abstract String getIndexName();

    @Before
    public void setup() {
        getElasticsearchContainer().start();

        createClient();
        indexProducts(getProducts());
    }

    private void createClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost(getElasticsearchContainer().getHost(), getElasticsearchContainer().getFirstMappedPort())).build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        client = new ElasticsearchClient(transport);
    }

    @After
    public void shutDown() {
        getElasticsearchContainer().stop();
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
