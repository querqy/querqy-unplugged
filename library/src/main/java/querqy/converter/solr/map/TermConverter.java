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
            return Map.of(
                    queryConfig.getConstantScoreNodeName(),
                    Map.of(
                            "filter", createTermNode(fieldConfig),
                            "boost", fieldConfig.getWeight() * boost
                    )
            );
        }


        private Map<String, Object> createTermNode(final FieldConfig fieldConfig) {
            final QueryTypeConfig queryTypeConfig = fieldConfig.getQueryTypeConfig()
                    .orElse(SolrQueryTypeConfig.defaultConfig());

            final Map<String, Object> constantParams = queryTypeConfig.getConstantParams();

            final Map<String, Object> queryParams = new HashMap<>(2 + constantParams.size());
            queryParams.put(queryTypeConfig.getQueryParamName(), term.getValue().toString());
            queryParams.put(queryTypeConfig.getFieldParamName(), fieldConfig.getFieldName());
            queryParams.putAll(constantParams);

            return Map.of(queryTypeConfig.getTypeName(), queryParams);
        }
    }

}
