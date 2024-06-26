package querqy.converter.opensearch.javaclient.builder;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import querqy.converter.generic.builder.TermQueryBuilder;
import querqy.converter.generic.model.TermQueryDefinition;

@RequiredArgsConstructor(staticName = "create")
public class OSJavaClientTermQueryBuilder implements TermQueryBuilder<Query> {

    @Override
    public Query build(final TermQueryDefinition termQueryDefinition) {
        return createTermQuery(termQueryDefinition);
    }

    private Query createTermQuery(final TermQueryDefinition termQueryDefinition) {
        final MatchQuery.Builder termQueryBuilder = new MatchQuery.Builder();

        termQueryBuilder.field(termQueryDefinition.getFieldConfig().getFieldName());
        termQueryBuilder.query(FieldValue.of(termQueryDefinition.getTerm()));

        return new Query(termQueryBuilder.build());
    }
}
