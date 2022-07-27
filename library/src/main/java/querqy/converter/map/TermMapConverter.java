package querqy.converter.map;

import lombok.Builder;
import querqy.QueryConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public class TermMapConverter {

    private final QueryConfig queryConfig;
    private final String value;

    public List<Object> createTermQueries() {
        return queryConfig.getFields().entrySet().stream()
                .map(entry -> createTermQuery(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createTermQuery(final String field, final Float weight) {
        return Map.of(
                queryConfig.getScoringNodeName(),
                Map.of(
                        "filter", createMatchingNode(field),
                        "boost", weight
                )
        );
    }

    private Map<String, Object> createMatchingNode(final String field) {
        return Map.of(
                queryConfig.getMatchingNodeName(),
                Map.of(
                        "f", field,
                        "query", value
                )
        );
    }
}
