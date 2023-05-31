package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.QueryStringQueryBuilder;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientQueryStringQueryBuilder implements QueryStringQueryBuilder<Query> {

    @Override
    public Query build(final String queryString) {
        return new Query(new QueryStringQuery.Builder().query(queryString).build());
    }
}
