package querqy.converter.opensearch.javaclient.builder;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch._types.query_dsl.DisMaxQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import querqy.converter.generic.builder.DismaxQueryBuilder;
import querqy.converter.generic.model.DismaxQueryDefinition;

@RequiredArgsConstructor(staticName = "create")
public class OSJavaClientDismaxQueryBuilder implements DismaxQueryBuilder<Query> {

    @Override
    public Query build(final DismaxQueryDefinition<Query> dismaxQueryDefinition) {
        final DisMaxQuery.Builder builder = new DisMaxQuery.Builder()
                .queries(dismaxQueryDefinition.getDismaxClauses());

        dismaxQueryDefinition.getTie().ifPresent(tie -> builder.tieBreaker(tie.doubleValue()));

        return new Query(builder.build());
    }
}
