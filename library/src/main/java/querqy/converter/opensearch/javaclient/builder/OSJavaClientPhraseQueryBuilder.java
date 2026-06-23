package querqy.converter.opensearch.javaclient.builder;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import querqy.converter.generic.builder.PhraseQueryBuilder;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public class OSJavaClientPhraseQueryBuilder implements PhraseQueryBuilder<Query> {

    @Override
    public Query build(final String field, final List<String> terms, final int slop, final float boost) {
        final MatchPhraseQuery.Builder builder = new MatchPhraseQuery.Builder()
                .field(field)
                .query(String.join(" ", terms))
                .boost(boost);

        if (slop > 0) {
            builder.slop(slop);
        }

        return new Query(builder.build());
    }
}
