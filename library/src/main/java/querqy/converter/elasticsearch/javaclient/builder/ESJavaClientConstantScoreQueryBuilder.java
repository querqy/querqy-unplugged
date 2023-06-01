package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.ConstantScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientConstantScoreQueryBuilder implements ConstantScoreQueryBuilder<Query> {

    @Override
    public Query build(Query query, float constantScore) {
        final ConstantScoreQuery.Builder constantScoreQueryBuilder = new ConstantScoreQuery.Builder();

        constantScoreQueryBuilder.filter(query);
        constantScoreQueryBuilder.boost(constantScore);

        return new Query(constantScoreQueryBuilder.build());
    }

}
