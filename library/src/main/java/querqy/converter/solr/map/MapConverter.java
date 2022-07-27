package querqy.converter.solr.map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import querqy.QueryConfig;
import querqy.converter.Converter;
import querqy.model.ExpandedQuery;
import querqy.model.QuerqyQuery;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PROTECTED)
public class MapConverter implements Converter<Map<String, Object>> {

    private final QueryConfig queryConfig;
    private final QuerqyQuery<?> userQuery;
    private final FilterMapConverter filterMapConverter;

    @Builder
    public static MapConverter build(final QueryConfig queryConfig, final ExpandedQuery expandedQuery) {
        final FilterMapConverter filterMapConverter = FilterMapConverter.builder()
                .queryConfig(queryConfig)
                .filterQueries(expandedQuery.getFilterQueries())
                .build();
        return MapConverter.of(queryConfig, expandedQuery.getUserQuery(), filterMapConverter);
    }

    @Override
    public Map<String, Object> convert() {
        final Map<String, Object> convertedUserQuery = convertUserQuery();

        if (filterMapConverter.hasFilters() /* || hasBoosts() */ ) {
            return expandConvertedUserQuery(convertedUserQuery);

        } else {
            return convertedUserQuery;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertUserQuery() {
        final Object query =  QuerqyQueryMapConverter.builder()
                .queryConfig(queryConfig)
                .node(userQuery)
                .parseAsUserQuery(true)
                .build()
                .convert();

        if (query instanceof Map) {
            return (Map<String, Object>) query;

        } else if (query instanceof String){
            return Map.of("bool", Map.of("must", (String) query));

        } else {
            throw new IllegalArgumentException("Converted user query must be of type Map or String");
        }
    }

    private Map<String, Object> expandConvertedUserQuery(final Object convertedUserQuery) {
        final Map<String, Object> expandedQueryNode = new HashMap<>(3);
        expandedQueryNode.put("must", convertedUserQuery);

        if (filterMapConverter.hasFilters()) {
            expandedQueryNode.put("filter", filterMapConverter.convertFilterQueries());
        }

        // TODO: Include boosts
//        if (filterAndBoostMapConverter.hasBoosts()) {
//            // expandedQueryNode.put("should", filterAndBoostMapConverter.convertBoostQueries());
//        }

        return Map.of("bool", expandedQueryNode);
    }

//    public String convertToJson() {
//        try {
//            final ObjectMapper objectMapper = createObjectMapper();
//            final Map<String, Object> convertedQuery = convert();
//            return objectMapper.writeValueAsString(convertedQuery);
//
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper;
    }

}
