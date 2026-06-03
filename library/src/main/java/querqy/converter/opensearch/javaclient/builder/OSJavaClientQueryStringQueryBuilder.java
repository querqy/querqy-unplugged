package querqy.converter.opensearch.javaclient.builder;

import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryStringQuery;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.QueryStringQueryBuilder;

@RequiredArgsConstructor(staticName = "create")
public class OSJavaClientQueryStringQueryBuilder implements QueryStringQueryBuilder<Query> {

    @Override
    public Query build(final String queryString) {
        return new Query(new QueryStringQuery.Builder().query(queryString).build());
    }
}
