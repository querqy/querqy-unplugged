package querqy.converter.elasticsearch.javaclient.builder;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import lombok.RequiredArgsConstructor;
import querqy.converter.elasticsearch.javaclient.ESJavaClientConverterConfig;
import querqy.converter.generic.builder.RawQueryBuilder;

import java.io.StringReader;

@RequiredArgsConstructor(staticName = "of")
public class ESJavaClientRawQueryBuilder implements RawQueryBuilder<Query> {

    private final ESJavaClientConverterConfig converterConfig;

    @Override
    public Query buildFromString(final String rawQueryString) {
        switch (converterConfig.getRawQueryInputType()) {

            case QUERY_STRING_QUERY: return parseAsQueryStringQuery(rawQueryString);
            case JSON: return parseAsJson(rawQueryString);

            default:
                throw new IllegalArgumentException(this.getClass().getName() + " does not support raw query input type " +
                        converterConfig.getRawQueryInputType().name());
        }
    }

    private Query parseAsQueryStringQuery(final String rawQueryString) {
        return new Query(new QueryStringQuery.Builder().query(rawQueryString).build());
    }

    private Query parseAsJson(final String rawQueryString) {
        final Query.Builder queryBuilder = new Query.Builder();

        try(final StringReader reader = new StringReader(rawQueryString)) {
            queryBuilder.withJson(reader);
            return queryBuilder.build();
        }
    }
}
