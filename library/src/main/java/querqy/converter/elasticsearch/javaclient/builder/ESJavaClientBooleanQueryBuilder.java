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
            if (BooleanQueryDefinition.Occur.SHOULD.equals(definition.getOccur())) {
                builder.should(definition.getDismaxQueries());

            } else if (BooleanQueryDefinition.Occur.MUST.equals(definition.getOccur())) {
                builder.must(definition.getDismaxQueries());

            } else if (BooleanQueryDefinition.Occur.MUST_NOT.equals(definition.getOccur())) {
                addClausesAsMustNot();

            } else {
                throw new IllegalArgumentException();
            }
        }

        private void addClausesAsMustNot() {
            final List<Query> dismaxQueries = definition.getDismaxQueries();

            builder.mustNot(dismaxQueries);

            if (dismaxQueries.size() == 1) {
                builder.should(MATCH_ALL_QUERY);
            }

        }
    }


}
