package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.BooleanQueryBuilder;
import querqy.converter.generic.model.BooleanQueryDefinition;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientBooleanQueryBuilder implements BooleanQueryBuilder<Query> {

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

            builder.boost(definition.getBoost());
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

        private void addMustNotClauses() {
            for (final Query query : definition.getMustNotClauses()) {
                builder.mustNot(query);
            }
        }
    }
}
