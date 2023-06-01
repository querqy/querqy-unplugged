package querqy.converter.solr.map.builder;

import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.model.BooleanQueryDefinition;

import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor(staticName = "create")
public class SolrMapBooleanQueryBuilder implements BooleanQueryBuilder<Map<String, Object>> {

    // TODO: queryTypeName

    private static final String BOOST_PARAM = "boost";
    private static final String MM_PARAM = "mm";
    private static final String MATCH_ALL_QUERY = "*:*";

    private static final String SHOULD = "should";
    private static final String MUST = "must";
    private static final String FILTER = "filter";
    private static final String MUST_NOT = "must_not";


    @Override
    public Map<String, Object> build(final BooleanQueryDefinition<Map<String, Object>> booleanQueryDefinition) {
        final _SolrMapBooleanQueryBuilder builder = _SolrMapBooleanQueryBuilder.of(booleanQueryDefinition);
        return builder.build();
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class _SolrMapBooleanQueryBuilder {

        private final BooleanQueryDefinition<Map<String, Object>> definition;

        private final Map<String, Object> innerNode = new HashMap<>(5);

        public Map<String, Object> build() {
            addClauses();

            definition.getBoost().ifPresent(boost -> innerNode.put(BOOST_PARAM, boost));
            definition.getMinimumShouldMatch().ifPresent(mm -> innerNode.put(MM_PARAM, mm));

            return Map.of("bool", innerNode);
        }

        private void addClauses() {
            if (
                    definition.getMustNotClauses().size() == 1
                    && definition.getMustClauses().isEmpty()
                    && definition.getShouldClauses().isEmpty()
            ) {
                addStandaloneMustNotClause();
                addFilterClauses();

            } else {
                addShouldClauses();
                addMustClauses();
                addFilterClauses();
                addMustNotClauses();
            }
        }

        private void addStandaloneMustNotClause() {
            innerNode.put(SHOULD, MATCH_ALL_QUERY);
            addMustNotClauses();
        }

        private void addShouldClauses() {
            if (definition.getShouldClauses().size() > 0) {
                innerNode.put(SHOULD, definition.getShouldClauses());
            }
        }

        private void addMustClauses() {
            if (definition.getMustClauses().size() > 0) {
                innerNode.put(MUST, definition.getMustClauses());
            }
        }

        private void addFilterClauses() {
            if (definition.getFilterClauses().size() > 0) {
                innerNode.put(FILTER, definition.getFilterClauses());
            }
        }

        private void addMustNotClauses() {
            if (definition.getMustNotClauses().size() > 0) {
                innerNode.put(MUST_NOT, definition.getMustNotClauses());
            }
        }
    }
}
