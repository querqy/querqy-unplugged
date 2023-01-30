package querqy.converter.solr.map;

import lombok.Builder;
import querqy.FieldConfig;
import querqy.QueryConfig;
import querqy.QueryTypeConfig;
import querqy.model.BoostedTerm;
import querqy.model.Term;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
public class TermConverter {

    // TODO: this is redundant to QueryNodesConfig?
    // TODO: fieldParamName is null here -> must not be nullable for other query type configs
    public static final QueryTypeConfig DEFAULT_CONSTANT_SCORE_QUERY_TYPE_CONFIG = QueryTypeConfig.builder()
            .typeName("constantScore")
            .queryParamName("filter")
            .build();

    public static final QueryTypeConfig DEFAULT_TERM_QUERY_TYPE_CONFIG = QueryTypeConfig.builder()
            .typeName("field")
            .queryParamName("query")
            .fieldParamName("f")
            .build();

    private final QueryConfig queryConfig;

    public List<Object> createTermQueries(final Term term) {
        final float termBoost = getTermBoost(term);

        final TermQueriesConverter converter = TermQueriesConverter.builder()
                .term(term)
                .queryConfig(queryConfig)
                .boost(termBoost)
                .build();

        return converter.convert();
    }

    private float getTermBoost(final Term term) {
        if (term instanceof BoostedTerm) {
            return ((BoostedTerm) term).getBoost();

        } else {
            return 1f;
        }
    }

    @Builder
    private static class TermQueriesConverter {
        private Term term;
        private final QueryConfig queryConfig;
        private float boost;

        public List<Object> convert() {
            return queryConfig.getFields().stream()
                    .map(this::createConstantScoreQuery)
                    .collect(Collectors.toList());
        }

        private Map<String, Object> createConstantScoreQuery(final FieldConfig fieldConfig) {
            final QueryTypeConfig constantScoreQueryTypeConfig = queryConfig.getQueryNodesConfig()
                    .getConstantScoreNodeConfig()
                    .orElse(DEFAULT_CONSTANT_SCORE_QUERY_TYPE_CONFIG);

            return Map.of(
                    constantScoreQueryTypeConfig.getTypeName(),
                    Map.of(
                            constantScoreQueryTypeConfig.getQueryParamName(), createTermNode(fieldConfig),
                            "boost", fieldConfig.getWeight() * boost
                    )
            );
        }

        private Map<String, Object> createTermNode(final FieldConfig fieldConfig) {
            final QueryTypeConfig termQueryTypeConfig = fieldConfig.getQueryTypeConfig()
                    .orElse(DEFAULT_TERM_QUERY_TYPE_CONFIG);

            final Map<String, Object> queryParams = createQueryParams(termQueryTypeConfig, fieldConfig.getFieldName());

            return Map.of(termQueryTypeConfig.getTypeName(), queryParams);
        }

        private Map<String, Object> createQueryParams(final QueryTypeConfig queryTypeConfig, final String fieldName) {
            final Map<String, Object> queryParams = new HashMap<>(2 + queryTypeConfig.getConstantParams().size());

            queryParams.put(queryTypeConfig.getQueryParamName(), term.getValue().toString());
            queryParams.put(queryTypeConfig.getFieldParamName(), fieldName);
            queryParams.putAll(queryTypeConfig.getConstantParams());

            return queryParams;

        }
    }

}
