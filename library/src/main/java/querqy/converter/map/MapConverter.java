package querqy.converter.map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Builder;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.model.AbstractNodeVisitor;
import querqy.model.ExpandedQuery;

import java.util.Map;

@Builder
public class MapConverter implements Converter<Map<String, Object>> {

    private final QueryConfig queryConfig;
    private final ExpandedQuery expandedQuery;

    @Override
    public Map<String, Object> convert() {
        final Map<String, Object> convertedUserQuery = NodeMapConverter.builder()
                .queryConfig(queryConfig)
                .node(expandedQuery.getUserQuery())
                .parseAsUserQuery(true)
                .build()
                .convert();

        return convertedUserQuery;
    }

    private Map<String, Object> addFiltersAndBoostsToUserQuery(final Map<String, Object> convertedUserQuery) {
        // TODO

        return convertedUserQuery;
    }

    @Override
    public String convertToJson() {
        try {
            final ObjectMapper objectMapper = createObjectMapper();
            final Map<String, Object> convertedQuery = convert();
            return objectMapper.writeValueAsString(convertedQuery);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }






    private ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper;
    }



}
