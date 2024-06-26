package querqy.converter.opensearch.javaclient.builder;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.model.BooleanQueryDefinition;


@RequiredArgsConstructor(staticName = "create")
public class OSJavaClientBooleanQueryBuilder implements BooleanQueryBuilder<Query> {

    private static final Query MATCH_ALL_QUERY = new Query(MatchAllQuery.of(matchAll -> matchAll));

    @Override
    public Query build(final BooleanQueryDefinition<Query> booleanQueryDefinition) {
        return _ESJavaClientBooleanQueryBuilder.of(booleanQueryDefinition).build();
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class _ESJavaClientBooleanQueryBuilder {

        private final BooleanQueryDefinition<Query> definition;
        private final BoolQuery.Builder builder = new BoolQuery.Builder();

        public Query build() {
            addClauses();

            definition.getBoost().ifPresent(builder::boost);
            definition.getMinimumShouldMatch().ifPresent(builder::minimumShouldMatch);

            return new Query(builder.build());
        }

        private void addClauses() {
            if (definition.getMustNotClauses().size() == 1
                    && definition.getMustClauses().isEmpty() && definition.getShouldClauses().isEmpty()) {
                addStandaloneMustNotClause();

            } else {
                addShouldClauses();
                addMustClauses();
                addFilterClauses();
                addMustNotClauses();
            }
        }

        private void addStandaloneMustNotClause() {
            builder.should(MATCH_ALL_QUERY);
            addMustNotClauses();
        }

        private void addShouldClauses() {
            for (final Query query : definition.getShouldClauses()) {
                builder.should(query);
            }
        }

        private void addMustClauses() {
            for (final Query query : definition.getMustClauses()) {
                builder.must(query);
            }
        }

        private void addFilterClauses() {
            for (final Query query : definition.getFilterClauses()) {
                builder.filter(query);
            }
        }

        private void addMustNotClauses() {
            for (final Query query : definition.getMustNotClauses()) {
                builder.mustNot(query);
            }
        }
    }
}
