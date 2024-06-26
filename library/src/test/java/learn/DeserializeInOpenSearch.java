package learn;

import com.fasterxml.jackson.core.JsonFactory;
import jakarta.json.stream.JsonGenerator;

import java.io.IOException;
import java.io.StringWriter;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.json.jackson.JacksonJsonpGenerator;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.json.jsonb.JsonbJsonpMapper;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBase;
import org.opensearch.client.opensearch._types.query_dsl.QueryVariant;

public class DeserializeInOpenSearch {
    static String toJson(BoolQuery.Builder builder, JacksonJsonpMapper mapper) {
        JsonFactory factory = new JsonFactory();
        StringWriter jsonObjectWriter = new StringWriter();
        try {
            JsonGenerator generator =
                    new JacksonJsonpGenerator(factory.createGenerator(jsonObjectWriter));
            builder.build().serialize(generator, mapper);
            generator.close();
            return jsonObjectWriter.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("yay");
        JacksonJsonpMapper mapper = new JacksonJsonpMapper();
        System.out.println(toJson(
                new BoolQuery.Builder().filter(new Query(new MatchAllQuery.Builder().build()))
                , mapper));

    }

}
