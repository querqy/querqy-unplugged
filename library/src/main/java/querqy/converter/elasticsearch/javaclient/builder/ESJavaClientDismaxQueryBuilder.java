package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.DisMaxQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.DismaxQueryBuilder;
import querqy.converter.generic.model.DismaxQueryDefinition;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientDismaxQueryBuilder implements DismaxQueryBuilder<Query> {

    @Override
    public Query build(final DismaxQueryDefinition<Query> dismaxQueryDefinition) {
        final DisMaxQuery.Builder builder = new DisMaxQuery.Builder()
                .queries(dismaxQueryDefinition.getDismaxClauses());

        dismaxQueryDefinition.getTie().ifPresent(tie -> builder.tieBreaker(tie.doubleValue()));

        return new Query(builder.build());
    }
}
