package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.TermQueryBuilder;
import querqy.converter.generic.model.TermQueryDefinition;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientTermQueryBuilder implements TermQueryBuilder<Query> {

    @Override
    public Query build(final TermQueryDefinition termQueryDefinition) {
        return createTermQuery(termQueryDefinition);
    }

    private Query createTermQuery(final TermQueryDefinition termQueryDefinition) {
        final MatchQuery.Builder termQueryBuilder = new MatchQuery.Builder();

        termQueryBuilder.field(termQueryDefinition.getFieldConfig().getFieldName());
        termQueryBuilder.query(termQueryDefinition.getTerm());

        return new Query(termQueryBuilder.build());
    }
}
