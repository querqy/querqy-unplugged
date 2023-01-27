package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import querqy.converter.generic.builder.RawQueryBuilder;

import java.io.StringReader;

@RequiredArgsConstructor(staticName = "create")
public class ESJavaClientRawQueryBuilder implements RawQueryBuilder<Query> {

    @Override
    public Query buildFromString(final String rawQueryString) {

        final Query.Builder queryBuilder = new Query.Builder();

        try(final StringReader reader = new StringReader(rawQueryString)) {
            queryBuilder.withJson(reader);
            return queryBuilder.build();
        }
    }
}
