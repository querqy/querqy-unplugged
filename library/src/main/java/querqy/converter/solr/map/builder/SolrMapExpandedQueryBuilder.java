package querqy.converter.solr.map.builder;

import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.ExpandedQueryBuilder;
import querqy.converter.generic.model.ExpandedQueryDefinition;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(staticName = "of")
public class SolrMapExpandedQueryBuilder implements ExpandedQueryBuilder<Map<String, Object>> {

    // TODO: queryTypeName

    private final SolrMapQueryReferenceBuilder queryReferenceBuilder;

    @Override
    public Map<String, Object> build(final ExpandedQueryDefinition<Map<String, Object>> expandedQueryDefinition) {
        final _SolrMapExpandedQueryBuilder builder = _SolrMapExpandedQueryBuilder.of(expandedQueryDefinition, queryReferenceBuilder);
        return builder.build();
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class _SolrMapExpandedQueryBuilder {
        private final ExpandedQueryDefinition<Map<String, Object>> definition;
        private final SolrMapQueryReferenceBuilder queryReferenceBuilder;

        public Map<String, Object> build() {
            final Map<String, Object> query = definition.getFilterQueries().isEmpty() && definition.getBoostQueries().isEmpty()
                    ? definition.getUserQuery()
                    : buildBoolQuery();

            return buildQuery(query);
        }

        private Map<String, Object> buildBoolQuery() {
            final Map<String, Object> query = new HashMap<>(3);
            query.put("must", definition.getUserQuery());

            addFilterQueries(query);
            addBoostQueries(query);

            return Map.of("bool", query);
        }

        private void addFilterQueries(final Map<String, Object> query) {
            if (definition.getFilterQueries().size() > 0) {
                query.put("filter", definition.getFilterQueries());
            }
        }

        private void addBoostQueries(final Map<String, Object> query) {
            if (definition.getBoostQueries().size() > 0) {
                query.put("should", definition.getBoostQueries());
            }
        }

        private Map<String, Object> buildQuery(final Map<String, Object> query) {
            final Map<String, Object> wrappedQuery = new HashMap<>();
            wrappedQuery.put("query", query);
            wrappedQuery.put("queries", queryReferenceBuilder.getReferences());
            return wrappedQuery;
        }
    }
}
