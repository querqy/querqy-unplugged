package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.PhraseQueryBuilder;

import java.util.List;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientPhraseQueryBuilder implements PhraseQueryBuilder<Query> {

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
