package opensearch.rewriting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import opensearch.AbstractOpenSearchTest;
import org.junit.Test;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class TestTest extends AbstractOpenSearchTest {

    @Test
    public void testJavaClient() throws IOException {
        System.out.println("Test");

        SearchResponse<Product> searchResponse = client.search(
                s -> s.index(INDEX_NAME).query(
                        q -> q.match(
                                m -> m.field("name").query(fv -> fv.stringValue("smartphone")))
                ),

                Product.class);
        for (int i = 0; i< searchResponse.hits().hits().size(); i++) {
            System.out.println(searchResponse.hits().hits().get(i).source());
        }


    }

    @Test
    public void testHighLevelClient() throws IOException {
        System.out.println("Test");

        final Request searchRequest = new Request("GET", INDEX_NAME + "/_search");
        searchRequest.setJsonEntity("{\"query\": { \"match_all\": {} }}");


        Response response = restClient.performRequest(searchRequest);

        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"), 8);
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        Object jsonObject = objectMapper.readValue(sb.toString(), Object.class);
        String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        System.out.println(prettyJson);

        HttpSearchResponse resp = objectMapper.readValue(sb.toString(), HttpSearchResponse.class);

        System.out.println(resp);

        for (Hit hit : resp.getHits().getHits()) {
            System.out.println(hit.product);
        }


    }

    @Data
    @NoArgsConstructor
    private static class HttpSearchResponse {
        private Hits hits;
    }

    @Data
    @NoArgsConstructor
    private static class Hits {
        private List<Hit> hits;
    }

    @Data
    @NoArgsConstructor
    private static class Hit {
        @JsonProperty("_score") private Double score;
        @JsonProperty("_source") private Product product;
    }

    @Override
    protected List<Product> createProducts() {
        return List.of(
                product("1", "apple", "smartphone"),
                product("2", "apple smartphone", "case"),
                product("3", "apple", "case")
        );
    }
}
