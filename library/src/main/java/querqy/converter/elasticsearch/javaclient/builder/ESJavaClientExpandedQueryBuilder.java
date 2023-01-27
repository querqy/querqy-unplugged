package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.ExpandedQueryBuilder;
import querqy.converter.generic.model.ExpandedQueryDefinition;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientExpandedQueryBuilder implements ExpandedQueryBuilder<Query> {

    @Override
    public Query build(final ExpandedQueryDefinition<Query> expandedQueryDefinition) {
        final BoolQuery.Builder builder = new BoolQuery.Builder();

        builder.must(expandedQueryDefinition.getUserQuery());
        builder.filter(expandedQueryDefinition.getFilterQueries());

        return new Query(builder.build());
    }
}
