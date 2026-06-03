package querqy.converter.opensearch.javaclient.builder;

import org.opensearch.client.opensearch._types.query_dsl.ConstantScoreQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.ConstantScoreQueryBuilder;

@RequiredArgsConstructor(staticName = "create")
public class OSJavaClientConstantScoreQueryBuilder implements ConstantScoreQueryBuilder<Query> {

    @Override
    public Query build(Query query, float constantScore) {
        final ConstantScoreQuery.Builder constantScoreQueryBuilder = new ConstantScoreQuery.Builder();

        constantScoreQueryBuilder.filter(query);
        constantScoreQueryBuilder.boost(constantScore);

        return new Query(constantScoreQueryBuilder.build());
    }

}
