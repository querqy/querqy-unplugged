package querqy.converter.opensearch.javaclient.builder;

import org.opensearch.client.opensearch._types.query_dsl.MatchAllQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.MatchAllQueryBuilder;

@RequiredArgsConstructor(staticName = "create")
public class OSJavaClientMatchAllQueryBuilder implements MatchAllQueryBuilder<Query> {

    @Override
    public Query build() {
        return new Query(new MatchAllQuery.Builder().build());
    }
}
