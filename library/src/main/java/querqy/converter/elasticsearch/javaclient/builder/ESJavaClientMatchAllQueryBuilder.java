package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.MatchAllQueryBuilder;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientMatchAllQueryBuilder implements MatchAllQueryBuilder<Query> {

    @Override
    public Query build() {
        return new Query(new MatchAllQuery.Builder().build());
    }
}
